// spark-shell --packages JohnSnowLabs:spark-nlp:2.3.4 --executor-memory=8g --executor-cores=24 --driver-memory=8g
// spark-shell --packages JohnSnowLabs:spark-nlp:2.3.4 --executor-memory=16g --executor-cores=6 --driver-memory=16g
// scp -r . rcuesta@10.160.1.215:/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models

import com.johnsnowlabs.nlp.{Annotation, SparkNLP, DocumentAssembler, Finisher, AnnotatorType}
import com.johnsnowlabs.nlp.{RecursivePipeline, LightPipeline}
import com.johnsnowlabs.nlp.annotators.{Stemmer, Tokenizer, Normalizer}
import com.johnsnowlabs.nlp.annotators.common.NerTagged
import com.johnsnowlabs.nlp.annotators.ner.{NerConverter, NerApproach}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach} 
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.embeddings.{BertEmbeddings, WordEmbeddingsFormat, WordEmbeddingsModel}
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import com.johnsnowlabs.nlp.training.CoNLL
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ResourceHelper, ReadAs}

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row, Dataset}
import org.apache.spark.ml.PipelineModel

def prepareBert (
    embeddingsModel: String,
    embeddingsDimensions: Int = 1024,
    embeddingsSentenceLength: Int = 512
): BertEmbeddings = {

    // PREPARAR BERT
    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN prepareBert " + embeddingsModel)
    val embeddings = BertEmbeddings.
        load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/" + embeddingsModel).
        setDimension(embeddingsDimensions).
        setMaxSentenceLength(embeddingsSentenceLength).
        setInputCols(Array("sentence", "token")).
        setOutputCol("embeddings")
    println(java.time.LocalTime.now + ": NER-TRAIN: END prepareBert " + embeddingsModel)

    embeddings

}

def prepareTestDataSet (
    embeddings: BertEmbeddings,
    data: Dataset[_],
    testDataSet: String = "CONLL-eng.tmp.parquet" 
): Unit = {
    // PREPARAR DATOS DE VALIDACION PARA NerDLApproach
    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN prepareTestDataSet")
    embeddings.
        transform(data).
        write.
        mode("overwrite").
        format("parquet").
        save(               "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/" + testDataSet)
    println(java.time.LocalTime.now + ": NER-TRAIN: END prepareTestDataSet")
}

def prepareNerDL (
    epochs: Int = 1,
    lr: Float = 1e-3f,
    po: Float = 5e-3f,
    testDataSet: String = "CONLL-eng.tmp.parquet"
): NerDLApproach = {

    // PREPARAR NerDLApproach
    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN prepareNerDL")
    val nerTagger =  new NerDLApproach().

        // CONFIGURACION TENSORFLOW
        // setConfigProtoBytes(bytes). // ConfigProto from tensorflow, serialized into byte array
        // setGraphFolder(path). // Folder path that contain external graph files
        setRandomSeed(0). // Random seed
        setMinEpochs(1). // Minimum number of epochs to train
        setMaxEpochs(epochs). // Maximum number of epochs to train
        setBatchSize(32). // Batch size

        // ENTRENAMIENTO TENSORFLOW
        setLr(lr). // Learning Rate
        setPo(po). // Learning rate decay coefficient. Real Learning Rage = lr / (1 + po * epoch)
        // setDropout(5e-1f). // Dropout coefficient
        setGraphFolder("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/tensorflow").

        // VALIDACIONES
        // setValidationSplit(validationSplit). //Choose the proportion of training dataset to be validated against the model on each Epoch. The value should be between 0.0 and 1.0 and by default it is 0.0 and off.
        setIncludeConfidence(true). // whether to include confidence scores in annotation metadata
        // setTestDataset("tmvar.test"). // Path to test dataset. If set used to calculate statistic on it during training.
        setTestDataset("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/" + testDataSet).
        //setValidationSplit(0.25). Si no hay conjunto de test, divide el de entrenamiento
        // setTestDataset(preparedTestData).

        // MEDIDAS
        setEnableOutputLogs(true). // Whether to output to annotators log folder
        setEvaluationLogExtended(true). // Whether logs for validation to be extended: it displays time and evaluation of each label. Default is false.
        setIncludeConfidence(true). // whether to include confidence scores in annotation metadata"

        // CONFIGURACI�N NERDLMODEL
        // setUseContrib(false) // whether to use contrib LSTM Cells. Not compatible with Windows
        // setVerbose(2). // Level of verbosity during training
        //setEntities(Array("MUT-DNA", "MUT-PRO", "MUT_SNP")). // Entities to recognize
        setInputCols(Array("sentence", "token", "embeddings")).
        setOutputCol("ner").
        setLabelColumn("label") // Column with label per each token
    println(java.time.LocalTime.now + ": NER-TRAIN: END prepareNerDL")

    nerTagger

}

