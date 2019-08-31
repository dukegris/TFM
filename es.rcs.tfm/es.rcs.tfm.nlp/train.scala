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

val conll = CoNLL()

val emptyData = spark.emptyDataset[String].toDF("text")

val training_data = conll.
	readDataset(
		ResourceHelper.spark, 
		"file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/sparknlp/eng.train")

val test_data = conll.
	readDataset(
		ResourceHelper.spark, 
		"file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/sparknlp/eng.testa")

val document = new DocumentAssembler().
	setInputCol("text").
	setOutputCol("document").
	setCleanupMode("shrink")

val sentence = new SentenceDetector().
	setInputCols(Array("document")).
	setOutputCol("sentences")

val token = new Tokenizer().
	setInputCols(Array("sentences")).
	setOutputCol("token")

val pos = PerceptronModel.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/pos/pos_anc_en_2.0.2_2.4_1556659930154").
	setInputCols(Array("sentences", "token")).
	setOutputCol("pos")
 
// --------------------------------------------------------------------------------------------------------------
// UNCASED 2018 - 768 12 layers
val embeddings = BertEmbeddings.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/uncased_L-12_H-768_A-12_M-512_B-32").
	setDimension(768).
	setMaxSentenceLength(512).
	setInputCols(Array("sentences", "token")).
	setOutputCol("embeddings")

val pipeline = new RecursivePipeline().
	setStages(Array(
		document,
		sentence,
		token,
		pos,
		embeddings
))

val model = pipeline.
	fit(emptyData)

val trainData = model.
	transform(training_data)

val testData = model.
	transform(test_data)

testData.
	write.
	mode("overwrite").
	parquet("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_uncased_L-12_H-768_A-12_M-512_B-32")

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
	setTestDataset("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_uncased_L-12_H-768_A-12_M-512_B-32")

val result = ner.fit(trainData)

result.
	write.
	overwrite.
	save("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/ner_dl_bert_uncased_L-12_H-768_A-12_M-512_B-32_2.2.0_2.4_20190830")

// --------------------------------------------------------------------------------------------------------------
// CASED 2018 - 768 12 layers
val embeddings = BertEmbeddings.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/cased_L-12_H-768_A-12_M-512_B-32").
	setDimension(768).
	setMaxSentenceLength(512).
	setInputCols(Array("sentences", "token")).
	setOutputCol("embeddings")

val pipeline = new RecursivePipeline().
	setStages(Array(
		document,
		sentence,
		token,
		pos,
		embeddings
))

val model = pipeline.
	fit(emptyData)

val trainData = model.
	transform(training_data)

val testData = model.
	transform(test_data)

testData.
	write.
	mode("overwrite").
	parquet("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_cased_L-12_H-768_A-12_M-512_B-32")

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
	setTestDataset("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_cased_L-12_H-768_A-12_M-512_B-32")

val result = ner.fit(trainData)

result.
	write.
	overwrite.
	save("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/ner_dl_bert_cased_L-12_H-768_A-12_M-512_B-32_2.2.0_2.4_20190830")

// --------------------------------------------------------------------------------------------------------------
// UNCASED 2018 - 1024 12 layers
val embeddings = BertEmbeddings.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/uncased_L-12_H-1024_A-12_M-512_B-32").
	setDimension(1024).
	setMaxSentenceLength(512).
	setInputCols(Array("sentences", "token")).
	setOutputCol("embeddings")

val pipeline = new RecursivePipeline().
	setStages(Array(
		document,
		sentence,
		token,
		pos,
		embeddings
))

val model = pipeline.
	fit(emptyData)

val trainData = model.
	transform(training_data)

val testData = model.
	transform(test_data)

testData.
	write.
	mode("overwrite").
	parquet("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_uncased_L-12_H-1024_A-12_M-512_B-32")

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
	setTestDataset("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_uncased_L-12_H-1024_A-12_M-512_B-32")

val result = ner.fit(trainData)

result.
	write.
	overwrite.
	save("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/ner_dl_bert_uncased_L-12_H-1024_A-12_M-512_B-32_2.2.0_2.4_20190830")

// --------------------------------------------------------------------------------------------------------------
// CASED 2018 - 1024 12 layers
val embeddings = BertEmbeddings.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/cased_L-12_H-1024_A-12_M-512_B-32").
	setDimension(1024).
	setMaxSentenceLength(512).
	setInputCols(Array("sentences", "token")).
	setOutputCol("embeddings")

val pipeline = new RecursivePipeline().
	setStages(Array(
		document,
		sentence,
		token,
		pos,
		embeddings
))

val model = pipeline.
	fit(emptyData)

val trainData = model.
	transform(training_data)

val testData = model.
	transform(test_data)

testData.
	write.
	mode("overwrite").
	parquet("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_cased_L-12_H-1024_A-12_M-512_B-32")

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
	setTestDataset("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_cased_L-12_H-1024_A-12_M-512_B-32")

val result = ner.fit(trainData)

result.
	write.
	overwrite.
	save("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/ner_dl_bert_cased_L-12_H-1024_A-12_M-512_B-32_2.2.0_2.4_20190830")

 
// --------------------------------------------------------------------------------------------------------------
// UNCASED 2018 - 768 24 layers
// OJO - No esta
val embeddings = BertEmbeddings.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/uncased_L-24_H-768_A-12_M-512_B-32").
	setDimension(768).
	setMaxSentenceLength(512).
	setInputCols(Array("sentences", "token")).
	setOutputCol("embeddings")

val pipeline = new RecursivePipeline().
	setStages(Array(
		document,
		sentence,
		token,
		pos,
		embeddings
))

val model = pipeline.
	fit(emptyData)

