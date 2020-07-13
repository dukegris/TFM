// spark-shell --packages com.johnsnowlabs.nlp:spark-nlp_2.11:2.5.1 --executor-memory=8g --executor-cores=24 --driver-memory=8g
// spark-shell --packages com.johnsnowlabs.nlp:spark-nlp_2.11:2.5.1 --executor-memory=16g --executor-cores=6 --driver-memory=16g
// spark-shell --packages JohnSnowLabs:spark-nlp_2.11:2.5.1 --jars --executor-memory=8g --executor-cores=24 --driver-memory=8g > crf.txt
// spark-shell --packages com.johnsnowlabs.nlp:spark-nlp_2.11:2.5.1,org.slf4j:slf4j-log4j12:1.7.28 --jars es.rcs.tfm.nlp/target/RCS-Nlp-0.0.4-SNAPSHOT.jar --executor-memory=32g --executor-cores=6 --driver-memory=24g --conf "spark.executor.extraJavaOptions='-Dlog4j.configuration=/opt/spark/conf/log4j.properties'" --conf "spark.driver.maxResultSize=8g" > dl.txt
// scp -r . rcuesta@10.160.1.215:/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models

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
import com.johnsnowlabs.nlp.annotators.ner.{NerConverter, NerApproach, Verbose}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach}
import com.johnsnowlabs.nlp.annotators.ner.crf.{NerCrfModel, NerCrfApproach}
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.embeddings.{BertEmbeddings, AlbertEmbeddings, WordEmbeddingsModel}
import com.johnsnowlabs.nlp.embeddings.{WordEmbeddingsFormat}
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import com.johnsnowlabs.nlp.training.CoNLL
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ResourceHelper, OutputHelper, ReadAs}
import com.johnsnowlabs.util.ConfigHelper

import es.rcs.tfm.nlp.model.TfmType
import es.rcs.tfm.nlp.util.{TfmHelper, TfmSave}

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row, Dataset}
import org.apache.spark.ml.PipelineModel

import scala.Any
import scala.collection.mutable
import scala.collection.mutable.WrappedArray
import spark.implicits._

val CORPUS_DIR = "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus"
val BERT_DIR = "file://" + CORPUS_DIR + "/models/bert/"
val TFGRAPH_DIR = CORPUS_DIR + "/models/tensorflow"
val NER_MODEL_DIR = "file://" + CORPUS_DIR + "/models/ner/"
val TRAINING_CONLL_DIR = CORPUS_DIR + "/training/conll/"
val TRAINING_TMP_DIR = CORPUS_DIR + "/training/tmp/"
val LOGS: String = ConfigHelper.getConfigValueOrElse(ConfigHelper.annotatorLogFolder, "/home/rcuesta/annotator_logs")

