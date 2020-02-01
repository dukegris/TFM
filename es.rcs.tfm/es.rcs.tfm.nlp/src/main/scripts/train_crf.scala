// spark-shell --packages JohnSnowLabs:spark-nlp:2.3.4 --executor-memory=8g --executor-cores=24 --driver-memory=8g
// spark-shell --packages JohnSnowLabs:spark-nlp:2.3.4 --executor-memory=16g --executor-cores=6 --driver-memory=16g
// scp -r . rcuesta@10.160.1.215:/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models
// spark-shell --packages JohnSnowLabs:spark-nlp:2.3.4 --executor-memory=8g --executor-cores=24 --driver-memory=8g > crf.txt

/*
 * DOS COSAS:
 * SENTENCE NO GENERADAS EN CONLL READER Y DA PROBLEMAS DESPUES EN NERDL. SE DEBE DE PONER DOCUMENT EN VEZ DE SENTENCE
 * LOS IOB SON RECORTADOS POR LO QUE SOLO ENTIENDE I- En los entrenamientos en ingles solo tiene I-, no B- y menos aun E-
 * En CRF requiere SENTENCE, así que ya veremos
 */
import com.johnsnowlabs.nlp.{Annotation, SparkNLP, DocumentAssembler, Finisher, AnnotatorType, AnnotatorModel}
import com.johnsnowlabs.nlp.{RecursivePipeline, LightPipeline}
import com.johnsnowlabs.nlp.annotators.{Stemmer, Tokenizer, Normalizer}
import com.johnsnowlabs.nlp.annotators.common.NerTagged
import com.johnsnowlabs.nlp.annotators.ner.{NerConverter, NerApproach}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach}
import com.johnsnowlabs.nlp.annotators.ner.crf.{NerCrfModel, NerCrfApproach}
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.embeddings.{BertEmbeddings, WordEmbeddingsFormat, WordEmbeddingsModel}
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import com.johnsnowlabs.nlp.training.CoNLL
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ResourceHelper, ReadAs}

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row, Dataset}
import org.apache.spark.ml.PipelineModel

import scala.collection.mutable

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
    println(java.time.LocalTime.now + ": NER-TRAIN: END   prepareBert " + embeddingsModel)

    embeddings

}

def prepareNerCrf (
    epochs: Int = 1,
    l2: Float = 1f,
    lossEps: Double = 1e-3,
    c0: Int = 2250000,
    minW: Double = 0.0
): NerCrfApproach = {
    // PREPARAR NerDLApproach
    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN prepareNerDL")
    val nerTagger =  new NerCrfApproach().

        // CONFIGURACION TENSORFLOW
        // setConfigProtoBytes(bytes). // ConfigProto from tensorflow, serialized into byte array
        setRandomSeed(0). // Random seed
        setMinEpochs(1). // Minimum number of epochs to train
        setMaxEpochs(epochs). // Maximum number of epochs to train
        //setBatchSize(32). // Batch size

        // ENTRENAMIENTO TENSORFLOW
        setL2(l2). // L2 regularization coefficient for CRF
        setC0(c0). // c0 defines decay speed for gradient
        setLossEps(lossEps). // If epoch relative improvement lass than this value, training is stopped
        setMinW(minW). // Features with less weights than this value will be filtered out
        //setGraphFolder("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/tensorflow").

        // VALIDACIONES
        //setDicts("").
        //setExternalFeatures(path, delimiter, readAs, options): Path to file or folder of line separated file that has something like this: Volvo:ORG with such delimiter, readAs LINE_BY_LINE or SPARK_DATASET with options passed to the latter.
        // MEDIDAS
        // setEnableOutputLogs(true). // Whether to output to annotators log folder
        // setEvaluationLogExtended(true). // Whether logs for validation to be extended: it displays time and evaluation of each label. Default is false.
        setIncludeConfidence(true). // whether to include confidence scores in annotation metadata

        // CONFIGURACION NERDLMODEL
        // setUseContrib(false) // whether to use contrib LSTM Cells. Not compatible with Windows
        // setVerbose(2). // Level of verbosity during training
        //setEntities(Array("MUT-DNA", "MUT-PRO", "MUT_SNP")). // Entities to recognize
        setInputCols(Array("sentence", "token", "pos", "embeddings")).
        setOutputCol("ner").
        setLabelColumn("label") // Column with label per each token
    println(java.time.LocalTime.now + ": NER-TRAIN: END   prepareNerDL")

    nerTagger

}

