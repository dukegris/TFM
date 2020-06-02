// spark-shell --packages com.johnsnowlabs.nlp:spark-nlp_2.11:2.5.1,org.slf4j:slf4j-log4j12:1.7.28 --jars es.rcs.tfm.nlp/target/RCS-Nlp-0.0.4-SNAPSHOT.jar --executor-memory=32g --executor-cores=6 --driver-memory=24g --conf "spark.executor.extraJavaOptions='-Dlog4j.configuration=/opt/spark/conf/log4j.properties'" --conf "spark.driver.maxResultSize=8g" > dl_bert_conll_eng.log
// scp -r . rcuesta@10.160.1.215:/" + CORPUS_DIR + "/models
/*

Epoch\s+(\d+)/(?:\d+)\s+started,\s+lr:\s+([\d\.]*)(?:.*(?:\r\n)*)+?
Epoch\s+(?:\d+)/(?:\d+)\s+-\s+([\d\.,]*)s\s+-\s+loss:\s+([\d\.]*)\s+-\s+batches:\s+([\d\.]*)(?:.*(?:\r\n)*)+?
Quality on validation(?:.*(?:\r\n)*)+?
tp:\s+(\d+)\s+fp:\s+(\d+)\s+fn:\s+(\d+).*\r\n
Macro-average\s+prec:\s+([\d\.]*),\s+rec:\s+([\d\.]*),\s+f1:\s+([\d\.]*)(?:.*(?:\r\n)*)+?
Micro-average\s+prec:\s+([\d\.]*),\s+rec:\s+([\d\.]*),\s+f1:\s+([\d\.]*)(?:.*(?:\r\n)*)+?
Quality on test(?:.*(?:\r\n)*)+?
tp:\s+(\d+)\s+fp:\s+(\d+)\s+fn:\s+(\d+).*\r\n
Macro-average\s+prec:\s+([\d\.]*),\s+rec:\s+([\d\.]*),\s+f1:\s+([\d\.]*)(?:.*(?:\r\n)*)+?
Micro-average\s+prec:\s+([\d\.]*),\s+rec:\s+([\d\.]*),\s+f1:\s+([\d\.]*)


Epoch\s+(\d+)/(?:\d+)\s+started,\s+lr:\s+([\d\.]*)(?:.*(?:\r\n)*)+?Epoch\s+(?:\d+)/(?:\d+)\s+-\s+([\d\.,]*)s\s+-\s+loss:\s+([\d\.]*)\s+-\s+batches:\s+([\d\.]*)(?:.*(?:\r\n)*)+?Quality on validation(?:.*(?:\r\n)*)+?tp:\s+(\d+)\s+fp:\s+(\d+)\s+fn:\s+(\d+).*\r\nMacro-average\s+prec:\s+([\d\.]*),\s+rec:\s+([\d\.]*),\s+f1:\s+([\d\.]*)(?:.*(?:\r\n)*)+?Micro-average\s+prec:\s+([\d\.]*),\s+rec:\s+([\d\.]*),\s+f1:\s+([\d\.]*)(?:.*(?:\r\n)*)+?Quality on test(?:.*(?:\r\n)*)+?tp:\s+(\d+)\s+fp:\s+(\d+)\s+fn:\s+(\d+).*\r\nMacro-average\s+prec:\s+([\d\.]*),\s+rec:\s+([\d\.]*),\s+f1:\s+([\d\.]*)(?:.*(?:\r\n)*)+?Micro-average\s+prec:\s+([\d\.]*),\s+rec:\s+([\d\.]*),\s+f1:\s+([\d\.]*)

$1\t$2\t$3\t$4\t$5\t$6\t$7\t$8\t$9\t$10\t$11\t$12\t$13\t$14\t$15\t$16\t$17\t$18\t$19\t$20\t$21\t$22\t$23

 */
sc.setLogLevel("WARN")

import com.johnsnowlabs.nlp.{Annotation, SparkNLP, DocumentAssembler, Finisher, AnnotatorType}
import com.johnsnowlabs.nlp.{RecursivePipeline, LightPipeline}
import com.johnsnowlabs.nlp.annotators.{Stemmer, Tokenizer, Normalizer, Chunker, ChunkTokenizer}
import com.johnsnowlabs.nlp.annotators.common.NerTagged
import com.johnsnowlabs.nlp.annotators.ner.{NerConverter, NerApproach, Verbose}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach}
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.embeddings.{AlbertEmbeddings, BertEmbeddings, WordEmbeddingsModel, ChunkEmbeddings}
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import com.johnsnowlabs.nlp.training.{CoNLL, PubTator}
import com.johnsnowlabs.util.Benchmark
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ResourceHelper, ReadAs}
//import com.johnsnowlabs.nlp.eval.ner.NerDLEvaluation
//val nerDLEvaluation = new NerDLEvaluation(spark, testFile, tagLevel)
//nerDLEvaluation.computeAccuracyAnnotator(trainFile, nerApproach, embeddings)

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row, Dataset}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.feature.NGram

import spark.implicits._

import scala.util.matching.Regex

import es.rcs.tfm.nlp.model.TfmType
import es.rcs.tfm.nlp.util.{TfmHelper, TfmSave, TfmMeasure}

//val CORPUS_DIR = "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus"
val CORPUS_DIR = "/D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus"

