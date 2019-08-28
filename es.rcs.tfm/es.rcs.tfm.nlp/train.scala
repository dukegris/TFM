import com.johnsnowlabs.nlp.{SparkNLP, DocumentAssembler, Finisher, AnnotatorType}
import com.johnsnowlabs.nlp.{RecursivePipeline, LightPipeline}
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import com.johnsnowlabs.nlp.embeddings.{BertEmbeddings, WordEmbeddingsFormat, WordEmbeddingsModel}
import com.johnsnowlabs.nlp.annotators.{Tokenizer, Normalizer}
import com.johnsnowlabs.nlp.annotators.ner.{NerConverter, NerApproach}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach, PretrainedNerDL} 
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.training.CoNLL
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ReadAs, ResourceHelper}
import com.johnsnowlabs.util.Benchmark

import org.apache.spark.ml.{Pipeline, PipelineModel}

import spark.implicits._

import scala.util.matching.Regex

val document = new DocumentAssembler().
	setInputCol("text").
	setOutputCol("document").
	setCleanupMode("shrink")

val sentence = new SentenceDetector().
	setInputCols("document").
	setOutputCol("sentences")

val token = new Tokenizer().
	setInputCols("sentences").
	setOutputCol("token")

val pos = PerceptronModel.pretrained().
	setInputCols(Array("sentences", "token")).
	setOutputCol("pos")
 
val embeddings = BertEmbeddings.
	load("models/uncased_L-12_H-768_A-12_M-128_B-32").
	setMaxSentenceLength(512).
	setDimension(768).
	setInputCols(Array("sentences", "token")).
	setOutputCol("embeddings")

val pipelineBert = new RecursivePipeline().
	setStages(Array(
		document,
		sentence,
		token,
		pos,
		embeddings
))

val conll = CoNLL()

val model = pipelineBert.fit(emptyData)

val training_data = conll.
	readDataset(
		ResourceHelper.spark, 
		"../ner/conll2003/eng.testa")
val trainData = model.
	transform(training_data)

val test_data = conll.
	readDataset(
		ResourceHelper.spark, 
		"../ner/conll2003/eng.testb")
val testData = model.
	transform(test_data)

testData.
	write.
	mode("overwrite").
	parquet("./tmp_conll_test")

val ner =  new NerDLApproach().
	setInputCols(Array("sentences", "token", "embeddings")).
	setOutputCol("ner").
	setLabelColumn("label").
	setRandomSeed(0).
	setLr(1e-3f).
	setPo(5e-3f).
	setDropout(5e-1f).
	setMaxEpochs(10).
	setVerbose(0).
	setTrainValidationProp(0.99f).
	setEvaluationLogExtended(true).
	setTestDataset("./tmp_conll_test/")

val result = ner.fit(trainData)

result.
	write.
	overwrite.
	save("ner_general")