def preparePipeline (
    embeddings: BertEmbeddings,
    nerTagger: NerDLApproach
): RecursivePipeline = {

    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN preparePipeline")
    val converter = new NerConverter().
        setInputCols("sentence", "token", "label").
        setOutputCol("label_converter")

    val finisher = new Finisher().
        setInputCols("document", "sentence", "token", "pos", "embeddings", "ner", "label", "label_converter").
        setIncludeMetadata(true).
        setCleanAnnotations(false)

    import spark.implicits._
    val emptyData = spark.emptyDataset[String].toDF("text")
    val stages = Array(
        embeddings,
        nerTagger,
        converter,
        finisher
    )

    val trainPipeline = new RecursivePipeline().setStages(stages)
    println(java.time.LocalTime.now + ": NER-TRAIN: END preparePipeline")

    trainPipeline

}

def prepareData (
    nerReader: CoNLL, 
    fileName: String
): Dataset[_] = {

    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN prepareData " + fileName)
    val file = ExternalResource(
        fileName,
        ReadAs.LINE_BY_LINE, 
        Map.empty[String, String])

    val data = nerReader.
        readDataset(
            ResourceHelper.spark, 
            file.path)
    println(java.time.LocalTime.now + ": NER-TRAIN: END prepareData " + fileName)

    data

}

def train(
    pipeline: RecursivePipeline,
    data: Dataset[_]
): PipelineModel = {

    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN TRAIN")
    val model = pipeline.fit(data)
    println(java.time.LocalTime.now + ": NER-TRAIN: END TRAIN")

    model

}

def saveModel(
    model: PipelineModel,
    nerDirectory: String = "tfm_ner_eng"
): NerDLModel = {

    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN saveModel")

    // GUARDAR EL MODELO
    val ner = model.
        stages.
        filter(s => s.isInstanceOf[NerDLModel]).
        head.
        asInstanceOf[NerDLModel]

    ner.  
        write.
        overwrite.
        save("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/" + nerDirectory)

    println(java.time.LocalTime.now + ": NER-TRAIN: END saveModel")

    ner

}

def measure(
    pipelineModel: PipelineModel,
    nerTagger: NerDLApproach,
    ner: NerDLModel,
    data: Dataset[_]
): Unit = {

    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN measure")
    val result = pipelineModel.transform(data)

    // Nos hace falta un grafo para con mas tags al incluir B-, I-, E- para cada mutacion 4
    // Tenemos 14 4x3 + O + "" 
    val instances = NerTagged.collectTrainingInstances(result.toDF(), Array("sentence", "token", "embeddings"), "label")
    val tags = instances.flatMap(r => r._1.labels).distinct
    println("Numero de tags (16) : " + tags.length)

    // Tenemos 768 dimensiones (procede del modelo BERT), ahora 1024
    val sentences = instances.map(r => r._2)
    val embeddingsDim = nerTagger.calculateEmbeddingsDim(sentences)
    println("Numero de dimensiones (1024) : " + embeddingsDim)

    // Tenemos 88 caracteres diferentes, ahora 62
    val chars = instances.flatMap(r => r._2.tokens.flatMap(token => token.token.toCharArray)).distinct
    println("Numero de caracteres (128): " + chars.length)

    // ESTADISTICAS DEL CONJUNTO DE ENTRENAMIENTO
    val labeledData = NerTagged.
        collectTrainingInstances(
            result, 
            Seq("sentence", "token", "embeddings"), 
            "label")

    println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel")
    ner.getModelIfNotSet.measure(labeledData, (s: String) => System.out.println(s), true, 0)
    println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel")

    val dataActualListOfNamedEntitiesMap = result.
        select("finished_ner").
        collectAsList().
        toArray.
        map(x=>x.toString.drop(1).dropRight(1).split("@")).
        map(keyValuePair=>keyValuePair.
            map(x=>(x.split("->").lastOption.get,x.slice(x.indexOf("->")+2,x.indexOf("#")))).
            filter(!_._1.equals("O")).
            groupBy(_._1).
            mapValues(_.map(_._2).toList))

    val length=dataActualListOfNamedEntitiesMap.length
    println("Numero de discrepancias(128): " + length)

    /*
    labeledData(0)._1    
    data.limit(1).select("text", "finished_ner").foreach { row => { 
        row.toSeq.foreach { col => println(col) } 
    } }
    for(index<-0 until length){
        println("Keys present in actualOutputMap but not in actualOutputMap:  %s".format(dataActualListOfNamedEntitiesMap(index)))
    }
    data.withColumn("ner1", org.apache.spark.sql.functions.explode(data.col("ner"))).show
    */
    
    println(java.time.LocalTime.now + ": NER-TRAIN: END measure")

}