def prepareNerDL (
    epochs: Int = 1,
    lr: Float = 1e-3f,
    po: Float = 5e-3f
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
        setBatchSize(32). // Batch size  epochs: Int = 1,
  l2: Float = 1f,
  lossEps: Double = 1e-3,
  c0: Int = 2250000,
  minW: Double = 0.0


        // ENTRENAMIENTO TENSORFLOW
        setLr(lr). // Learning Rate
        setPo(po). // Learning rate decay coefficient. Real Learning Rage = lr / (1 + po * epoch)
        // setDropout(5e-1f). // Dropout coefficient
        setGraphFolder("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/tensorflow").

        // VALIDACIONES
        // setValidationSplit(validationSplit). //Choose the proportion of training dataset to be validated against the model on each Epoch. The value should be between 0.0 and 1.0 and by default it is 0.0 and off.
        setIncludeConfidence(true). // whether to include confidence scores in annotation metadata
        // setTestDataset("tmvar.test"). // Path to test dataset. If set used to calculate statistic on it during training.
        //setTestDataset("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/" + testDataSet).
        setValidationSplit(0.20f). //Si no hay conjunto de test, divide el de entrenamiento
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
    println(java.time.LocalTime.now + ": NER-TRAIN: END   prepareNerDL")

    nerTagger

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
    println(java.time.LocalTime.now + ": NER-TRAIN: END   prepareData " + fileName)

    data

}

def saveModel[_](
    model: AnnotatorModel[_],
    nerDirectory: String = "tfm_ner_pubtator"
): AnnotatorModel[_] = {

    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN saveModel")

    model.
        write.
        overwrite.
        save("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/DL_" + nerDirectory)

    println(java.time.LocalTime.now + ": NER-TRAIN: END   saveModel")

    model

}

def saveDlModel(
    model: PipelineModel,
    nerDirectory: String = "tfm_ner_pubtator"
): NerDLModel = {

    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN saveDLModel")

    // GUARDAR EL MODELO
    val ner = model.
        stages.
        filter(s => s.isInstanceOf[NerDLModel]).
        head.
        asInstanceOf[NerDLModel]

    saveModel(ner, nerDirectory)

    println(java.time.LocalTime.now + ": NER-TRAIN: END   saveDLModel")

    ner

}

def saveCrfModel(
    model: PipelineModel,
    nerDirectory: String = "tfm_ner_pubtator"
): NerCrfModel = {

    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN saveCRFModel")

    // GUARDAR EL MODELO
    val ner = model.
        stages.
        filter(s => s.isInstanceOf[NerCrfModel]).
        head.
        asInstanceOf[NerCrfModel]

    saveModel(ner, nerDirectory)

    println(java.time.LocalTime.now + ": NER-TRAIN: END   saveCRFModel")

    ner

}

def calcStat(
    correct: Int,
    predicted: Int,
    predictedCorrect: Int
): (Float, Float, Float) = {

    // prec = (predicted & correct) / predicted
    // rec = (predicted & correct) / correct
    val prec = predictedCorrect.toFloat / predicted
    val rec = predictedCorrect.toFloat / correct
    val f1 = 2 * prec * rec / (prec + rec)

    (prec, rec, f1)

}

def preparePipeline (
    nerTagger: AnnotatorModel[_],
    data: Dataset[_]
): PipelineModel = {

    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN preparePipeline")
    val converterLabel = new NerConverter().
        setInputCols("sentence", "token", "label").
        setOutputCol("label_converter")

    val converterNer = new NerConverter().
        setInputCols("sentence", "token", "ner"). // BUG
        setOutputCol("ner_converter")

    val finisher = new Finisher().
        setInputCols("document", "sentence", "token", "pos", "embeddings", "ner", "label", "ner_converter", "label_converter").
        setIncludeMetadata(true).
        setCleanAnnotations(false)

    import spark.implicits._
    val stages = Array(
        nerTagger,
        converterLabel,
        converterNer,
        finisher
    )

    val trainPipeline = new RecursivePipeline().setStages(stages)
    val model = trainPipeline.fit(data)
    println(java.time.LocalTime.now + ": NER-TRAIN: END   preparePipeline")

    model

}

