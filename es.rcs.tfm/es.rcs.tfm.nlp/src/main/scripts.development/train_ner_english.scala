// spark-shell --packages com.johnsnowlabs.nlp:spark-nlp_2.11:2.4.3,org.slf4j:slf4j-log4j12:1.7.28 --jars es.rcs.tfm.nlp/target/RCS-Nlp-0.0.4-SNAPSHOT.jar --executor-memory=32g --executor-cores=6 --driver-memory=24g --conf "spark.executor.extraJavaOptions='-Dlog4j.configuration=/opt/spark/conf/log4j.properties'" --conf "spark.driver.maxResultSize=8g"
// > dl.txt

import com.johnsnowlabs.nlp.{SparkNLP, DocumentAssembler, Finisher, AnnotatorType}
import com.johnsnowlabs.nlp.{RecursivePipeline, LightPipeline}
import com.johnsnowlabs.nlp.annotators.{Stemmer, Tokenizer, Normalizer, Chunker, ChunkTokenizer}
import com.johnsnowlabs.nlp.annotators.common.NerTagged
import com.johnsnowlabs.nlp.annotators.ner.{NerConverter, Verbose}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach}
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.embeddings.{BertEmbeddings, WordEmbeddingsModel, ChunkEmbeddings}
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import com.johnsnowlabs.nlp.training.CoNLL
import com.johnsnowlabs.util.Benchmark
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ResourceHelper, ReadAs}

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row, Dataset}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.feature.NGram

import spark.implicits._

import scala.util.matching.Regex

val emptyData = spark.emptyDataset[String].toDF("text")

val testData = spark.createDataFrame(Seq(
	(18, "Ehlers-Danlos syndrome, vascular type (vEDS) (MIM #130050) is an autosomal dominant disorder caused by type III procollagen gene (COL3A1) mutations. Most COL3A1 mutations are detected y using total RNA from patient-derived fibroblasts, which requires an invasive skin biopsy. High-resolution melting curve analysis (hrMCA) has recently been developed as a post-PCR mutation scanning method which enables simple, rapid, cost-effective, and highly sensitive mutation screening of large genes. We established a hrMCA method to screen for COL3A1 mutations using genomic DNA. PCR primers pairs for COL3A1 (52 amplicons) were designed to cover all coding regions of the 52 exons, including the splicing sites. We used 15 DNA samples (8 validation samples and 7 samples of clinically suspected vEDS patients) in this study. The eight known COL3A1 mutations in validation samples were all successfully detected by the hrMCA. In addition, we identified five novel COL3A1 mutations, including one deletion (c.2187delA) and one nonsense mutation (c.2992C>T) that could not be determined by the conventional total RNA method. Furthermore, we established a small amplicon genotyping (SAG) method for detecting three high frequency coding-region SNPs (rs1800255:G>A, rs1801184:T>C, and rs2271683:A>G) in COL3A1 to differentiate mutations before sequencing. The use of hrMCA in combination with SAG from genomic DNA enables rapid detection of COL3A1 mutations with high efficiency and specificity. A better understanding of the genotype-phenotype correlation in COL3A1 using this method will lead to improve in diagnosis and treatment."),
	(99, "HGNC:37133")
)).toDF("id", "text")

val document = new DocumentAssembler().
	setInputCol("text").
	setOutputCol("document").
	setCleanupMode("shrink")

val sentence = new SentenceDetector().
	setInputCols(Array("document")).
	setOutputCol("sentence")

val token = new Tokenizer().
	setInputCols(Array("sentence")).
	setOutputCol("token")

val normalizer = new Normalizer().
	setInputCols("token").
	setOutputCol("normal")

val pos = PerceptronModel.
	load("file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/pos/pos_anc_en_2.0.2_2.4_1556659930154").
	setInputCols(Array("sentence", "token")).
	setOutputCol("pos")

val embeddings = BertEmbeddings.
//	load("file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/bert_base_cased_en_2.4.0_2.4_1580579557778").
//	setDimension(768).
//	setMaxSentenceLength(512).
//	setBatchSize(256).
//	setCaseSensitive(true).
	load("file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/bert_large_uncased_en_2.4.0_2.4_1580581306683").
	setDimension(1024).
	setMaxSentenceLength(512).
	setBatchSize(256).
	setCaseSensitive(false).
	setPoolingLayer(-1).
	setInputCols(Array("sentence", "token")).
	setOutputCol("embeddings")

val glove = WordEmbeddingsModel.
	pretrained(name ="glove_100d", lang="en").
	setInputCols(Array("sentence", "token")).
	setOutputCol("embeddings").
	setCaseSensitive(false)

val nerGlove = NerDLModel.
	load("file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/ner_dl_en_2.4.0_2.4_1580251789753").
	setInputCols(Array("sentence", "token", "embeddings")).
	setOutputCol("ner")

