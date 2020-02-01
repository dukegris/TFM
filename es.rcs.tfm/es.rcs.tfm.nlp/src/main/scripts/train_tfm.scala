// spark-shell --packages JohnSnowLabs:spark-nlp:2.3.4 --executor-memory=8g --executor-cores=24 --driver-memory=8g
// spark-shell --packages JohnSnowLabs:spark-nlp:2.3.4 --executor-memory=16g --executor-cores=6 --driver-memory=16g
// scp -r . rcuesta@10.160.1.215:/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models
// spark-shell --packages JohnSnowLabs:spark-nlp:2.3.4 --jars --executor-memory=8g --executor-cores=24 --driver-memory=8g > crf.txt
// spark-shell --packages com.johnsnowlabs.nlp:spark-nlp_2.11:2.3.5,org.slf4j:slf4j-log4j12:1.7.28 --jars es.rcs.tfm.nlp/target/RCS-Nlp-0.0.4-SNAPSHOT.jar --executor-memory=32g --executor-cores=6 --driver-memory=24g --conf "spark.executor.extraJavaOptions='-Dlog4j.configuration=/opt/spark/conf/log4j.properties'" --conf "spark.driver.maxResultSize=3g" > dl.txt
sc.setLogLevel("WARN")

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
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ResourceHelper, OutputHelper, ReadAs}

import es.rcs.tfm.nlp.model.TfmType
import es.rcs.tfm.nlp.util.TfmHelper

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row, Dataset}
import org.apache.spark.ml.PipelineModel

import scala.Any
import scala.collection.mutable
import scala.collection.mutable.WrappedArray
import spark.implicits._

val BERT_DIR = "file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/"
val BERT_MODELS = Array(
    //Array("biobert_v1.1_pubmed_M-128_B-32", 768, 128),
    Array("biobert_v1.1_pubmed_M-512_B-32", 768, 512)

    //Array("cased_L-12_H-768_A-12_M-128_B-32", 768, 128),
    //Array("cased_L-12_H-768_A-12_M-512_B-32", 768, 512),
    //Array("uncased_L-12_H-768_A-12_M-128_B-32", 768, 128),
    //Array("uncased_L-12_H-768_A-12_M-512_B-32", 768, 512),
    //Array("multi_cased_L-12_H-768_A-12_M-128_B-32", 768, 128),
    //Array("multi_cased_L-12_H-768_A-12_M-512_B-32", 768, 512),

    //Array("wwm_cased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
    //Array("wwm_cased_L-24_H-1024_A-16_M-512_B-32", 1024, 512),
    //Array("wwm_uncased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
    //Array("wwm_uncased_L-24_H-1024_A-16_M-512_B-32", 1024, 512),

    //Array("cased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
    //Array("cased_L-24_H-1024_A-16_M-512_B-32", 1024, 512),
    //Array("uncased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
    //Array("uncased_L-24_H-1024_A-16_M-512_B-32", 1024, 512)
)

val CONLL_MODELS = Array(
   // Array("ner_txt_cdr_train.conll", "ner_txt_cdr_test.conll"),
    //Array("ner_txt_cdr_train.conll", "ner_txt_dnorm_test.conll"),
    //Array("ner_txt_cdr_train.conll", "ner_txt_chem_test.conll"),
    //Array("ner_txt_cdr_devel.conll", "ner_txt_chem_test.conll"),
    //Array("JNLPBA_train.conll",      "JNLPBA_test.conll"),
    Array("ner_txt_train.conll",     "ner_txt_test.conll")
)