val BERT_MODELS = Array(
//    Array("bert_base_cased_en_2.4.0_2.4_1580579557778", 768, 128),
//    Array("bert_base_uncased_en_2.4.0_2.4_1580579889322", 768, 128),
    Array("bert_large_cased_en_2.4.0_2.4_1580580251298", 1024, 128),
    Array("bert_large_uncased_en_2.4.0_2.4_1580581306683", 1024, 128),
    Array("bert_multi_cased_xx_2.4.0_2.4_1580582335793", 768, 128),

    Array("biobert_pmc_base_cased_en_2.5.0_2.4_1590489029151", 768, 128),
    Array("biobert_pubmed_pmc_base_cased_en_2.5.0_2.4_1590489367180", 768, 128),
    Array("biobert_pubmed_base_cased_en_2.5.0_2.4_1590487367971", 768, 128),
    Array("biobert_pubmed_large_cased_en_2.5.0_2.4_1590487739645", 768, 128),
    Array("biobert_discharge_base_cased_en_2.5.0_2.4_1590490193605", 768, 128),
    Array("biobert_clinical_base_cased_en_2.5.0_2.4_1590489819943", 768, 128)

//	Array("albert_base_uncased_en_2.5.0_2.4_1588073363475", 768, 128),
//	Array("albert_large_uncased_en_2.5.0_2.4_1588073397355", , 128),
//	Array("albert_xlarge_uncased_en_2.5.0_2.4_1588073443653", 1024, 128),
//	Array("albert_xxlarge_uncased_en_2.5.0_2.4_1588073443653", 1024, 128)

//    Array("biobert_v1.1_pubmed_M-128_B-32", 768, 128),
//    Array("biobert_v1.1_pubmed_M-512_B-32", 768, 512),

//    Array("cased_L-12_H-768_A-12_M-128_B-32", 768, 128),
//    Array("cased_L-12_H-768_A-12_M-512_B-32", 768, 512),
//    Array("uncased_L-12_H-768_A-12_M-128_B-32", 768, 128),
//    Array("uncased_L-12_H-768_A-12_M-512_B-32", 768, 512),
//    Array("multi_cased_L-12_H-768_A-12_M-128_B-32", 768, 128),
//    Array("multi_cased_L-12_H-768_A-12_M-512_B-32", 768, 512),

//    Array("wwm_cased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
//    Array("wwm_cased_L-24_H-1024_A-16_M-512_B-32", 1024, 512),
//    Array("wwm_uncased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
//    Array("wwm_uncased_L-24_H-1024_A-16_M-512_B-32", 1024, 512),

//    Array("cased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
//    Array("cased_L-24_H-1024_A-16_M-512_B-32", 1024, 512),
//    Array("uncased_L-24_H-1024_A-16_M-128_B-32", 1024, 128),
//    Array("uncased_L-24_H-1024_A-16_M-512_B-32", 1024, 512)
)

val CONLL_MODELS = Array(
//    Array("eng.train_testa.conll",     "eng.testb.conll"),
//    Array("eng.train_testb.conll",     "eng.testa.conll"),
//    Array("ner_txt_cdr_devel.conll",   "ner_txt_chem_test.conll"),
//    Array("ner_txt_cdr_train.conll",   "ner_txt_cdr_test.conll"),
//    Array("ner_txt_dnorm_train.conll", "ner_txt_dnorm_test.conll"),
//    Array("ner_txt_chem_train.conll",  "ner_txt_chem_test.conll"),
//    Array("JNLPBA_train.conll",        "JNLPBA_test.conll"),
    Array("ner_txt_train.conll",       "ner_txt_test.conll")
)