def measure(
    result: Dataset[_]
): Unit = {

    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN measure")
    val rows = result.select("ner_converter", "label_converter").collect()

    val correctPredicted = mutable.Map[String, Int]()
    val predicted = mutable.Map[String, Int]()
    val correct = mutable.Map[String, Int]()
    var toPrintErrors = 0

    for (row <- rows) {

        val predictions = NerTagged.getAnnotations(row, 0).filter(a => a.result != "O")
        for (p <- predictions) {
            val tag = p.metadata("entity")
            predicted(tag) = predicted.getOrElse(tag, 0) + 1
        }

        val labels = NerTagged.getAnnotations(row, 1).filter(a => a.result != "O")
        for (l <- labels) {
            val tag = l.metadata("entity")
            correct(tag) = correct.getOrElse(tag, 0) + 1
        }

        val correctPredictions = labels.toSet.intersect(predictions.toSet)
        for (a <- correctPredictions) {
            val tag = a.metadata("entity")
            correctPredicted(tag) = correctPredicted.getOrElse(tag, 0) + 1
        }

        if (toPrintErrors > 0) {

            for (p <- predictions) {
                if (toPrintErrors > 0 && !correctPredictions.contains(p)) {
                    System.out.println(s"Predicted\t${p.result}\t${p.begin}\t${p.end}\t${p.metadata("text")}")
                    toPrintErrors -= 1
                }
            }

            for (p <- labels) {
                if (toPrintErrors > 0 && !correctPredictions.contains(p)) {
                    System.out.println(s"Correct\t${p.result}\t${p.begin}\t${p.end}\t${p.metadata("text")}")
                    toPrintErrors -= 1
                }
            }

        }
    }

    val (prec, rec, f1) = calcStat(correct.values.sum, predicted.values.sum, correctPredicted.values.sum)
    System.out.println(s"\tprec\t\trec\t\tf1")
    System.out.println(s"\t$prec\t$rec\t$f1")

    val keys = (correct.keys ++ predicted.keys ++ correctPredicted.keys).toList.distinct
    for (key <- keys) {
        val (prec, rec, f1) = calcStat(correct.getOrElse(key, 0), predicted.getOrElse(key, 0), correctPredicted.getOrElse(key, 0))
        System.out.println(s"$key\t$prec\t$rec\t$f1")
    }
    println(java.time.LocalTime.now + ": NER-TRAIN: END   measure")

}

def measureDL(
    embeddings: BertEmbeddings,
    ner: NerDLModel,
    data: Dataset[_]
): Unit = {
    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN measureDL")
    val model = preparePipeline(ner, data)
    val result = model.transform(data)
    println(java.time.LocalTime.now + ": NER-TRAIN: END   measureDL")
    measure(result)

    // ESTADISTICAS DEL CONJUNTO DE ENTRENAMIENTO
    val labeledData = NerTagged.
        collectTrainingInstances(
            result,
            Seq("sentence", "token", "embeddings"),
            "label")

    println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel")
    ner.getModelIfNotSet.measure(labeledData, (s: String) => System.out.println(s), true, 0)
    println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel")

}

def measureCRF(
    embeddings: BertEmbeddings,
    ner: NerCrfModel,
    data: Dataset[_]
): Unit = {
    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN measureCRF")
    val model = preparePipeline(ner, data)
    val result = model.transform(data)
    println(java.time.LocalTime.now + ": NER-TRAIN: END   measureCRF")
    measure(result)
}