val TFGRAPH_DIR = "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/tensorflow"
val NER_MODEL_DIR = "file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/"
val NERDL_PARAMS = Array(
    //FUERA POR DROP: Array("DL020 LR:0.003   PO:0        DROP:0.10", 20, 3e-3f, 0f, 0.10f),
    //YA: Array("DL020 LR:0.003   PO:0        DROP:0.50", 20, 3e-3f, 0f, 0.50f),
    //YA: Array("DL020 LR:0.003   PO:0        DROP:0.68", 20, 3e-3f, 0f, 0.68f),
    //YA: Array("DL020 LR:0.003   PO:0        DROP:0.80", 20, 3e-3f, 0f, 0.80f)
    //MUCHO PEOR: Array("DL020 LR:0.0003  PO:0        DROP:0.50", 20, 3e-4f, 0f, 0.50f),
    //MUCHO PEOR: Array("DL020 LR:0.0003  PO:0        DROP:0.68", 20, 3e-4f, 0f, 0.68f),
    //MUCHO PEOR: Array("DL020 LR:0.0003  PO:0        DROP:0.80", 20, 3e-4f, 0f, 0.80f),
    //MUCHO PEOR: Array("DL020 LR:0.00003 PO:0        DROP:0.50", 20, 3e-5f, 0f, 0.50f),
    //MUCHO PEOR: Array("DL020 LR:0.00003 PO:0        DROP:0.68", 20, 3e-5f, 0f, 0.68f),
    //MUCHO PEOR: Array("DL020 LR:0.00003 PO:0        DROP:0.80", 20, 3e-5f, 0f, 0.80f)
    //BUENO: Array("DL020 LR:0.002   PO:0        DROP:0.50", 20, 2e-3F, 0f, 0.50f),
    //BUENO: Array("DL020 LR:0.002   PO:0        DROP:0.68", 20, 2e-3F, 0f, 0.68f),
    //BUENO: Array("DL020 LR:0.002   PO:0        DROP:0.80", 20, 2e-3F, 0f, 0.80f),
    //PEOR: Array("DL020 LR:0.001   PO:0        DROP:0.50", 20, 1e-3F, 0f, 0.50f),
    //PEOR: Array("DL020 LR:0.001   PO:0        DROP:0.68", 20, 1e-3F, 0f, 0.68f),
    //PEOR: Array("DL020 LR:0.001   PO:0        DROP:0.80", 20, 1e-3F, 0f, 0.80f),
    //MUCHO PEOR: Array("DL020 LR:0.01    PO:0        DROP:0.50", 20, 1e-2F, 0f, 0.50f),
    //MUCHO PEOR: Array("DL020 LR:0.01    PO:0        DROP:0.68", 20, 1e-2F, 0f, 0.68f),
    //MUCHO PEOR: Array("DL020 LR:0.01    PO:0        DROP:0.80", 20, 1e-2F, 0f, 0.80f),
    //MUCHO PEOR: Array("DL020 LR:0.05    PO:0        DROP:0.50", 20, 5e-2F, 0f, 0.50f),
    //MUCHO PEOR: Array("DL020 LR:0.05    PO:0        DROP:0.68", 20, 5e-2F, 0f, 0.68f),
    //MUCHO PEOR: Array("DL020 LR:0.05    PO:0        DROP:0.80", 20, 5e-2F, 0f, 0.80f)
    // DE 2 A 3
    //Array("DL020 LR:0.002   PO:-0.017   DROP:0.50", 20, 2e-3F, -0.017f, 0.50f),
    //Array("DL020 LR:0.002   PO:-0.017   DROP:0.68", 20, 2e-3F, -0.017f, 0.68f),
    //Array("DL020 LR:0.002   PO:-0.017   DROP:0.80", 20, 2e-3F, -0.017f, 0.80f),
    // DE 3 A 2. TIENE MAS SENTIDO
    //Array("DL020 LR:0.003   PO: 0.023   DROP:0.50", 20, 3e-3F,  0.023f, 0.50f),
    //Array("DL020 LR:0.003   PO: 0.023   DROP:0.68", 20, 3e-3F,  0.023f, 0.68f),
    //Array("DL020 LR:0.003   PO: 0.023   DROP:0.80", 20, 3e-3F,  0.023f, 0.80f),
    // DE 4 A 3. TIENE MAS SENTIDO
    //Array("DL020 LR:0.004   PO: 0.016   DROP:0.50", 20, 4e-3F,  0.016f, 0.50f),
    //Array("DL020 LR:0.004   PO: 0.016   DROP:0.68", 20, 4e-3F,  0.016f, 0.68f),
    //Array("DL020 LR:0.004   PO: 0.016   DROP:0.80", 20, 4e-3F,  0.016f, 0.80f)
    Array("DL080 LR:0.004   PO: 0.001   DROP:0.80", 80, 4e-3F,  0.001f, 0.80f)
)

