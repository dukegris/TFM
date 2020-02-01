// spark-shell --packages JohnSnowLabs:spark-nlp:2.3.4 --executor-memory=8g --executor-cores=24 --driver-memory=8g
// spark-shell --packages JohnSnowLabs:spark-nlp:2.3.4 --executor-memory=8g --executor-cores=6 --driver-memory=8g
// scp -r . rcuesta@10.160.1.215:/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models

import com.johnsnowlabs.nlp.{SparkNLP, DocumentAssembler, Finisher, AnnotatorType}
import com.johnsnowlabs.nlp.{RecursivePipeline, LightPipeline}
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import com.johnsnowlabs.nlp.embeddings.{BertEmbeddings, WordEmbeddingsFormat, WordEmbeddingsModel, ChunkEmbeddings}
import com.johnsnowlabs.nlp.annotators.{Tokenizer, Normalizer, Chunker, ChunkTokenizer}
import com.johnsnowlabs.nlp.annotators.ner.NerConverter
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel} 
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.util.Benchmark

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
	setOutputCol("sentences")

val token = new Tokenizer().
	setInputCols(Array("sentences")).
	setOutputCol("token")

val normalizer = new Normalizer().
	setInputCols("token").
	setOutputCol("normal")

val pos = PerceptronModel.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/pos/pos_anc_en_2.0.2_2.4_1556659930154").
	setInputCols(Array("sentences", "token")).
	setOutputCol("pos")
 
val embeddings = BertEmbeddings.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/cased_L-12_H-768_A-12_M-512_B-32").
	setDimension(768).
	setMaxSentenceLength(512).
	setInputCols(Array("sentences", "token")).
	setOutputCol("embeddings")

val ner = NerDLModel.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/ner_dl_bert_cased_L-12_H-768_A-12_M-512_B-32_2.2.0_2.4_20190830").
	setInputCols(Array("sentences", "token", "embeddings")).
	setOutputCol("ner")

val chunk = new NerConverter().
	setInputCols(Array("sentences", "token", "ner")).
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

val finisher = new Finisher().
	setInputCols(Array(
		"document", 
		"sentences", 
		"token", 
		"normal", 
		"pos", 
		"embeddings", 
		"ner",
		"chunk",
		//"chunk_embeddings"
		"3-gram",
		"3-grams"//,
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
		ner,
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
