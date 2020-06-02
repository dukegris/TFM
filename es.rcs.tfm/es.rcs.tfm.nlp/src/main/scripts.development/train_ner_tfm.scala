// spark-shell --packages com.johnsnowlabs.nlp:spark-nlp_2.11:2.4.3 --executor-memory=8g --executor-cores=24 --driver-memory=8g
// spark-shell --packages com.johnsnowlabs.nlp:spark-nlp_2.11:2.4.3 --executor-memory=16g --executor-cores=6 --driver-memory=16g
// spark-shell --packages JohnSnowLabs:spark-nlp_2.11:2.4.3 --jars --executor-memory=8g --executor-cores=24 --driver-memory=8g > crf.txt
// spark-shell --packages com.johnsnowlabs.nlp:spark-nlp_2.11:2.4.3,org.slf4j:slf4j-log4j12:1.7.28 --jars es.rcs.tfm.nlp/target/RCS-Nlp-0.0.4-SNAPSHOT.jar --executor-memory=32g --executor-cores=6 --driver-memory=24g --conf "spark.executor.extraJavaOptions='-Dlog4j.configuration=/opt/spark/conf/log4j.properties'" --conf "spark.driver.maxResultSize=8g" > dl.txt
// scp -r . rcuesta@10.160.1.215:D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models
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

val BERT_DIR = "file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/"
val BERT_MODELS = Array(
	Array("bert_base_cased_en_2.4.0_2.4_1580579557778", 768, 128),
	Array("bert_base_uncased_en_2.4.0_2.4_1580579889322", 768, 128),
	Array("bert_large_cased_en_2.4.0_2.4_1580580251298", 1024, 128),
	Array("bert_large_uncased_en_2.4.0_2.4_1580581306683", 1024, 128)

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
	Array("eng.train_testa.conll",     "eng.testb.conll"),
	Array("eng.train_testb.conll",     "eng.testa.conll"),
	Array("ner_txt_cdr_devel.conll",   "ner_txt_chem_test.conll"),
	Array("ner_txt_cdr_train.conll",   "ner_txt_cdr_test.conll"),
	Array("ner_txt_dnorm_train.conll", "ner_txt_dnorm_test.conll"),
	Array("ner_txt_chem_train.conll",  "ner_txt_chem_test.conll"),
	Array("JNLPBA_train.conll",        "JNLPBA_test.conll"),
	Array("ner_txt_train.conll",       "ner_txt_test.conll")
)

val TFGRAPH_DIR = "D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/tensorflow"
val NER_MODEL_DIR = "file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/"
val NERDL_PARAMS = Array(
	//FUERA POR DROP:
	Array("DL020__LR_0_003____PO_0_______DROP_0_10", 20, 3e-3f, 0f, 0.10f),
	//YA:
	Array("DL020__LR_0_003____PO_0_______DROP_0_50", 20, 3e-3f, 0f, 0.50f),
	//YA:
	Array("DL020__LR_0_003____PO_0_______DROP_0_68", 20, 3e-3f, 0f, 0.68f),
	//YA:
	Array("DL020__LR_0_003____PO_0_______DROP_0_80", 20, 3e-3f, 0f, 0.80f),
	//MUCHO PEOR:
	Array("DL020__LR_0_0003___PO_0_______DROP_0_50", 20, 3e-4f, 0f, 0.50f),
	//MUCHO PEOR:
	Array("DL020__LR_0_0003___PO_0_______DROP_0_68", 20, 3e-4f, 0f, 0.68f),
	//MUCHO PEOR:
	Array("DL020__LR_0_0003___PO_0_______DROP_0_80", 20, 3e-4f, 0f, 0.80f),
	//MUCHO PEOR:
	Array("DL020__LR_0_00003__PO_0_______DROP_0_50", 20, 3e-5f, 0f, 0.50f),
	//MUCHO PEOR:
	Array("DL020__LR_0_00003__PO_0_______DROP_0_68", 20, 3e-5f, 0f, 0.68f),
	//MUCHO PEOR:
	Array("DL020__LR_0_00003__PO_0_______DROP_0_80", 20, 3e-5f, 0f, 0.80f),
	//BUENO:
	Array("DL020__LR_0_002____PO_0_______DROP_0_50", 20, 2e-3F, 0f, 0.50f),
	//BUENO:
	Array("DL020__LR_0_002____PO_0_______DROP_0_68", 20, 2e-3F, 0f, 0.68f),
	//BUENO:
	Array("DL020__LR_0_002____PO_0_______DROP_0_80", 20, 2e-3F, 0f, 0.80f),
	//PEOR:
	Array("DL020__LR_0_001____PO_0_______DROP_0_50", 20, 1e-3F, 0f, 0.50f),
	//PEOR:
	Array("DL020__LR_0_001____PO_0_______DROP_0_68", 20, 1e-3F, 0f, 0.68f),
	//PEOR:
	Array("DL020__LR_0_001____PO_0_______DROP_0_80", 20, 1e-3F, 0f, 0.80f),
	//MUCHO PEOR:
	Array("DL020__LR_0_01_____PO_0_______DROP_0_50", 20, 1e-2F, 0f, 0.50f),
	//MUCHO PEOR:
	Array("DL020__LR_0_01_____PO_0_______DROP_0_68", 20, 1e-2F, 0f, 0.68f),
	//MUCHO PEOR:
	Array("DL020__LR_0_01_____PO_0_______DROP_0_80", 20, 1e-2F, 0f, 0.80f),
	//MUCHO PEOR:
	Array("DL020__LR_0_05_____PO_0_______DROP_0_50", 20, 5e-2F, 0f, 0.50f),
	//MUCHO PEOR:
	Array("DL020__LR_0_05_____PO_0_______DROP_0_68", 20, 5e-2F, 0f, 0.68f),
	//MUCHO PEOR:
	Array("DL020__LR_0_05_____PO_0_______DROP_0_80", 20, 5e-2F, 0f, 0.80f),
	// DE 2 A 3
	Array("DL020__LR_0_002____PO_-0.017__DROP_0_50", 20, 2e-3F, -0.017f, 0.50f),
	Array("DL020__LR_0_002____PO_-0.017__DROP_0_68", 20, 2e-3F, -0.017f, 0.68f),
	Array("DL020__LR_0_002____PO_-0.017__DROP_0_80", 20, 2e-3F, -0.017f, 0.80f),
	// DE 3 A 2. TIENE MAS SENTIDO
	Array("DL020__LR_0_003____PO_ 0.023__DROP_0_50", 20, 3e-3F,  0.023f, 0.50f),
	Array("DL020__LR_0_003____PO_ 0.023__DROP_0_68", 20, 3e-3F,  0.023f, 0.68f),
	Array("DL020__LR_0_003____PO_ 0.023__DROP_0_80", 20, 3e-3F,  0.023f, 0.80f),
	// DE 4 A 3. TIENE MAS SENTIDO
	Array("DL020__LR_0_004____PO_ 0.016__DROP_0_50", 20, 4e-3F,  0.016f, 0.50f),
	Array("DL020__LR_0_004____PO_ 0.016__DROP_0_68", 20, 4e-3F,  0.016f, 0.68f),
	Array("DL020__LR_0_004____PO_ 0.016__DROP_0_80", 20, 4e-3F,  0.016f, 0.80f),
	Array("DL080__LR_0_004____PO_ 0.001__DROP_0_80", 80, 4e-3F,  0.001f, 0.80f)
)