def run(): Unit = {

    val BERT_MODELS = Array(
        Array("biobert_v1.1_pubmed_M-128_B-32", 768, 128),
        Array("biobert_v1.1_pubmed_M-512_B-32", 768, 512),

        Array("cased_L-12_H-768_A-12_M-128_B-32", 768, 128),
        Array("cased_L-12_H-768_A-12_M-512_B-32", 768, 512),
        Array("uncased_L-12_H-768_A-12_M-128_B-32", 768, 128),
        Array("uncased_L-12_H-768_A-12_M-512_B-32", 768, 512),
        Array("multi_cased_L-12_H-768_A-12_M-128_B-32", 768, 128),
        Array("multi_cased_L-12_H-768_A-12_M-512_B-32", 768, 512),

        Array("cased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
        Array("cased_L-24_H-1024_A-16_M-512_B-32", 1024, 512),
        Array("uncased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
        Array("uncased_L-24_H-1024_A-16_M-512_B-32", 1024, 512),

        Array("wwm_cased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
        Array("wwm_cased_L-24_H-1024_A-16_M-512_B-32", 1024, 512)
        Array("wwm_uncased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
        Array("wwm_uncased_L-24_H-1024_A-16_M-512_B-32", 1024, 512),
        //Array("bert_base_cased_en_2.2.0_2.4_1566671427398", 768, 128)
        //Array("biobert_v1.1_pubmed_M-128_B-32", 768, 128)
    )

    val NERDL_PARAMS = Array(
        Array("DL01_ma",  1, 1e-3f, 5e-4f),
        Array("DL10_ma", 10, 1e-3f, 5e-4f),
        Array("DL99_ma", 99, 1e-3f, 5e-4f),
        Array("DL01_mi",  1, 1e-4f, 5e-3f),
        Array("DL10_mi", 10, 1e-4f, 5e-3f),
        Array("DL99_mi", 99, 1e-4f, 5e-3f),
        Array("DL10_me",  1, 1e-4f, 5e-4f),
        Array("DL10_me", 10, 1e-4f, 5e-4f),
        Array("DL99_me", 99, 1e-4f, 5e-4f)
        //Array("DL10_me", 10, 1e-4f, 5e-3f)
    )

    val NERCRF_PARAMS = Array(
        Array("CRF10_ma",  1, 1f, 1e-3, 2250000, 0.0f),
        Array("CRF10_ma", 10, 1f, 1e-3, 2250000, 0.0f),
        Array("CRF99_ma", 99, 1f, 1e-3, 2250000, 0.0f),
        Array("CRF10_me",  1, 1f, 1e-4, 2250000, 0.0f),
        Array("CRF10_me", 10, 1f, 1e-4, 2250000, 0.0f),
        Array("CRF99_me", 99, 1f, 1e-4, 2250000, 0.0f)
        //Array("CRF10_me", 10, 1f, 1e-4f, 2250000, 0.0)
    )

    var bertModel = 0
    var nerModel = 0

    val nerReader = CoNLL(explodeSentences = false)
    val trainData = prepareData(nerReader, "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/ner_txt_train.conll")
    val testData = prepareData(nerReader, "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/ner_txt_test.conll")

    // ITERATE BERT MODELS
    for (bertModel <- 0 to BERT_MODELS.length - 1) {
    //for (bertModel <- 9 to 9) {

        println(BERT_MODELS(bertModel)(0).toString + " ----------------------------------------------------------------")

        val embeddings = prepareBert(
            BERT_MODELS(bertModel)(0).toString,
            BERT_MODELS(bertModel)(1).asInstanceOf[Int],
            BERT_MODELS(bertModel)(2).asInstanceOf[Int])

        val trainDataEmbeddings = embeddings.transform(trainData)
        val testDataEmbeddings = embeddings.transform(testData)

        // ITERATE NER PARAMS
        for (nerModel <- 0 to NERDL_PARAMS.length - 1) {
        //for (nerModel <- 2 to 2) {

            println("DL  - " + NERDL_PARAMS(nerModel)(0).toString + " -------------------------------------------------")
            val nerTagger = prepareNerDL(
                NERDL_PARAMS(nerModel)(1).asInstanceOf[Int],
                NERDL_PARAMS(nerModel)(2).asInstanceOf[Float],
                NERDL_PARAMS(nerModel)(3).asInstanceOf[Float])

            println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN TRAIN")
            val ner = nerTagger.fit(trainDataEmbeddings)
            println(java.time.LocalTime.now + ": NER-TRAIN: END   TRAIN")

            saveModel(
                ner,
                "TFM-BIODL-" + NERDL_PARAMS(nerModel)(0).toString + "-" + BERT_MODELS(bertModel)(0).toString)

            measureDL(embeddings, ner, trainDataEmbeddings)
            measureDL(embeddings, ner, testDataEmbeddings)

        }

        // ITERATE NER PARAMS
        /*
        for (nerModel <- 0 to NERCRF_PARAMS.length - 1) {
        //for (nerModel <- 2 to 2) {

            println("CRF - " + NERCRF_PARAMS(nerModel)(0).toString + " ------------------------------------------------")
            val nerTagger = prepareNerCrf(
                NERCRF_PARAMS(nerModel)(1).asInstanceOf[Int],
                NERCRF_PARAMS(nerModel)(2).asInstanceOf[Float],
                NERCRF_PARAMS(nerModel)(3).asInstanceOf[Double],
                NERCRF_PARAMS(nerModel)(4).asInstanceOf[Int],
                NERCRF_PARAMS(nerModel)(5).asInstanceOf[Float])

            println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN TRAIN")
            val ner = nerTagger.fit(trainDataEmbeddings)
            println(java.time.LocalTime.now + ": NER-TRAIN: END   TRAIN")

            saveModel(
                ner,
                "TFM-BIOCRL-" + NERCRF_PARAMS(nerModel)(0).toString + "-" + BERT_MODELS(bertModel)(0).toString)

            measureCRF(embeddings, ner, trainDataEmbeddings)
            measureCRF(embeddings, ner, testDataEmbeddings)

        }
        */
    }

}

//run()








    val BERT_MODELS = Array(
        //Array("biobert_v1.1_pubmed_M-128_B-32", 768, 128),
        //Array("biobert_v1.1_pubmed_M-512_B-32", 768, 512),

        //Array("cased_L-12_H-768_A-12_M-128_B-32", 768, 128),
        //Array("cased_L-12_H-768_A-12_M-512_B-32", 768, 512),
        //Array("multi_cased_L-12_H-768_A-12_M-128_B-32", 768, 128),
        //Array("multi_cased_L-12_H-768_A-12_M-512_B-32", 768, 512),

        //Array("cased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
        //Array("cased_L-24_H-1024_A-16_M-512_B-32", 1024, 512),
        //Array("wwm_uncased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
        //Array("wwm_uncased_L-24_H-1024_A-16_M-512_B-32", 1024, 512),
        //Array("wwm_cased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
        //Array("wwm_cased_L-24_H-1024_A-16_M-512_B-32", 1024, 512)
        //Array("bert_base_cased_en_2.2.0_2.4_1566671427398", 768, 128)
        Array("biobert_v1.1_pubmed_M-128_B-32", 768, 128)
    )

    val NERDL_PARAMS = Array(
        //Array("DL01_ma",  1, 1e-3f, 5e-3f),
        //Array("DL10_ma", 10, 1e-4f, 5e-4f),
        //Array("DL99_ma", 99, 1e-4f, 5e-4f),
        //Array("DL01_mi",  1, 1e-4f, 5e-4f),
        //Array("DL10_mi", 10, 1e-4f, 5e-4f),
        //Array("DL99_mi", 99, 1e-4f, 5e-4f),
        //Array("DL10_me", 10, 1e-4f, 5e-3f),
        //Array("DL99_me", 99, 1e-3f, 5e-4f)
        Array("DL10_me", 10, 1e-4f, 5e-3f)
    )

    var bertModel = 0
    var nerModel = 0

    val nerReader = CoNLL(explodeSentences = false)
    val trainData = prepareData(nerReader, "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/ner_txt_train.conll")
    val testData = prepareData(nerReader, "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/ner_txt_test.conll")

    val embeddings = prepareBert(
        BERT_MODELS(bertModel)(0).toString,
        BERT_MODELS(bertModel)(1).asInstanceOf[Int],
        BERT_MODELS(bertModel)(2).asInstanceOf[Int])

    val trainDataEmbeddings = embeddings.transform(trainData)
    val testDataEmbeddings = embeddings.transform(testData)



    val nerTagger = prepareNerDL(
        NERDL_PARAMS(nerModel)(1).asInstanceOf[Int],
        NERDL_PARAMS(nerModel)(2).asInstanceOf[Float],
        NERDL_PARAMS(nerModel)(3).asInstanceOf[Float])

    val ner = nerTagger.fit(trainDataEmbeddings)

    saveModel(
        ner,
        "TFM-BIODL-" + NERDL_PARAMS(nerModel)(0).toString + "-" + BERT_MODELS(bertModel)(0).toString)

    val model = preparePipeline(ner, testDataEmbeddings)
    val result = model.transform(testDataEmbeddings)

            measureDL(embeddings, ner, trainDataEmbeddings)
            measureDL(embeddings, ner, testDataEmbeddings)

    val rows = result.select("ner", "label", "finished_ner_converter_metadata", "finished_label_converter_metadata").collect()
    rows(9)(0)
    rows(9)(1)
    rows(9)(2)
    rows(9)(3)










bertModel = 2
    val embeddings = prepareBert(
        BERT_MODELS(bertModel)(0).toString,
        BERT_MODELS(bertModel)(1).asInstanceOf[Int],
        BERT_MODELS(bertModel)(2).asInstanceOf[Int])

    val trainDataEmbeddings = embeddings.transform(trainData)


import scala.Any
import scala.collection.mutable.WrappedArray
import spark.implicits._

val a = trainDataEmbeddings.
    select("embeddings", "token", "text").
    filter(r => r(3).asInstanceOf[WrappedArray[String]].length != r(6).asInstanceOf[WrappedArray[String]].length)
    map(r => Array(r(0).asInstanceOf[WrappedArray[String]].length, r(1).asInstanceOf[WrappedArray[String]].length))

b.show()





    val NERCRF_PARAMS = Array(
        //Array("CRF10_me", 10, 1f, 1e-4f, 2250000, 0.0),
        //Array("CRF99_me", 99, 1f, 1e-3f, 2250000, 0.0)
        Array("CRF10_me", 10, 1f, 1e-4, 2250000, 0.0f)
    )

    println("CRF - " + NERCRF_PARAMS(nerModel)(0).toString + " --------------------------------------------------------")
    val nerTaggerCRF = prepareNerCrf(
        NERCRF_PARAMS(nerModel)(1).asInstanceOf[Int],
        NERCRF_PARAMS(nerModel)(2).asInstanceOf[Float],
        NERCRF_PARAMS(nerModel)(3).asInstanceOf[Double],
        NERCRF_PARAMS(nerModel)(4).asInstanceOf[Int],
        NERCRF_PARAMS(nerModel)(5).asInstanceOf[Float])



    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN TRAIN")
    val trainDataOk = trainDataEmbeddings.
        filter(r => r(3).asInstanceOf[WrappedArray[String]].length == r(6).asInstanceOf[WrappedArray[String]].length)
    val nerCRF = nerTaggerCRF.fit(trainDataOk)
    println(java.time.LocalTime.now + ": NER-TRAIN: END   TRAIN")

    saveModel(
        nerCRF,
        "TFM-BIOCRL-" + NERCRF_PARAMS(nerModel)(0).toString + "-" + BERT_MODELS(bertModel)(0).toString)

    measureCRF(embeddings, nerCRF, trainDataOk)
    measureCRF(embeddings, nerCRF, testDataEmbeddings)

    val rows = result.select("finished_document_metadata", "finished_sentence_metadata", "finished_token_metadata", "finished_embeddings_metadata").collect()

    val rows = result.collect()

    rows(9)(0)
    rows(9)(1)
    rows(9)(2).toString().contains("1]")
    rows(9)(3)

    rows.filter(r => r(2).toString().contains("1]")).foreach(r => println(r(2)))
    rows.filter(r => r(2).toString().contains("1]")).foreach(r => println(r(2)))





















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
    println(java.time.LocalTime.now + ": NER-TRAIN: END   prepareTestDataSet")
}

def preparePipeline (
    nerTagger: NerCrfApproach
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
        nerTagger,
        converter,
        finisher
    )

    val trainPipeline = new RecursivePipeline().setStages(stages)
    println(java.time.LocalTime.now + ": NER-TRAIN: END   preparePipeline")

    trainPipeline

}

def train(
    pipeline: RecursivePipeline,
    data: Dataset[_]
): PipelineModel = {

    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN TRAIN")
    val model = pipeline.fit(data)
    println(java.time.LocalTime.now + ": NER-TRAIN: END   TRAIN")

    model

}

  def calcStat(
      correct: Int,
      predicted: Int,
      predictedCorrect: Int): (Float, Float, Float) = {

    // prec = (predicted & correct) / predicted
    // rec = (predicted & correct) / correct
    val prec = predictedCorrect.toFloat / predicted
    val rec = predictedCorrect.toFloat / correct
    val f1 = 2 * prec * rec / (prec + rec)

    (prec, rec, f1)

  }

def measure(
    pipelineModel: PipelineModel,
    ner: NerCrfModel,
    data: Dataset[_]
): Unit = {

    println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN measure")
    val result = pipelineModel.transform(data)

    val rows = result.select("ner", "label").collect()

    val correctPredicted = mutable.Map[String, Int]()
    val predicted = mutable.Map[String, Int]()
    val correct = mutable.Map[String, Int]()
    var toPrintErrors = 0

    for (row <- rows) {

      val predictions = NerTagged.getAnnotations(row, 0).filter(a => a.result != "O")
      for (p <- predictions) {
        val tag = p.metadata("entity")
        predicted(tag) = predicted.getOrElse(tag, 0) + 1
      }

      val labels = NerTagged.getAnnotations(row, 1).filter(a => a.result != "O")
      for (l <- labels) {
        val tag = l.metadata("entity")
        correct(tag) = correct.getOrElse(tag, 0) + 1
      }

      val correctPredictions = labels.toSet.intersect(predictions.toSet)
      for (a <- correctPredictions) {
        val tag = a.metadata("entity")
        correctPredicted(tag) = correctPredicted.getOrElse(tag, 0) + 1
      }

      if (toPrintErrors > 0) {

        for (p <- predictions) {
          if (toPrintErrors > 0 && !correctPredictions.contains(p)) {
            System.out.println(s"Predicted\t${p.result}\t${p.begin}\t${p.end}\t${p.metadata("text")}")
            toPrintErrors -= 1
          }
        }

        for (p <- labels) {
          if (toPrintErrors > 0 && !correctPredictions.contains(p)) {
            System.out.println(s"Correct\t${p.result}\t${p.begin}\t${p.end}\t${p.metadata("text")}")
            toPrintErrors -= 1
          }
        }

      }
    }

    val (prec, rec, f1) = calcStat(correct.values.sum, predicted.values.sum, correctPredicted.values.sum)
    System.out.println(s"$prec\t$rec\t$f1")

    val keys = (correct.keys ++ predicted.keys ++ correctPredicted.keys).toList.distinct
    for (key <- keys) {
      val (prec, rec, f1) = calcStat(correct.getOrElse(key, 0), predicted.getOrElse(key, 0), correctPredicted.getOrElse(key, 0))
      System.out.println(s"$key\t$prec\t$rec\t$f1")
    }

    // Nos hace falta un grafo para con mas tags al incluir B-, I-, E- para cada mutacion 4
    // Tenemos 14 4x3 + O + ""
    val instances = NerTagged.collectTrainingInstances(result.toDF(), Array("sentence", "token", "embeddings"), "label")
    val tags = instances.flatMap(r => r._1.labels).distinct
    println("Numero de tags (16) : " + tags.length)

    // Tenemos 768 dimensiones (procede del modelo BERT), ahora 1024
    val sentences = instances.map(r => r._2)
    //val embeddingsDim = nerTagger.calculateEmbeddingsDim(sentences)
    //println("Numero de dimensiones (1024) : " + embeddingsDim)

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
    //ner.getModelIfNotSet.measure(labeledData, (s: String) => System.out.println(s), true, 0)
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

    println(java.time.LocalTime.now + ": NER-TRAIN: END   measure")

}