val TRAINING_NER_DIR = "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/"
val TRAINING_TMP_DIR = "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/"

for (bertModel <- 0 to BERT_MODELS.length - 1) {
    //var bertModel = 9
    println(BERT_MODELS(bertModel)(0).toString + " ------------------------------------------------------------------")
    val embeddings = TfmHelper.prepareBert(
        BERT_DIR +
        BERT_MODELS(bertModel)(0).toString,
        BERT_MODELS(bertModel)(2).asInstanceOf[Int],
        BERT_MODELS(bertModel)(1).asInstanceOf[Int])

    for (conllModel <- 0 to CONLL_MODELS.length - 1) {
        //var conllModel = 4
        println(CONLL_MODELS(conllModel)(0).toString + "/" + CONLL_MODELS(conllModel)(1).toString + " -------------------")
        val nerReader = CoNLL(explodeSentences = false)
        val trainData = TfmHelper.prepareData(nerReader, TRAINING_NER_DIR + CONLL_MODELS(conllModel)(0).toString)
        val testData = TfmHelper.prepareData(nerReader,  TRAINING_NER_DIR + CONLL_MODELS(conllModel)(1).toString)
        val trainDataEmbeddings = embeddings.transform(trainData)
        val testDataEmbeddings =  embeddings.transform(testData)
        val testDatasetDirectory = TRAINING_TMP_DIR + CONLL_MODELS(conllModel)(1).toString + "-test.tmp.parquet"

        TfmHelper.saveParquetDataSet(testDataEmbeddings, testDatasetDirectory)

        for (nerDlModel <- 0 to NERDL_PARAMS.length - 1) {
            //var nerDlModel = 2
            println("DL  - " + NERDL_PARAMS(nerDlModel)(0).toString + " -----------------------------------------------------")
            val nerTaggerTEST = TfmHelper.prepareNerDL(
                TFGRAPH_DIR,
                NERDL_PARAMS(nerDlModel)(1).asInstanceOf[Int],
                NERDL_PARAMS(nerDlModel)(2).asInstanceOf[Float],
                NERDL_PARAMS(nerDlModel)(3).asInstanceOf[Float],
                NERDL_PARAMS(nerDlModel)(4).asInstanceOf[Float],
                testDatasetDirectory,
                0.20f,
                32)

            //println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN TRAIN")
            val nerTest = nerTaggerTEST.fit(trainDataEmbeddings)
            OutputHelper.writeAppend(nerTaggerTEST.toString, "TRAIN: " + CONLL_MODELS(conllModel)(0).toString)
            OutputHelper.writeAppend(nerTaggerTEST.toString, "TEST:  " + CONLL_MODELS(conllModel)(1).toString)
            OutputHelper.writeAppend(nerTaggerTEST.toString, "BERT:  " + BERT_MODELS(bertModel)(0).toString)
            OutputHelper.writeAppend(nerTaggerTEST.toString, "NER:   " + NERDL_PARAMS(nerDlModel)(0).toString)
            //println(java.time.LocalTime.now + ": NER-TRAIN: END   TRAIN")

            TfmHelper.saveModel(
                nerTest,
                NER_MODEL_DIR + "TFM-DL-" + nerTaggerTEST.toString)
        }
    }
}













import com.johnsnowlabs.util.ConfigHelper
import com.johnsnowlabs.nlp.util.io.{OutputHelper, ResourceHelper}
import org.apache.hadoop.fs.{FileSystem, Path}

val fs = FileSystem.get(ResourceHelper.spark.sparkContext.hadoopConfiguration)
val logsFolder: String = ConfigHelper.getConfigValueOrElse(ConfigHelper.annotatorLogFolder, fs.getHomeDirectory + "/annotator_logs")
val logsFolderExists = fs.exists(new Path(logsFolder))