val TRAINING_NER_DIR = "D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/"
val TRAINING_TMP_DIR = "D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/"

val nerReader = CoNLL(explodeSentences = false)

//for (bertModel <- 0 to BERT_MODELS.length - 1) {
var bertModel = 1
	println(BERT_MODELS(bertModel)(0).toString + " ------------------------------------------------------------------")
	val embeddings = TfmHelper.prepareBert(
		BERT_DIR +
		BERT_MODELS(bertModel)(0).toString,
		BERT_MODELS(bertModel)(2).asInstanceOf[Int],
		BERT_MODELS(bertModel)(1).asInstanceOf[Int])

	//for (conllModel <- 0 to CONLL_MODELS.length - 1) {
	var conllModel = 8
		println(CONLL_MODELS(conllModel)(0).toString + "/" + CONLL_MODELS(conllModel)(1).toString + " -------------------")
		val trainData = TfmHelper.prepareData(nerReader, TRAINING_NER_DIR + CONLL_MODELS(conllModel)(0).toString)
		val trainDataEmbeddings = embeddings.transform(trainData)

		val testData = TfmHelper.prepareData(nerReader,  TRAINING_NER_DIR + CONLL_MODELS(conllModel)(1).toString)
		val testDataEmbeddings =  embeddings.transform(testData)
		val testDatasetDirectory = TRAINING_TMP_DIR + CONLL_MODELS(conllModel)(1).toString + "-test.tmp.parquet"
		TfmHelper.saveParquetDataSet(testDataEmbeddings, testDatasetDirectory)

		//for (nerDlModel <- 0 to NERDL_PARAMS.length - 1) {
		var nerDlModel = 2
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

			println(java.time.LocalTime.now + ": NER-TRAIN: BEGIN TRAIN")
			val nerTest = nerTaggerTEST.fit(trainDataEmbeddings)
			OutputHelper.writeAppend(nerTaggerTEST.toString, "TRAIN: " + CONLL_MODELS(conllModel)(0).toString)
			OutputHelper.writeAppend(nerTaggerTEST.toString, "TEST:  " + CONLL_MODELS(conllModel)(1).toString)
			OutputHelper.writeAppend(nerTaggerTEST.toString, "BERT:  " + BERT_MODELS(bertModel)(0).toString)
			OutputHelper.writeAppend(nerTaggerTEST.toString, "NER:   " + NERDL_PARAMS(nerDlModel)(0).toString)
			println(java.time.LocalTime.now + ": NER-TRAIN: END   TRAIN")

			TfmHelper.saveModel(
				nerTest,
				NER_MODEL_DIR + "TFM-DL-" + nerTaggerTEST.toString + "-" + CONLL_MODELS(conllModel)(0).toString + "-" + NERDL_PARAMS(nerDlModel)(0).toString)
		}
	}
}