val trainData = model.
	transform(training_data)

val testData = model.
	transform(test_data)

testData.
	write.
	mode("overwrite").
	parquet("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_uncased_L-24_H-768_A-12_M-512_B-32")

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
	setTestDataset("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_uncased_L-24_H-768_A-12_M-512_B-32")

val result = ner.fit(trainData)

result.
	write.
	overwrite.
	save("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/ner_dl_bert_uncased_L-24_H-768_A-12_M-512_B-32_2.2.0_2.4_20190830")

// --------------------------------------------------------------------------------------------------------------
// CASED 2018 - 768 24 layers
// OJO - No esta
val embeddings = BertEmbeddings.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/cased_L-24_H-768_A-12_M-512_B-32").
	setDimension(768).
	setMaxSentenceLength(512).
	setInputCols(Array("sentences", "token")).
	setOutputCol("embeddings")

val pipeline = new RecursivePipeline().
	setStages(Array(
		document,
		sentence,
		token,
		pos,
		embeddings
))

val model = pipeline.
	fit(emptyData)

val trainData = model.
	transform(training_data)

val testData = model.
	transform(test_data)

testData.
	write.
	mode("overwrite").
	parquet("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_cased_L-24_H-768_A-12_M-512_B-32")

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
	setTestDataset("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_cased_L-24_H-768_A-12_M-512_B-32")

val result = ner.fit(trainData)

result.
	write.
	overwrite.
	save("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/ner_dl_bert_cased_L-24_H-768_A-12_M-512_B-32_2.2.0_2.4_20190830")

// --------------------------------------------------------------------------------------------------------------
// UNCASED 2018 - 1024 24 layers
val embeddings = BertEmbeddings.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/uncased_L-24_H-1024_A-16_M-512_B-32").
	setDimension(1024).
	setMaxSentenceLength(512).
	setInputCols(Array("sentences", "token")).
	setOutputCol("embeddings")

val pipeline = new RecursivePipeline().
	setStages(Array(
		document,
		sentence,
		token,
		pos,
		embeddings
))

val model = pipeline.
	fit(emptyData)

val trainData = model.
	transform(training_data)

val testData = model.
	transform(test_data)

testData.
	write.
	mode("overwrite").
	parquet("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_uncased_L-24_H-1024_A-16_M-512_B-32")

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
	setTestDataset("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_uncased_L-24_H-1024_A-16_M-512_B-32")

val result = ner.fit(trainData)

result.
	write.
	overwrite.
	save("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/ner_dl_bert_uncased_L-24_H-1024_A-16_M-512_B-32_2.2.0_2.4_20190830")

// --------------------------------------------------------------------------------------------------------------
// CASED 2018 - 1024 24 layers
val embeddings = BertEmbeddings.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/cased_L-24_H-1024_A-16_M-512_B-32").
	setDimension(1024).
	setMaxSentenceLength(512).
	setInputCols(Array("sentences", "token")).
	setOutputCol("embeddings")

val pipeline = new RecursivePipeline().
	setStages(Array(
		document,
		sentence,
		token,
		pos,
		embeddings
))

val model = pipeline.
	fit(emptyData)

val trainData = model.
	transform(training_data)

val testData = model.
	transform(test_data)

testData.
	write.
	mode("overwrite").
	parquet("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_cased_L-24_H-1024_A-16_M-512_B-32")

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
	setTestDataset("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_cased_L-24_H-1024_A-16_M-512_B-32")

val result = ner.fit(trainData)

result.
	write.
	overwrite.
	save("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/ner_dl_bert_cased_L-24_H-1024_A-16_M-512_B-32_2.2.0_2.4_20190830")

// --------------------------------------------------------------------------------------------------------------
// UNCASED 2019 - 1024 24 layers
val embeddings = BertEmbeddings.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/wwm_uncased_L-24_H-1024_A-16").
	setDimension(1024).
	setMaxSentenceLength(512).
	setInputCols(Array("sentences", "token")).
	setOutputCol("embeddings")

val pipeline = new RecursivePipeline().
	setStages(Array(
		document,
		sentence,
		token,
		pos,
		embeddings
))

val model = pipeline.
	fit(emptyData)

val trainData = model.
	transform(training_data)

val testData = model.
	transform(test_data)

testData.
	write.
	mode("overwrite").
	parquet("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_wwm_uncased_L-24_H-1024_A-16")

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
	setTestDataset("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_wwm_uncased_L-24_H-1024_A-16")

val result = ner.fit(trainData)

result.
	write.
	overwrite.
	save("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/ner_dl_bert_wwm_uncased_L-24_H-1024_A-16_2.2.0_2.4_20190830")

// --------------------------------------------------------------------------------------------------------------
// CASED 2019 - 1024 24 layers
val embeddings = BertEmbeddings.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/wwm_cased_L-24_H-1024_A-16").
	setDimension(1024).
	setMaxSentenceLength(512).
	setInputCols(Array("sentences", "token")).
	setOutputCol("embeddings")

val pipeline = new RecursivePipeline().
	setStages(Array(
		document,
		sentence,
		token,
		pos,
		embeddings
))

val model = pipeline.
	fit(emptyData)

val trainData = model.
	transform(training_data)

val testData = model.
	transform(test_data)

testData.
	write.
	mode("overwrite").
	parquet("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_wwm_cased_L-24_H-1024_A-16")

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
	setTestDataset("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/test_conll_wwm_cased_L-24_H-1024_A-16")

val result = ner.fit(trainData)

result.
	write.
	overwrite.
	save("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/ner_dl_bert_wwm_cased_L-24_H-1024_A-16_2.2.0_2.4_20190830")

  