println(ResourceHelper.spark.sparkContext.hadoopConfiguration)
val logsFolder: String = ConfigHelper.getConfigValueOrElse(ConfigHelper.annotatorLogFolder, fs.getHomeDirectory + "/annotator_logs")
val fs = ResourceHelper.spark.sparkContext.hadoopConfiguration
println(fs.getHomeDirectory)

OutputHelper.writeAppend("aver", "donde")











val NERCRF_PARAMS = Array(
    Array("CRF10_ma", 10, 20, 1f, 1e-3, 2250000, 0.0f),
    Array("CRF10_ma", 10, 50, 1f, 1e-3, 2250000, 0.0f),
    Array("CRF99_ma", 20,150, 1f, 1e-3, 2250000, 0.0f),
    Array("CRF10_mi", 10, 20, 1f, 1e-4, 2250000, 0.0f),
    Array("CRF10_mi", 10, 50, 1f, 1e-4, 2250000, 0.0f),
    Array("CRF99_mi", 20,150, 1f, 1e-4, 2250000, 0.0f)
)


var bertModel = 1
println(BERT_MODELS(bertModel)(0).toString + " -----------------------------------------------------------------")
val embeddings = TfmHelper.prepareBert(
    BERT_DIR +
    BERT_MODELS(bertModel)(0).toString,
    BERT_MODELS(bertModel)(2).asInstanceOf[Int],
    BERT_MODELS(bertModel)(1).asInstanceOf[Int])
val trainDataEmbeddings = embeddings.transform(trainData)
val testDataEmbeddings =  embeddings.transform(testData)
val testDatasetDirectory = TRAINING_TMP_DIR + "CONLL-test.tmp.parquet"

TfmHelper.saveParquetDataSet(testDataEmbeddings, testDatasetDirectory)

var nerDlModel = 9
println("DL  - " + NERDL_PARAMS(nerDlModel)(0).toString + " ------------------------------------------------------")
val nerTaggerTEST = TfmHelper.prepareNerDL(
    TFGRAPH_DIR,
    NERDL_PARAMS(nerDlModel)(1).asInstanceOf[Int],
    NERDL_PARAMS(nerDlModel)(2).asInstanceOf[Float],
    NERDL_PARAMS(nerDlModel)(3).asInstanceOf[Float],
    testDatasetDirectory,
    0.20f,
    32)

//println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN TRAIN")
val nerTest = nerTaggerTEST.fit(trainDataEmbeddings)
//println(java.time.LocalTime.now + ": NER-TRAIN: END   TRAIN")


// -----------------------------------------------------------------------------------------
val trainData = TfmHelper.prepareData(nerReader, TRAINING_NER_DIR + "ner_txt_train.conll")
val testData =  TfmHelper.prepareData(nerReader, TRAINING_NER_DIR + "ner_txt_test.conll")


// -----------------------------------------------------------------------------------------
var bertModel = 9
println(BERT_MODELS(bertModel)(0).toString + " -----------------------------------------------------------------")
val embeddings = TfmHelper.prepareBert(
    BERT_DIR +
    BERT_MODELS(bertModel)(0).toString,
    BERT_MODELS(bertModel)(2).asInstanceOf[Int],
    BERT_MODELS(bertModel)(1).asInstanceOf[Int])
val trainDataEmbeddings = embeddings.transform(trainData)
val testDataEmbeddings =  embeddings.transform(testData)
val testDatasetDirectory = TRAINING_TMP_DIR + "CONLL-eng.tmp.parquet"
TfmHelper.saveParquetDataSet(testDataEmbeddings, testDatasetDirectory)


