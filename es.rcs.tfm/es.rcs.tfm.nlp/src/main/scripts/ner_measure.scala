// spark-shell --packages JohnSnowLabs:spark-nlp:2.3.4 --executor-memory=8g --executor-cores=24 --driver-memory=8g
// spark-shell --packages JohnSnowLabs:spark-nlp:2.3.4 --executor-memory=16g --executor-cores=6 --driver-memory=16g
// scp -r . rcuesta@10.160.1.215:/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models

import com.johnsnowlabs.nlp.{Annotation, SparkNLP, DocumentAssembler, Finisher, AnnotatorType}
import com.johnsnowlabs.nlp.{RecursivePipeline, LightPipeline}
import com.johnsnowlabs.nlp.annotators.{Stemmer, Tokenizer, Normalizer}
import com.johnsnowlabs.nlp.annotators.common.NerTagged
import com.johnsnowlabs.nlp.annotators.ner.{NerConverter, NerApproach}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach} 
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.embeddings.{BertEmbeddings, WordEmbeddingsFormat, WordEmbeddingsModel}
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import com.johnsnowlabs.nlp.training.CoNLL
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ResourceHelper, ReadAs}

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row, Dataset}
import org.apache.spark.ml.PipelineModel

// PIPELINE

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
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/uncased_L-24_H-1024_A-16_M-128_B-32").
//	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/biobert_v1.1_pubmed_M-512_B-32").
	setDimension(1024).
	setMaxSentenceLength(512).
	setInputCols(Array("sentence", "token")).
	setOutputCol("word_embeddings")

val ner = NerDLModel.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/ner_dl_bert_uncased_L-12_H-1024_A-12_M-512_B-32_2.2.0_2.4_20190830").
	setInputCols(Array("sentence", "token", "word_embeddings")).
	setOutputCol("ner")

val converter = new NerConverter().
	setInputCols("sentence", "token", "label").
	setOutputCol("label_converter")

val finisher = new Finisher().
    setInputCols("document", "sentence", "token", "pos", "word_embeddings", "ner", "label", "label_converter").
    setIncludeMetadata(true).
    setCleanAnnotations(false)

import spark.implicits._
val stages = Array(
    embeddings,
    ner,
    converter,
    finisher
)

val trainPipeline = new RecursivePipeline().setStages(stages)

// DATOS
val nerReader = CoNLL()

val trainFile = ExternalResource(
    "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/ner_txt_train.conll", 
    ReadAs.LINE_BY_LINE, 
    Map.empty[String, String])

val trainData = nerReader.
	readDataset(
		ResourceHelper.spark, 
		trainFile.path)

val testFile = ExternalResource(
    "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/ner_txt_test.conll",
    ReadAs.LINE_BY_LINE, 
    Map.empty[String, String])

val testData = nerReader.
	readDataset(
		ResourceHelper.spark, 
		testFile.path)
)

// REALIZAR EL ENTRENAMIENTO
println(java.time.LocalTime.now + ": NER-TRAIN: begin TRAIN")
val trainModel = trainPipeline.fit(trainData)
println(java.time.LocalTime.now + ": NER-TRAIN: end TRAIN")


// GUARDAR EL MODELO
val ner = trainModel.
    stages.
    filter(s => s.isInstanceOf[NerDLModel]).
    head.
    asInstanceOf[NerDLModel]

// EJECUTAR SOBRE CONJUNTOS
println(java.time.LocalTime.now + ": NER-TRAIN: begin TRANSFORM 01")
val trainedTrainData = trainModel.transform(trainData)
println(java.time.LocalTime.now + ": NER-TRAIN: begin TRANSFORM 02")
val trainedTestData = trainModel.transform(testData)
println(java.time.LocalTime.now + ": NER-TRAIN: end TRANSFORM")


// ESTADISTICAS DEL CONJUNTO DE ENTRENAMIENTO
println(java.time.LocalTime.now + ": NER-TRAIN: begin TRAIN STATISTICS")
val labeledTrain = NerTagged.
    collectTrainingInstances(
        trainedTrainData, 
        Seq("sentence", "token", "word_embeddings"), 
        "label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel")
ner.getModelIfNotSet.measure(labeledTrain, (s: String) => System.out.println(s), true, 0)
	
labeledTrain(0)._1    
trainedTrainData.limit(1).select("text", "finished_ner").foreach { row => { 
    row.toSeq.foreach { col => println(col) } 
} }

// ESTADISTICAS DEL CONJUNTO DE TEST
println(java.time.LocalTime.now + ": NER-TRAIN: begin TEST STATISTICS")
val labeledTest = NerTagged.
    collectTrainingInstances(
        trainedTestData, 
        Seq("sentence", "token", "word_embeddings"), 
        "label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel")
ner.getModelIfNotSet.measure(labeledTest, (s: String) => System.out.println(s), true, 0)
	
labeledTest(0)._1    
trainedTestData.limit(1).select("text", "finished_ner").foreach { row => { 
    row.toSeq.foreach { col => println(col) } 
} }
