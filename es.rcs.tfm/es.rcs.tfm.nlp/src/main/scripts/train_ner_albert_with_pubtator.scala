// spark-shell --packages com.johnsnowlabs.nlp:spark-nlp_2.11:2.5.1,org.slf4j:slf4j-log4j12:1.7.28 --jars es.rcs.tfm.nlp/target/RCS-Nlp-0.0.4-SNAPSHOT.jar --executor-memory=32g --executor-cores=6 --driver-memory=24g --conf "spark.executor.extraJavaOptions='-Dlog4j.configuration=/opt/spark/conf/log4j.properties'" --conf "spark.driver.maxResultSize=8g" > dl_albert_pubtator.txt
// scp -r . rcuesta@10.160.1.215:/" + CORPUS_DIR + "/models

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

val embeddings = TfmHelper.prepareAlbert(
//	"file://" + CORPUS_DIR + "/models/bert/albert_base_uncased_en_2.5.0_2.4_1588073363475")
//	"file://" + CORPUS_DIR + "/models/bert/albert_large_uncased_en_2.5.0_2.4_1588073397355")
//	"file://" + CORPUS_DIR + "/models/bert/albert_xlarge_uncased_en_2.5.0_2.4_1588073443653")
	"file://" + CORPUS_DIR + "/models/bert/albert_xxlarge_uncased_en_2.5.0_2.4_1588073588232")

val testDS=embeddings.transform(PubTator.readDataset(
	spark,
	"file://" + CORPUS_DIR + "/datasets/tmVar/tmVarCorpus/test.PubTator.txt"))

testDS.
	write.
	mode("overwrite").
	parquet("file://" + CORPUS_DIR + "/training/tmp/ner_txt_test.pubtator.parquet")

val trainDS=embeddings.transform(PubTator.readDataset(
	spark,
	"file://" + CORPUS_DIR + "/datasets/tmVar/tmVarCorpus/train.PubTator.txt"))

val nerApproach = new NerDLApproach().
	setTestDataset("file://" + CORPUS_DIR + "/training/tmp/ner_txt_test.pubtator.parquet").
	setGraphFolder(CORPUS_DIR + "/models/tensorflow").
	setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS)).
	setOutputCol(TfmType.NAMED_ENTITY).
	setLabelColumn(TfmType.LABEL).
	setEnableOutputLogs(true).
	setIncludeConfidence(true).
	setEvaluationLogExtended(true).
	setMinEpochs(20).
	setMaxEpochs(20).
	setLr(0.002f).
	setPo(0.0f).
	setDropout(0.68f).
	setValidationSplit(0.20f).
	setVerbose(Verbose.PerStep)

val nerTagger = nerApproach.
	fit(trainDS)

TfmSave.saveModel(
	nerTagger, 
//	CORPUS_DIR + "/models/ner/tfm_ner_albert_base_pubtator_M-128_B-32")
//	CORPUS_DIR + "/models/ner/tfm_ner_albert_large_pubtator_M-128_B-32")
//	CORPUS_DIR + "/models/ner/tfm_ner_albert_xlarge_pubtator_M-128_B-32")
	CORPUS_DIR + "/models/ner/tfm_ner_albert_xxlarge_pubtator_M-128_B-32")

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