// -----------------------------------------------------------------------------------------
var nerDlModel = 9
println("DL  - " + NERDL_PARAMS(nerDlModel)(0).toString + " ------------------------------------------------------")
val nerTaggerDL = TfmHelper.prepareNerDL(
    TFGRAPH_DIR,
    NERDL_PARAMS(nerDlModel)(1).asInstanceOf[Int],
    NERDL_PARAMS(nerDlModel)(2).asInstanceOf[Float],
    NERDL_PARAMS(nerDlModel)(3).asInstanceOf[Float],
    testDatasetDirectory,
    0.20f,
    32)

//println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN TRAIN")
val nerDL = nerTaggerDL.fit(trainDataEmbeddings)
//println(java.time.LocalTime.now + ": NER-TRAIN: END   TRAIN")

// -----------------------------------------------------------------------------------------
val trainDataOk = trainDataEmbeddings.
    filter(r => r(3).asInstanceOf[WrappedArray[String]].length == r(6).asInstanceOf[WrappedArray[String]].length)
val testDataOk = testDataEmbeddings.
    filter(r => r(3).asInstanceOf[WrappedArray[String]].length == r(6).asInstanceOf[WrappedArray[String]].length)

var nerCrfModel = 3
println("CRF - " + NERCRF_PARAMS(nerCrfModel)(0).toString + " --------------------------------------------------------")
val nerTaggerCRF = TfmHelper.prepareNerCrfApproach(
    400,
    500,
    NERCRF_PARAMS(nerCrfModel)(3).asInstanceOf[Float],
    NERCRF_PARAMS(nerCrfModel)(4).asInstanceOf[Double],
    NERCRF_PARAMSB-.*time.LocalTime.now + ": NER-TRAIN: BEGIN TRAIN")
val nerCRF = nerTaggerCRF.fit(trainDataOk)
//println(java.time.LocalTime.now + ": NER-TRAIN: END   TRAIN")

TfmHelper.measureCRF(nerCRF, trainDataOk)
TfmHelper.measureCRF(nerCRF, testDataOk)

// -----------------------------------------------------------------------------------------
val trainDataCoNLL = TfmHelper.prepareData(nerReader, "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/JNLPBA/Genia4ERtask2.conll")
val testDataCoNLL = TfmHelper.prepareData(nerReader, "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/JNLPBA/Genia4EReval2.conll")
//val trainData = TfmHelper.prepareData(nerReader, "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/JNLPBA/sampletest2.conll")

val pos = TfmHelper.preparePos("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/pos/pos_anc_en_2.0.2_2.4_1556659930154")
val trainData = pos.transform(trainDataCoNLL.withColumnRenamed("pos", "old_pos").select("text", "document", "sentence", "token", "label", "old_pos"))
val testData = pos.transform(testDataCoNLL.withColumnRenamed("pos", "old_pos").select("text", "document", "sentence", "token", "label", "old_pos"))

val trainData = pos.transform(trainDataCoNLL.withColumnRenamed("pos", "old_pos").select("text", "document", "sentence", "token", "label", "old_pos"))
val testData = pos.transform(testDataCoNLL.withColumnRenamed("pos", "old_pos").select("text", "document", "sentence", "token", "label", "old_pos"))

TfmHelper.saveDsToCsvInDatabricks(ds = trainData.select("token", "pos", "old_pos", "label").coalesce(1).toDF(), sep = " ", targetFile = TRAINING_NER_DIR + "JNLPBA_train.conll")
TfmHelper.saveDsToCsvInDatabricks(ds = testData.select("token", "pos", "old_pos", "label").coalesce(1).toDF(), sep = " ",  targetFile = TRAINING_NER_DIR + "JNLPBA_test.conll")

import scala.collection.mutable.ArrayBuffer
val finisher = new Finisher().
      setInputCols(Array(
          "token", "pos", "label")).
      setIncludeMetadata(true).
      setCleanAnnotations(false)