val NERDL_PARAMS = Array(
    
    Array("DL200__LR_0_001____PO_0_______DROP_0_10", 200, 1e-3f, 0f, 0.10f),
    Array("DL200__LR_0_001____PO_0_______DROP_0_50", 200, 1e-3f, 0f, 0.50f),
    Array("DL200__LR_0_001____PO_0_______DROP_0_68", 200, 1e-3f, 0f, 0.68f),
    Array("DL200__LR_0_001____PO_0_______DROP_0_80", 200, 1e-3f, 0f, 0.80f),

    Array("DL200__LR_0_0001___PO_0_______DROP_0_10", 200, 1e-4f, 0f, 0.10f),
    Array("DL200__LR_0_0001___PO_0_______DROP_0_50", 200, 1e-4f, 0f, 0.50f),
    Array("DL200__LR_0_0001___PO_0_______DROP_0_68", 200, 1e-4f, 0f, 0.68f),
    Array("DL200__LR_0_0001___PO_0_______DROP_0_80", 200, 1e-4f, 0f, 0.80f),

    Array("DL200__LR_0_00001__PO_0_______DROP_0_10", 200, 1e-5f, 0f, 0.10f),
    Array("DL200__LR_0_00001__PO_0_______DROP_0_50", 200, 1e-5f, 0f, 0.50f),
    Array("DL200__LR_0_00001__PO_0_______DROP_0_68", 200, 1e-5f, 0f, 0.68f),
    Array("DL200__LR_0_00001__PO_0_______DROP_0_80", 200, 1e-5f, 0f, 0.80f),

    Array("DL200__LR_0_002____PO_0_______DROP_0_10", 200, 2e-3f, 0f, 0.10f),
    Array("DL200__LR_0_002____PO_0_______DROP_0_50", 200, 2e-3f, 0f, 0.50f),
    Array("DL200__LR_0_002____PO_0_______DROP_0_68", 200, 2e-3f, 0f, 0.68f),
    Array("DL200__LR_0_002____PO_0_______DROP_0_80", 200, 2e-3f, 0f, 0.80f),

    Array("DL200__LR_0_0002___PO_0_______DROP_0_10", 200, 2e-4f, 0f, 0.10f),
    Array("DL200__LR_0_0002___PO_0_______DROP_0_50", 200, 2e-4f, 0f, 0.50f),
    Array("DL200__LR_0_0002___PO_0_______DROP_0_68", 200, 2e-4f, 0f, 0.68f),
    Array("DL200__LR_0_0002___PO_0_______DROP_0_80", 200, 2e-4f, 0f, 0.80f),

    Array("DL200__LR_0_00002__PO_0_______DROP_0_10", 200, 2e-5f, 0f, 0.10f),
    Array("DL200__LR_0_00002__PO_0_______DROP_0_50", 200, 2e-5f, 0f, 0.50f),
    Array("DL200__LR_0_00002__PO_0_______DROP_0_68", 200, 2e-5f, 0f, 0.68f),
    Array("DL200__LR_0_00002__PO_0_______DROP_0_80", 200, 2e-5f, 0f, 0.80f),

    Array("DL200__LR_0_003____PO_0_______DROP_0_10", 200, 3e-3f, 0f, 0.10f),
    Array("DL200__LR_0_003____PO_0_______DROP_0_50", 200, 3e-3f, 0f, 0.50f),
    Array("DL200__LR_0_003____PO_0_______DROP_0_68", 200, 3e-3f, 0f, 0.68f),
    Array("DL200__LR_0_003____PO_0_______DROP_0_80", 200, 3e-3f, 0f, 0.80f),

    Array("DL200__LR_0_0003___PO_0_______DROP_0_10", 200, 3e-4f, 0f, 0.10f),
    Array("DL200__LR_0_0003___PO_0_______DROP_0_50", 200, 3e-4f, 0f, 0.50f),
    Array("DL200__LR_0_0003___PO_0_______DROP_0_68", 200, 3e-4f, 0f, 0.68f),
    Array("DL200__LR_0_0003___PO_0_______DROP_0_80", 200, 3e-4f, 0f, 0.80f),

    Array("DL200__LR_0_00003__PO_0_______DROP_0_10", 200, 3e-5f, 0f, 0.10f),
    Array("DL200__LR_0_00003__PO_0_______DROP_0_50", 200, 3e-5f, 0f, 0.50f),
    Array("DL200__LR_0_00003__PO_0_______DROP_0_68", 200, 3e-5f, 0f, 0.68f),
    Array("DL200__LR_0_00003__PO_0_______DROP_0_80", 200, 3e-5f, 0f, 0.80f),

    // DE 3 A 2. TIENE MAS SENTIDO
    Array("DL200__LR_0_0003___PO_0.0023__DROP_0_10", 200, 3e-4f, 0.0023f, 0.10f),
    // DE 2 A 1. TIENE MAS SENTIDO
    Array("DL200__LR_0_0002___PO_0.0016__DROP_0_10", 200, 2e-4f, 0.0016f, 0.10f)

)

val nerReader = CoNLL(explodeSentences = false)