val embeddings = TfmHelper.prepareBert(
//	"file://" + CORPUS_DIR + "/models/bert/biobert_pmc_base_cased_en_2.5.0_2.4_1590489029151")
//	"file://" + CORPUS_DIR + "/models/bert/biobert_pubmed_pmc_base_cased_en_2.5.0_2.4_1590489367180")
//	"file://" + CORPUS_DIR + "/models/bert/biobert_pubmed_base_cased_en_2.5.0_2.4_1590487367971")
//	"file://" + CORPUS_DIR + "/models/bert/biobert_pubmed_large_cased_en_2.5.0_2.4_1590487739645")
//	"file://" + CORPUS_DIR + "/models/bert/biobert_discharge_base_cased_en_2.5.0_2.4_1590490193605")
//	"file://" + CORPUS_DIR + "/models/bert/biobert_clinical_base_cased_en_2.5.0_2.4_1590489819943")
//	"file://" + CORPUS_DIR + "/models/bert/bert_base_cased_en_2.4.0_2.4_1580579557778")
//	"file://" + CORPUS_DIR + "/models/bert/bert_base_uncased_en_2.4.0_2.4_1580579889322")
//	"file://" + CORPUS_DIR + "/models/bert/bert_large_cased_en_2.4.0_2.4_1580580251298")
	"file://" + CORPUS_DIR + "/models/bert/bert_large_uncased_en_2.4.0_2.4_1580581306683")
//	"file://" + CORPUS_DIR + "/models/bert/bert_multi_cased_xx_2.4.0_2.4_1580582335793")

val testDS=embeddings.transform(CoNLL().readDataset(
	spark,
	"file://" + CORPUS_DIR + "/training/conll/eng.testb.conll"))
//	"file://" + CORPUS_DIR + "/training/conll/ner_bioc_test.conll"))
//	"file://" + CORPUS_DIR + "/training/conll/ner_txt_test.conll"))

testDS.
	write.
	mode("overwrite").
	parquet("file://" + CORPUS_DIR + "/training/tmp/eng.testb.conll.parquet")
//	parquet("file://" + CORPUS_DIR + "/training/tmp/ner_bioc_test.conll.parquet")
//	parquet("file://" + CORPUS_DIR + "/training/tmp/ner_txt_test.conll.parquet")

val trainDS=embeddings.transform(CoNLL().readDataset(
	spark,
	"file://" + CORPUS_DIR + "/training/conll/eng.train_testa.conll"))
//	"file://" + CORPUS_DIR + "/training/conll/ner_bioc_train.conll"))
//	"file://" + CORPUS_DIR + "/training/conll/ner_txt_train.conll"))


val nerApproach = new NerDLApproach().
	setTestDataset("file://" + CORPUS_DIR + "/training/tmp/eng.testb.conll.parquet").
//	setTestDataset("file://" + CORPUS_DIR + "/training/tmp/ner_bioc_test.conll.parquet").
//	setTestDataset("file://" + CORPUS_DIR + "/training/tmp/ner_txt_test.conll.parquet").
	setGraphFolder(CORPUS_DIR + "/models/tensorflow").
	setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS)).
	setOutputCol(TfmType.NAMED_ENTITY).
	setLabelColumn(TfmType.LABEL).
	setEnableOutputLogs(true).
	setIncludeConfidence(true).
	setEvaluationLogExtended(true).
	setMinEpochs(20).
	setMaxEpochs(150).
	setLr(0.1f).
	setPo(0.05f).
	setDropout(0.5f).
	setValidationSplit(0.20f).
	setVerbose(Verbose.PerStep)

val nerTagger = nerApproach.
	fit(trainDS)

TfmSave.saveModel(
	nerTagger, 
//	"file://" + CORPUS_DIR + "/models/ner/tfm_eng_ner_pubmed_base_cased_conll_M-128_B-32")
//	"file://" + CORPUS_DIR + "/models/ner/tfm_eng_ner_pubmed_large_cased_conll_M-128_B-32")
//	"file://" + CORPUS_DIR + "/models/ner/tfm_eng_ner_pmc_base_cased_conll_M-128_B-32")
//	"file://" + CORPUS_DIR + "/models/ner/tfm_eng_ner_pubmedpmc_base_cased_conll_M-128_B-32")
//	"file://" + CORPUS_DIR + "/models/ner/tfm_eng_ner_bert_base_cased_conll_M-128_B-32")
//	"file://" + CORPUS_DIR + "/models/ner/tfm_eng_ner_bert_base_uncased_conll_M-128_B-32")
//	"file://" + CORPUS_DIR + "/models/ner/tfm_eng_ner_bert_large_cased_conll_M-128_B-32")
	"file://" + CORPUS_DIR + "/models/ner/tfm_eng_ner_bert_large_uncased_conll_M-128_B-32")
//	"file://" + CORPUS_DIR + "/models/ner/tfm_eng_ner_bert_multi_cased_conll_M-128_B-32")

TfmMeasure.measureDL(
	nerApproach,
	nerTagger, 
	trainDS,
	true,
	10)

TfmMeasure.measureDL(
	nerApproach,
	nerTagger, 
	testDS,
	true,
	10)

TfmMeasure.measureTagsDL(
	nerTagger,
	trainDS,
	10)

TfmMeasure.measureTagsDL(
	nerTagger,
	testDS,
	10)