val b = finisher.
    transform(testData.select("token", "pos", "label")).
    select("finished_token", "finished_pos", "finished_label", "finished_token_metadata").
    as[(Array[String], Array[String], Array[String], Array[(String, String)])].
    flatMap(row => {
        val newColumns: ArrayBuffer[(String, String, String, String)] = ArrayBuffer()
        val columns = (row._1 zip row._2 zip row._3 zip row._4.map(_._2.toInt)).map{case (((a,b), c), d) => (a, b, c, d)}
        var sentenceId = 0
        newColumns.append(("", "", "", ""))
        newColumns.append(("-DOCSTART-", "-X-", "-X-", "O"))
        newColumns.append(("", "", "", ""))
        columns.foreach(a => {
        if (a._4 != sentenceId){
            newColumns.append(("", "", "", ""))
            sentenceId = a._4
        }
        newColumns.append((a._1, a._2, a._2, a._3))
        })
        newColumns
    })
TfmHelper.saveDsToCsvInDatabricks(ds = b.coalesce(1).toDF(), sep = " ", targetFile = TRAINING_NER_DIR + "JNLPBA_test.conll")


var bertModel = 9
println(BERT_MODELS(bertModel)(0).toString + " -----------------------------------------------------------------")
val embeddings = TfmHelper.prepareBert(
    BERT_DIR +
    BERT_MODELS(bertModel)(0).toString,
    BERT_MODELS(bertModel)(2).asInstanceOf[Int],
    BERT_MODELS(bertModel)(1).asInstanceOf[Int])
val trainDataEmbeddings = embeddings.transform(trainData)
val testDataEmbeddings =  embeddings.transform(testData)
val testDatasetDirectory = TRAINING_TMP_DIR + "CONLL-test.tmp.parquet"

TfmHelper.saveParquetDataSet(testDataEmbeddings, testDatasetDirectory)

var nerDlModel = 9
println("DL  - " + NERDL_PARAMS(nerDlModel)(0).toString + " ------------------------------------------------------")
val nerTaggerTEST = TfmHelper.prepareNerDL(
    TFGRAPH_DIR,
    NERDL_PARAMS(nerDlModel)(1).asInstanceOf[Int],
    NERDL_PARAMS(nerDlModel)(2).asInstanceOf[Float],
    NERDL_PARAMS(nerDlModel)(3).asInstanceOf[Float],
    testDatasetDirectory,
    0.20f,
    32)

//println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN TRAIN")
val nerTest = nerTaggerTEST.fit(trainDataEmbeddings)
//println(java.time.LocalTime.now + ": NER-TRAIN: END   TRAIN")
















TfmHelper.saveModel(
    ner,
    NER_MODEL_DIR + "TFM-BIODL-" + NERDL_PARAMS(nerModel)(0).toString + "-" + BERT_MODELS(bertModel)(0).toString)

val trainDataEmbeddingsDiscrepances = trainDataEmbeddings.
    //select(TfmType.WORD_EMBEDDINGS, TfmType.TOKEN, TfmType.TEXT).
    filter(r => r.getAs[WrappedArray[String]](TfmType.WORD_EMBEDDINGS).length != 
                r.getAs[WrappedArray[String]](TfmType.TOKEN).length).
    map(r => Array(
                r.getAs[WrappedArray[String]](TfmType.WORD_EMBEDDINGS).length, 
                r.getAs[WrappedArray[String]](TfmType.TOKEN).length))
trainDataEmbeddingsDiscrepances.show()

val t = Annotation.collect(trainDataEmbeddings, TfmType.TOKEN)
   
val model = TfmHelper.prepareMeasureNerPipeline(ner, testDataEmbeddings)
val result = model.transform(testDataEmbeddings)

val labeledData = NerTagged.
    collectTrainingInstances(
        result,
        Seq(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS),
        TfmType.LABEL)

ner.getModelIfNotSet.measure(labeledData, (s: String) => System.out.println(s), true, 0)



result.
  select(
    $"token",
    $"named_entity",
    $"label",
    $"token.result".alias("predictedTokens"),
    $"named_entity.result".alias("predictedTags")).
  show(1)

result.
  select(
    $"text",
    $"document",
    $"sentence",
    $"token",
    $"pos",
    $"word_embeddings",
    $"ner",
    $"label",
    $"token.result".alias("predictedTokens"),
    $"ner.result".alias("predictedTags")