//for (bertModel <- 0 to BERT_MODELS.length - 1) {
for (bertModel <- 0 to BERT_MODELS.length - 1) {
//    var bertModel = 1
    println(BERT_MODELS(bertModel)(0).toString + " ------------------------------------------------------------------")
    var embeddings = TfmHelper.prepareBert(
        BERT_DIR +
        BERT_MODELS(bertModel)(0).toString,
        BERT_MODELS(bertModel)(2).asInstanceOf[Int],
        BERT_MODELS(bertModel)(1).asInstanceOf[Int])
    /*
    if (BERT_MODELS(bertModel)(0).toString.startsWith("bert")) {
      embeddings = TfmHelper.prepareBert(
          BERT_DIR +
          BERT_MODELS(bertModel)(0).toString,
          BERT_MODELS(bertModel)(2).asInstanceOf[Int],
          BERT_MODELS(bertModel)(1).asInstanceOf[Int])
    } else {
      embeddings = TfmHelper.prepareAlbert(
          BERT_DIR +
          BERT_MODELS(bertModel)(0).toString,
          BERT_MODELS(bertModel)(2).asInstanceOf[Int],
          BERT_MODELS(bertModel)(1).asInstanceOf[Int])
    }
    */

    for (conllModel <- CONLL_MODELS.length - 1 to 0 by -1) {
//        var conllModel = 7
        println(CONLL_MODELS(conllModel)(0).toString + "/" + CONLL_MODELS(conllModel)(1).toString + " -------------------")
        val trainData = TfmHelper.prepareData(nerReader, TRAINING_CONLL_DIR + CONLL_MODELS(conllModel)(0).toString)
        val trainDataEmbeddings = embeddings.transform(trainData)

        val testData = TfmHelper.prepareData(nerReader,  TRAINING_CONLL_DIR + CONLL_MODELS(conllModel)(1).toString)
        val testDataEmbeddings =  embeddings.transform(testData)
        val testDatasetDirectory = TRAINING_TMP_DIR + CONLL_MODELS(conllModel)(1).toString + "-test.tmp.parquet"
        TfmSave.saveParquetDataSet(testDataEmbeddings, testDatasetDirectory)

        for (nerDlModel <- 0 to NERDL_PARAMS.length - 1) {
//            var nerDlModel = 2
            println("DL  - " + NERDL_PARAMS(nerDlModel)(0).toString + " -----------------------------------------------------")
            val nerTaggerTEST = new NerDLApproach().
                setTestDataset(testDatasetDirectory).
                setGraphFolder(TFGRAPH_DIR).
                setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS)).
                setOutputCol(TfmType.NAMED_ENTITY).
                setLabelColumn(TfmType.LABEL).
                setEnableOutputLogs(true).
                setIncludeConfidence(false).
                setEvaluationLogExtended(true).
                setMinEpochs(20).
                setMaxEpochs(NERDL_PARAMS(nerDlModel)(1).asInstanceOf[Int]).
                setLr(NERDL_PARAMS(nerDlModel)(2).asInstanceOf[Float]).
                setPo(NERDL_PARAMS(nerDlModel)(3).asInstanceOf[Float]).
                setDropout(NERDL_PARAMS(nerDlModel)(4).asInstanceOf[Float]).
                setValidationSplit(0.20f).
                setBatchSize(32).
                setVerbose(Verbose.PerStep)

            println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN TRAIN")
            val nerTest = nerTaggerTEST.fit(trainDataEmbeddings)
            OutputHelper.writeAppend(nerTaggerTEST.toString, "TRAIN: " + CONLL_MODELS(conllModel)(0).toString, LOGS)
            OutputHelper.writeAppend(nerTaggerTEST.toString, "TEST:  " + CONLL_MODELS(conllModel)(1).toString, LOGS)
            OutputHelper.writeAppend(nerTaggerTEST.toString, "BERT:  " + BERT_MODELS(bertModel)(0).toString, LOGS)
            OutputHelper.writeAppend(nerTaggerTEST.toString, "NER:   " + NERDL_PARAMS(nerDlModel)(0).toString, LOGS)
            println(java.time.LocalTime.now + ": NER-TRAIN: END   TRAIN")

            TfmSave.saveModel(
                nerTest,
                NER_MODEL_DIR + "TFM-DL-" + nerTaggerTEST.toString + "-" + CONLL_MODELS(conllModel)(0).toString + "-" + NERDL_PARAMS(nerDlModel)(0).toString)
        }
    }
}
