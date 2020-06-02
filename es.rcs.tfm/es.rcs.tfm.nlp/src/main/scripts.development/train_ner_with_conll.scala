// spark-shell --packages com.johnsnowlabs.nlp:spark-nlp_2.11:2.4.3,org.slf4j:slf4j-log4j12:1.7.28 --jars es.rcs.tfm.nlp/target/RCS-Nlp-0.0.4-SNAPSHOT.jar --executor-memory=32g --executor-cores=6 --driver-memory=24g --conf "spark.executor.extraJavaOptions='-Dlog4j.configuration=/opt/spark/conf/log4j.properties'" --conf "spark.driver.maxResultSize=8g" > dl.txt
// scp -r . rcuesta@10.160.1.215:D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models

import com.johnsnowlabs.nlp.{Annotation, SparkNLP, DocumentAssembler, Finisher, AnnotatorType}
import com.johnsnowlabs.nlp.{RecursivePipeline, LightPipeline}
import com.johnsnowlabs.nlp.annotators.{Stemmer, Tokenizer, Normalizer, Chunker, ChunkTokenizer}
import com.johnsnowlabs.nlp.annotators.common.NerTagged
import com.johnsnowlabs.nlp.annotators.ner.{NerConverter, NerApproach, Verbose}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach}
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.embeddings.{BertEmbeddings, WordEmbeddingsModel, ChunkEmbeddings}
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import com.johnsnowlabs.nlp.training.CoNLL
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

val embeddings = TfmHelper.prepareBert(
	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/bert_base_cased_en_2.4.0_2.4_1580579557778")

val testConll=embeddings.transform(CoNLL().readDataset(
	spark,
//	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/conll/eng.testb.conll"))
//	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/conll/ner_bioc_test.conll"))
	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/conll/ner_txt_test.conll"))

testConll.
	write.
	mode("overwrite").
//	parquet("file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/eng.testb.conll.parquet")
//	parquet("file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/ner_bioc_test.conll.parquet")
	parquet("file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/ner_txt_test.conll.parquet")

val trainingConll=embeddings.transform(CoNLL().readDataset(
	spark,
//	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/conll/eng.train_testa.conll"))
//	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/conll/ner_bioc_train.conll"))
	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/conll/ner_txt_train.conll"))

val nerApproach = TfmHelper.prepareNerDLApproach(
//	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/eng.testb.conll.parquet",
//	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/ner_bioc_test.conll.parquet",
	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/ner_txt_test.conll.parquet",
	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/tensorflow")

val nerTagger = nerApproach.
	setEnableOutputLogs(true).
	setMinEpochs(20).
	setMaxEpochs(20).
	setLr(0.002f).
	setPo(0.0f).
	setDropout(0.68f).
	setValidationSplit(0.33f).
	setVerbose(Verbose.PerStep).
	fit(trainingConll)

TfmSave.saveModel(
	nerTagger, 
	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/tfm_ner_M-128_B-32")

TfmMeasure.measureDL(
	nerApproach,
	nerTagger, 
	trainingConll,
	true,
	10)

TfmMeasure.measureDL(
	nerApproach,
	nerTagger, 
	testConll,
	true,
	10)

TfmMeasure.measureTagsDL(
	nerTagger,
	trainingConll,
	10)

TfmMeasure.measureTagsDL(
	nerTagger,
	testConll,
	10)