val trainingConll=embeddings.transform(CoNLL().readDataset(
	spark,
	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/conll/eng.train_testb.conll"))
//	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/conll/ner_bioc_train.conll"))
//	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/conll/ner_txt_train.conll"))

val testConll=embeddings.transform(CoNLL().readDataset(
	spark,
	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/conll/eng.testa.conll"))
//	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/conll/ner_bioc_test.conll"))
//	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/conll/ner_txt_test.conll"))

testConll.
	write.
	mode("overwrite").
	parquet("file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/eng.testa.conll.parquet")
//	parquet("file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/ner_bioc_test.conll.parquet")
//	parquet("file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/ner_txt_test.conll.parquet")

val nerApproach = new NerDLApproach().
	setTestDataset("file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/eng.testa.conll.parquet").
//	setTestDataset("file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/ner_bioc_test.conll.parquet").
//	setTestDataset("file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/ner_txt.conll.parquet").
	setInputCols(Array("sentence", "token", "embeddings")).
	setLabelColumn("label").
	setOutputCol("ner").
	setEnableOutputLogs(true).
	setIncludeConfidence(true).
	setEvaluationLogExtended(true).
	setVerbose(Verbose.TrainingStat).
	setMinEpochs(20).
	setMaxEpochs(20).
	setValidationSplit(0.33f).
	setLr(0.002f).
	setPo(0.0001f).
	setDropout(0.68f).
	setBatchSize(512)
/*
Verbose
  val All = Value(0)
  val PerStep = Value(1)
  val Epochs = Value(2)
  val TrainingStat = Value(3)
  val Silent = Value(4)
  */

val nerTagger = nerApproach.
	setLabelColumn("label").
	fit(trainingConll)

nerTagger.write.
	overwrite.
	save("D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/tfm_ner_english_M-512_B-256_bert_large_uncased_en")

val chunk = new NerConverter().
	setInputCols(Array("sentence", "token", "ner")).
	setOutputCol("chunk")

val chunker = new Chunker().
	setInputCols(Array("document", "pos")).
	setOutputCol("chunk").
	setRegexParsers(Array("‹NNP›+", "‹DT|PP\\$›?‹JJ›*‹NN›"))

val chunkEmbeddings = new ChunkEmbeddings().
	setInputCols(Array("chunk", "embeddings")).
	setOutputCol("chunk_embeddings").
	setPoolingStrategy("AVERAGE")

val ngram = new NGram().
	setN(3).
	setInputCol("finished_normal").
	setOutputCol("3-gram")

val gramAssembler = new DocumentAssembler().
	setInputCol("3-gram").
	setOutputCol("3-grams")

val nerConverter = new NerConverter().
	setInputCols("sentence", "token", "ner"). // BUG
	setOutputCol("ner_converter")

val labelConverter = new NerConverter().
	setInputCols("sentence", "token", "label").
	setOutputCol("label_converter")

val finisher = new Finisher().
	setInputCols(Array(
		"document",
		"sentence",
		"token",
		"normal",
		"pos",
		"embeddings",
		"ner",
		"chunk"//,
		//"chunk_embeddings"
		)).
	setIncludeMetadata(true).
	setCleanAnnotations(false)

val pipeline = new RecursivePipeline().
	setStages(Array(
		document,
		sentence,
		token,
		normalizer,
		pos,
		embeddings,
		//glove,
		//nerGlove
		nerTagger,
		chunker,
		//chunkEmbeddings,
		finisher,
		ngram,
		gramAssembler
))

val model = pipeline.fit(emptyData)
val result = model.transform(testData)
result.show
result.withColumn("ner1", org.apache.spark.sql.functions.explode(result.col("ner"))).show
result.withColumn("res", org.apache.spark.sql.functions.explode(result.col("finished_ner"))).select("res").show

// GUARDAR EL MODELO
val ner = model.
	stages.
	filter(s => s.isInstanceOf[NerDLModel]).
	head.
	asInstanceOf[NerDLModel]

ner.write.
	overwrite.
	save("D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/tfm_ner_english_M-512_B-256_bert_large_uncased_en_desdepipeline")

// Nos hace falta un grafo para con mas tags al incluir B-, I-, E- para cada mutacion 4
// Tenemos 14 4x3 + O + ""
val trainDataset = NerTagged.collectTrainingInstances(
	trainingConll.toDF(), 
	Array("sentence", "token", "embeddings"), "label")
val labels = trainDataset.flatMap(r => r._1.labels).distinct
labels.length

// Tenemos 768 dimensiones (procede del modelo BERT
val trainSentences = trainDataset.map(r => r._2)
trainSentences.length

val embeddingsDim = nerApproach.calculateEmbeddingsDim(trainSentences)

// Tenemos 88 caracteres diferentes
val chars = trainDataset.
	flatMap(r => r._2.
		tokens.
		flatMap(token => token.token.toCharArray)).
	distinct
chars.length

val nerTF = nerTagger.getModelIfNotSet

// Probar overfit
nerTF.measure(trainDataset, (s: String) => System.out.println(s), true, 10)

// Resultados sobre test
val testDataset = NerTagged.collectTrainingInstances(
	testConll.toDF(), 
	Array("sentence", "token", "embeddings"), "label")
nerTF.measure(testDataset, (s: String) => System.out.println(s), true, 10)

// Resultados sobre test (measureTagsDL)
val nerTransform = nerTagger.transform(testConll)
val nerTagsConverted = nerConverter.transform(nerTransform)
val resultForMeasure = labelConverter.transform(nerTagsConverted)

resultForMeasure.
	withColumn(
		"label_converter_explode", 
		org.apache.spark.sql.functions.explode(resultForMeasure.col("label_converter"))).
	show
resultForMeasure.
	withColumn(
		"label_converter_explode", 
		org.apache.spark.sql.functions.explode(resultForMeasure.col("label_converter"))).
	select("label_converter_explode").
	show