val BERT_MODELS = Array(
    Array("biobert_v1.1_pubmed_M-128_B-32", 768, 128),
    Array("biobert_v1.1_pubmed_M-512_B-32", 768, 512),

    Array("cased_L-12_H-768_A-12_M-128_B-32", 768, 128),
    Array("cased_L-12_H-768_A-12_M-512_B-32", 768, 512),
    Array("multi_cased_L-12_H-768_A-12_M-128_B-32", 768, 128),
    Array("multi_cased_L-12_H-768_A-12_M-512_B-32", 768, 512),

    Array("cased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
    Array("cased_L-24_H-1024_A-16_M-512_B-32", 1024, 512),
    Array("wwm_cased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
    Array("wwm_cased_L-24_H-1024_A-16_M-512_B-32", 1024, 512),
    Array("wwm_uncased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
    Array("wwm_uncased_L-24_H-1024_A-16_M-512_B-32", 1024, 512)
)

val NER_PARAMS = Array(
    Array("ex01_ma",  1, 1e-3f, 5e-3f),
    Array("ex10_ma", 10, 1e-4f, 5e-4f),
    Array("ex99_ma", 99, 1e-4f, 5e-4f),
    Array("ex01_mi",  1, 1e-4f, 5e-4f),
    Array("ex10_mi", 10, 1e-4f, 5e-4f),
    Array("ex99_mi", 99, 1e-4f, 5e-4f)
)

var bertModel = 0
var nerModel = 0

val nerReader =       CoNLL()
val trainData =       prepareData(nerReader, "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/sparknlp/eng.train")
val testData =        prepareData(nerReader, "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/sparknlp/eng.testb")
val validationData =  prepareData(nerReader, "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/sparknlp/eng.testa")

// ITERATE BERT MODELS
for (bertModel <- 0 to BERT_MODELS.length - 1) {
//for (bertModel <- 9 to 9) {    

    println(BERT_MODELS(bertModel)(0).toString + " --------------------------------------------------------------------------------------------------------------------------")

    val embeddings =      prepareBert(
        BERT_MODELS(bertModel)(0).toString, 
        BERT_MODELS(bertModel)(1).asInstanceOf[Int], 
        BERT_MODELS(bertModel)(2).asInstanceOf[Int])

    prepareTestDataSet (
        embeddings, 
        validationData,  
        "CONLL-" + BERT_MODELS(bertModel)(0).toString + ".parquet")

    // ITERATE NER PARAMS
    for (nerModel <- 0 to NER_PARAMS.length - 1) {
    //for (nerModel <- 2 to 2) {

        println(NER_PARAMS(nerModel)(0).toString + " --------------------------------------------------------------------------------------------------------------------------")
        val nerTagger =       prepareNerDL(
            NER_PARAMS(nerModel)(1).asInstanceOf[Int], 
            NER_PARAMS(nerModel)(2).asInstanceOf[Float], 
            NER_PARAMS(nerModel)(3).asInstanceOf[Float], 
            "CONLL-" + BERT_MODELS(bertModel)(0) + ".parquet")
        val pipeline =        preparePipeline (embeddings, nerTagger)
        val pipelineModel =   train(pipeline, trainData)
        val ner =             saveModel(
            pipelineModel, 
            "TFM-" + NER_PARAMS(nerModel)(0).toString + "-" + BERT_MODELS(bertModel)(0).toString)

        measure(pipelineModel, nerTagger, ner, trainData)
        measure(pipelineModel, nerTagger, ner, testData)
        measure(pipelineModel, nerTagger, ner, validationData)
    }
}
