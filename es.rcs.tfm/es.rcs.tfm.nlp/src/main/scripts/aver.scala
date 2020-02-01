







































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
//import com.johnsnowlabs.nlp.eval.ner.NerDLEvaluation
//val nerDLEvaluation = new NerDLEvaluation(spark, testFile, tagLevel)
//nerDLEvaluation.computeAccuracyAnnotator(trainFile, nerApproach, embeddings)

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row, Dataset}
import org.apache.spark.ml.PipelineModel

val nerReader = CoNLL()

val trainFile = ExternalResource(
//    "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/ner_txt_train.conll", 
    "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/sparknlp/eng.train",
    ReadAs.LINE_BY_LINE, 
    Map.empty[String, String])

val testFile = ExternalResource(
//    "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/ner_txt_test.conll",
    "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/sparknlp/eng.testa",
    ReadAs.LINE_BY_LINE, 
    Map.empty[String, String])
 
val trainData = nerReader.
	readDataset(
		ResourceHelper.spark, 
		trainFile.path)

val testData = nerReader.
	readDataset(
		ResourceHelper.spark, 
		testFile.path)

/*
val embeddingsTrainData = embeddings.transform(trainData)
val embeddingsTestData = embeddings.transform(testData)
val preparedDataset = embeddingsTrainData.randomSplit(Array(0.85, 0.15), seed = 11L)
*/

val preparedTrainDataset = trainData.randomSplit(Array(0.85, 0.15), seed = 11L)
val preparedTestDataset = testData

embeddings.transform(preparedTrainDataset(1)).
	write.
	mode("overwrite").
	format("parquet").
	save(          "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/CONLL-pubtator.tmp.parquet")

// PIPELINE
val embeddings = BertEmbeddings.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/uncased_L-24_H-1024_A-16_M-128_B-32").
	setDimension(1024).
	setMaxSentenceLength(512).
	setInputCols(Array("sentence", "token")).
	setOutputCol("embeddings")

// ENTRENAMIENTO
val nerTagger =  new NerDLApproach().

	// CONFIGURACION TENSORFLOW
	// setConfigProtoBytes(bytes). // ConfigProto from tensorflow, serialized into byte array
	// setGraphFolder(path). // Folder path that contain external graph files
    setRandomSeed(0). // Random seed
    setMinEpochs(1). // Minimum number of epochs to train
    setMaxEpochs(1). // Maximum number of epochs to train
    setBatchSize(32). // Batch size

    // ENTRENAMIENTO TENSORFLOW
    setLr(1e-3f). // Learning Rate
    setPo(0.005f). // Learning rate decay coefficient. Real Learning Rage = lr / (1 + po * epoch)
    // setDropout(5e-1f). // Dropout coefficient
	setGraphFolder("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/tensorflow").

	// VALIDACIONES
	// setValidationSplit(validationSplit). //Choose the proportion of training dataset to be validated against the model on each Epoch. The value should be between 0.0 and 1.0 and by default it is 0.0 and off.
	setIncludeConfidence(true). // whether to include confidence scores in annotation metadata
	// setTestDataset("tmvar.test"). // Path to test dataset. If set used to calculate statistic on it during training.
	setTestDataset("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/CONLL-pubtator.tmp.parquet").

	// MEDIDAS
	setEnableOutputLogs(true). // Whether to output to annotators log folder
	setEvaluationLogExtended(true). // Whether logs for validation to be extended: it displays time and evaluation of each label. Default is false.
	setIncludeConfidence(true). // whether to include confidence scores in annotation metadata"

	// CONFIGURACI�N NERDLMODEL
	// setUseContrib(false) // whether to use contrib LSTM Cells. Not compatible with Windows
	// setVerbose(2). // Level of verbosity during training
	setEntities(Array("MUT-DNA", "MUT-PRO", "MUT_SNP")). // Entities to recognize
	setInputCols(Array("sentence", "token", "embeddings")).
	setOutputCol("ner").
	setLabelColumn("label") // Column with label per each token

// MODELO OK, APLICACI�N SOBRE EL CONJUNTO DE TEST
val converter = new NerConverter().
	setInputCols("sentence", "token", "ner").
	setOutputCol("chunk")

val finisher = new Finisher().
    setInputCols("document", "sentence", "token", "pos", "embeddings", "ner", "chunk", "label").
    setIncludeMetadata(true).
    setCleanAnnotations(false)

import spark.implicits._
val emptyData = spark.emptyDataset[String].toDF("text")
val stages = Array(
	embeddings,
    nerTagger,
    converter,
    finisher
)

val trainPipeline = new RecursivePipeline().setStages(stages)

println(java.time.LocalTime.now + ": NER-TRAIN: begin train")

val trainModel = trainPipeline.fit(preparedTrainDataset(0))

println(java.time.LocalTime.now + ": NER-TRAIN: begin trained")

val ner = trainModel.
    stages.
    filter(s => s.isInstanceOf[NerDLModel]).
    head.
    asInstanceOf[NerDLModel]

println(java.time.LocalTime.now + ": NER-TRAIN: located")

ner.  
    write.
    overwrite.
    save("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/tfm_ner_pubtator")

println(java.time.LocalTime.now + ": NER-TRAIN: saved")

val trainedTrainData = trainModel.transform(preparedTrainDataset)
val trainedTestData = trainModel.transform(preparedTestDataset)

// Nos hace falta un grafo para con mas tags al incluir B-, I-, E- para cada mutacion 4
// Tenemos 14 4x3 + O + "" 
val trainLabels = NerTagged.collectTrainingInstances(trainedTrainData.toDF(), Array("sentence", "token", "embeddings"), "label")
val labels = trainLabels.flatMap(r => r._1.labels).distinct
println(labels.length)

// Tenemos 768 dimensiones (procede del modelo BERT), ahora 1024
val trainSentences = trainLabels.map(r => r._2)
val embeddingsDim = nerTagger.calculateEmbeddingsDim(trainSentences)
println(embeddingsDim)

// Tenemos 88 caracteres diferentes, ahora 62
val chars = trainLabels.flatMap(r => r._2.tokens.flatMap(token => token.token.toCharArray)).distinct
println(chars.length)

// ESTAD�STICAS DEL CONJUNTO DE TEST
val labeledTrain = NerTagged.
	collectTrainingInstances(
		trainedTrainData, 
		Seq("sentence", "token", "embeddings"), 
		"label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel labeled")
ner.getModelIfNotSet.measure(labeledTrain, (s: String) => System.out.println(s), true, 0)
	

// ESTAD�STICAS DEL CONJUNTO DE TEST
val labeledTest = NerTagged.
	collectTrainingInstances(
		trainedTestData, 
		Seq("sentence", "token", "embeddings"), 
		"label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel labeled")
ner.getModelIfNotSet.measure(labeledTest, (s: String) => System.out.println(s), true, 0)


// DATOS
val actualListOfNamedEntitiesMap = trainedTrainData.
	select("finished_ner").
	collectAsList().
	toArray.
	map(x=>x.toString.drop(1).dropRight(1).split("@")).
	map(keyValuePair=>keyValuePair.
		map(x=>(x.split("->").lastOption.get,x.slice(x.indexOf("->")+2,x.indexOf("#")))).
		filter(!_._1.equals("O")).
		groupBy(_._1).
		mapValues(_.map(_._2).toList))


val length=actualListOfNamedEntitiesMap.length
for(index<-0 until length){
	println("Keys present in actualOutputMap but not in actualOutputMap:  %s".format(actualListOfNamedEntitiesMap(index)))
}
	
labeledTrain(0)._1    
trainedTrainData.limit(1).select("text", "finished_ner").foreach { row => { 
	row.toSeq.foreach { col => println(col) } 
} }

labeledTest(0)._1    
trainedTestData.limit(1).select("text", "finished_ner").foreach { row => { 
	row.toSeq.foreach { col => println(col) } 
} }


trainedTrainData.limit(1).select("finished_token", "finished_ner").foreach { row => { 
	val a = Array(
		row.getSeq[String](0).toArray, 
		row.getSeq[String](1).toArray
	)
	val columns = a(0) zip a(1)
	columns.foreach{ col => println(col) } 
}}

trainedTestData.limit(1).select("finished_token", "finished_ner").foreach { row => { 
	val a = Array(
		row.getSeq[String](0).toArray, 
		row.getSeq[String](1).toArray
	)
	val columns = a(0) zip a(1)
	columns.foreach{ col => println(col) } 
} }
































	
// MODELO OK, APLICACI�N SOBRE EL CONJUNTO DE TEST
val converter = new NerConverter().
	setInputCols("sentence", "token", "ner").
	setOutputCol("chunk")

val finisher = new Finisher().
	setInputCols("sentence", "token", "ner", "chunk").
	setIncludeMetadata(true).
  setCleanAnnotations(false)
//  .
//  setOutputAsArray(false).
//  setValueSplitSymbol(";")


// TRAIN
var nerTrainData = ner.transform(preparedTrainData)
var converterTrainData = converter.transform(nerTrainData)
var finisherTrainData = finisher.transform(converterTrainData)

finisherTrainData.show


// ESTAD�STICAS DEL CONJUNTO DE TEST
val labeledTrain = NerTagged.
  collectTrainingInstances(
    finisherTrainData, 
    Seq("sentence", "token", "embeddings"), 
    "label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel labeled")
ner.getModelIfNotSet.measure(labeledTrain, (s: String) => System.out.println(s), true, 0)
	

// TEST
var nerTestData = ner.transform(preparedTestData)
var converterTestData = converter.transform(nerTestData)
var finisherTestData = finisher.transform(converterTestData)

finisherTestData.show


// ESTAD�STICAS DEL CONJUNTO DE TEST
val labeledTest = NerTagged.
  collectTrainingInstances(
    finisherTestData, 
    Seq("sentence", "token", "embeddings"), 
    "label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel labeled")
ner.getModelIfNotSet.measure(labeledTest, (s: String) => System.out.println(s), true, 0)


// DATOS
	
labeledTrain(0)._1    
finisherTrainData.limit(1).select("text", "finished_ner").foreach { row => { 
  row.toSeq.foreach { col => println(col) } 
} }

labeledTest(0)._1    
finisherTestData.limit(1).select("text", "finished_ner").foreach { row => { 
  row.toSeq.foreach { col => println(col) } 
} }
















val document = new DocumentAssembler().
	setInputCol("text").
	setOutputCol("document").
	setCleanupMode("shrink")

val sentences = new SentenceDetector().
	setInputCols(Array("document")).
	setOutputCol("sentence")

val token = new Tokenizer().
	setInputCols(Array("sentence")).
	setOutputCol("token")

val pos = PerceptronModel.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/pos/pos_anc_en_2.0.2_2.4_1556659930154").
	setInputCols(Array("sentence", "token")).
	setOutputCol("pos")














import spark.implicits._
val emptyData = spark.emptyDataset[String].toDF("text")
val stages = Array(
		document,
		sentences,
		token,
		pos,
		embeddings
)

val preparedPipeline = new RecursivePipeline().setStages(stages)
val preparedModel = preparedPipeline.fit(emptyData)
val preparedTrainData = preparedModel.transform(trainData)
val preparedTestData = preparedModel.transform(testData)


//COSAS DEL TRAIN
val preparedDataset = preparedTrainData.randomSplit(Array(0.85, 0.15), seed = 11L)
preparedDataset(1).write.mode("overwrite").parquet("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/CONLL-test.tmp.parquet")



// Nos hace falta un grafo para con mas tags al incluir B-, I-, E- para cada mutacion 4
// Tenemos 14 4x3 + O + "" 
val trainLabels = NerTagged.collectTrainingInstances(preparedTrainData.toDF(), Array("sentence", "token", "embeddings"), "label")
val labels = trainLabels.flatMap(r => r._1.labels).distinct

// Tenemos 768 dimensiones (procede del modelo BERT), ahora 1024
val trainSentences = trainLabels.map(r => r._2)
val embeddingsDim = nerTagger.calculateEmbeddingsDim(trainSentences)

// Tenemos 88 caracteres diferentes, ahora 62
val chars = trainLabels.flatMap(r => r._2.tokens.flatMap(token => token.token.toCharArray)).distinct
	
	
// MODELO OK, APLICACI�N SOBRE EL CONJUNTO DE TEST
val converter = new NerConverter().
	setInputCols("sentence", "token", "ner").
	setOutputCol("chunk")

val finisher = new Finisher().
	setInputCols("sentence", "token", "ner", "chunk").
	setIncludeMetadata(true).
  setCleanAnnotations(false)
//  .// spark-shell --packages JohnSnowLabs:spark-nlp:2.3.4 --executor-memory=8g --executor-cores=24 --driver-memory=8g
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
val embeddings = BertEmbeddings.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/uncased_L-24_H-1024_A-16_M-128_B-32").
	setDimension(1024).
	setMaxSentenceLength(512).
	setInputCols(Array("sentence", "token")).
	setOutputCol("embeddings")

val nerTagger =  new NerDLApproach().

    // CONFIGURACION TENSORFLOW
    // setConfigProtoBytes(bytes). // ConfigProto from tensorflow, serialized into byte array
    // setGraphFolder(path). // Folder path that contain external graph files
    setRandomSeed(0). // Random seed
    setMinEpochs(1). // Minimum number of epochs to train
    setMaxEpochs(1). // Maximum number of epochs to train
    setBatchSize(32). // Batch size

    // ENTRENAMIENTO TENSORFLOW
    setLr(1e-3f). // Learning Rate
    setPo(0.005f). // Learning rate decay coefficient. Real Learning Rage = lr / (1 + po * epoch)
    // setDropout(5e-1f). // Dropout coefficient
    setGraphFolder("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/tensorflow").

    // VALIDACIONES
    // setValidationSplit(validationSplit). //Choose the proportion of training dataset to be validated against the model on each Epoch. The value should be between 0.0 and 1.0 and by default it is 0.0 and off.
    setIncludeConfidence(true). // whether to include confidence scores in annotation metadata
    // setTestDataset("tmvar.test"). // Path to test dataset. If set used to calculate statistic on it during training.
    setTestDataset(     "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/CONLL-pubtator.tmp.parquet").
    // setTestDataset(preparedTestData).

    // MEDIDAS
    setEnableOutputLogs(true). // Whether to output to annotators log folder
    setEvaluationLogExtended(true). // Whether logs for validation to be extended: it displays time and evaluation of each label. Default is false.
    setIncludeConfidence(true). // whether to include confidence scores in annotation metadata"

    // CONFIGURACI�N NERDLMODEL
    // setUseContrib(false) // whether to use contrib LSTM Cells. Not compatible with Windows
    // setVerbose(2). // Level of verbosity during training
    //setEntities(Array("MUT-DNA", "MUT-PRO", "MUT_SNP")). // Entities to recognize
    setInputCols(Array("sentence", "token", "embeddings")).
    setOutputCol("ner").
    setLabelColumn("label") // Column with label per each token

// MODELO OK, APLICACI�N SOBRE EL CONJUNTO DE TEST
val converter = new NerConverter().
	setInputCols("sentence", "token", "label").
	setOutputCol("label_converter")

val finisher = new Finisher().
    setInputCols("document", "sentence", "token", "pos", "embeddings", "ner", "label", "label_converter").
    setIncludeMetadata(true).
    setCleanAnnotations(false)

import spark.implicits._
val emptyData = spark.emptyDataset[String].toDF("text")
val stages = Array(
    embeddings,
    nerTagger,
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

val allTrainData = nerReader.
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

valr allTrainData
val preparedTrainDataset = allTrainData.randomSplit(Array(0.85, 0.15), seed = 11L)
val trainData = preparedTrainDataset(0)
val validationData = preparedTrainDataset(1)

// PREPARAR DATOS DE VALIDACION PARA NerDLApproach
println(java.time.LocalTime.now + ": NER-TRAIN: begin VALIDATION DATA")
embeddings.
    transform(validationData).
	write.
	mode("overwrite").
	format("parquet").
	save(               "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/CONLL-pubtator.tmp.parquet")
println(java.time.LocalTime.now + ": NER-TRAIN: end VALIDATION DATA")


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

ner.  
    write.
    overwrite.
    save("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/tfm_ner_pubtator")

println(java.time.LocalTime.now + ": NER-TRAIN: saved")


// EJECUTAR SOBRE CONJUNTOS
println(java.time.LocalTime.now + ": NER-TRAIN: begin TRANSFORM 01")
val trainedTrainData = trainModel.transform(trainData)
println(java.time.LocalTime.now + ": NER-TRAIN: begin TRANSFORM 02")
val trainedValidationData = trainModel.transform(validationData)
println(java.time.LocalTime.now + ": NER-TRAIN: begin TRANSFORM 03")
val trainedTestData = trainModel.transform(testData)
println(java.time.LocalTime.now + ": NER-TRAIN: end TRANSFORM")


// Nos hace falta un grafo para con mas tags al incluir B-, I-, E- para cada mutacion 4
// Tenemos 14 4x3 + O + "" 
val trainLabels = NerTagged.collectTrainingInstances(trainedTrainData.toDF(), Array("sentence", "token", "embeddings"), "label")
val labels = trainLabels.flatMap(r => r._1.labels).distinct
println(labels.length)

// Tenemos 768 dimensiones (procede del modelo BERT), ahora 1024
val trainSentences = trainLabels.map(r => r._2)
val embeddingsDim = nerTagger.calculateEmbeddingsDim(trainSentences)
println(embeddingsDim)

// Tenemos 88 caracteres diferentes, ahora 62
val chars = trainLabels.flatMap(r => r._2.tokens.flatMap(token => token.token.toCharArray)).distinct
println(chars.length)



// ESTADISTICAS DEL CONJUNTO DE ENTRENAMIENTO
println(java.time.LocalTime.now + ": NER-TRAIN: begin TRAIN STATISTICS")
val labeledTrain = NerTagged.
    collectTrainingInstances(
        trainedTrainData, 
        Seq("sentence", "token", "embeddings"), 
        "label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel")
ner.getModelIfNotSet.measure(labeledTrain, (s: String) => System.out.println(s), true, 0)
	
labeledTrain(0)._1    
trainedTrainData.limit(1).select("text", "finished_ner").foreach { row => { 
    row.toSeq.foreach { col => println(col) } 
} }

val actualListOfNamedEntitiesMap = trainedTrainData.
	select("finished_ner").
	collectAsList().
	toArray.
	map(x=>x.toString.drop(1).dropRight(1).split("@")).
	map(keyValuePair=>keyValuePair.
		map(x=>(x.split("->").lastOption.get,x.slice(x.indexOf("->")+2,x.indexOf("#")))).
		filter(!_._1.equals("O")).
		groupBy(_._1).
		mapValues(_.map(_._2).toList))


val length=actualListOfNamedEntitiesMap.length
for(index<-0 until length){
	println("Keys present in actualOutputMap but not in actualOutputMap:  %s".format(actualListOfNamedEntitiesMap(index)))
}
println(java.time.LocalTime.now + ": NER-TRAIN: begin TRAIN STATISTICS")



// ESTADISTICAS DEL CONJUNTO DE VALIDACION
println(java.time.LocalTime.now + ": NER-TRAIN: begin VALIDATION STATISTICS")
val labeledValidation = NerTagged.
    collectTrainingInstances(
        trainedValidationData, 
        Seq("sentence", "token", "embeddings"), 
        "label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel")
ner.getModelIfNotSet.measure(labeledValidation, (s: String) => System.out.println(s), true, 0)
	
labeledValidation(0)._1    
trainedValidationData.limit(1).select("text", "finished_ner").foreach { row => { 
    row.toSeq.foreach { col => println(col) } 
} }

val actualListOfNamedEntitiesMap = trainedValidationData.
	select("finished_ner").
	collectAsList().
	toArray.
	map(x=>x.toString.drop(1).dropRight(1).split("@")).
	map(keyValuePair=>keyValuePair.
		map(x=>(x.split("->").lastOption.get,x.slice(x.indexOf("->")+2,x.indexOf("#")))).
		filter(!_._1.equals("O")).
		groupBy(_._1).
		mapValues(_.map(_._2).toList))


val length=actualListOfNamedEntitiesMap.length
for(index<-0 until length){
	println("Keys present in actualOutputMap but not in actualOutputMap:  %s".format(actualListOfNamedEntitiesMap(index)))
}
println(java.time.LocalTime.now + ": NER-TRAIN: begin VALIDATION STATISTICS")



// ESTADISTICAS DEL CONJUNTO DE TEST
println(java.time.LocalTime.now + ": NER-TRAIN: begin TEST STATISTICS")
val labeledTest = NerTagged.
    collectTrainingInstances(
        trainedTestData, 
        Seq("sentence", "token", "embeddings"), 
        "label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel")
ner.getModelIfNotSet.measure(labeledTest, (s: String) => System.out.println(s), true, 0)
	
labeledTest(0)._1    
trainedTestData.limit(1).select("text", "finished_ner").foreach { row => { 
    row.toSeq.foreach { col => println(col) } 
} }

val actualListOfNamedEntitiesMap = trainedTestData.
	select("finished_ner").
	collectAsList().
	toArray.
	map(x=>x.toString.drop(1).dropRight(1).split("@")).
	map(keyValuePair=>keyValuePair.
		map(x=>(x.split("->").lastOption.get,x.slice(x.indexOf("->")+2,x.indexOf("#")))).
		filter(!_._1.equals("O")).
		groupBy(_._1).
		mapValues(_.map(_._2).toList))


val length=actualListOfNamedEntitiesMap.length
for(index<-0 until length){
	println("Keys present in actualOutputMap but not in actualOutputMap:  %s".format(actualListOfNamedEntitiesMap(index)))
}
println(java.time.LocalTime.now + ": NER-TRAIN: begin TEST STATISTICS")










































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
//import com.johnsnowlabs.nlp.eval.ner.NerDLEvaluation
//val nerDLEvaluation = new NerDLEvaluation(spark, testFile, tagLevel)
//nerDLEvaluation.computeAccuracyAnnotator(trainFile, nerApproach, embeddings)

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row, Dataset}
import org.apache.spark.ml.PipelineModel

val nerReader = CoNLL()

val trainFile = ExternalResource(
//    "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/ner_txt_train.conll", 
    "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/sparknlp/eng.train",
    ReadAs.LINE_BY_LINE, 
    Map.empty[String, String])

val testFile = ExternalResource(
//    "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/ner_txt_test.conll",
    "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/sparknlp/eng.testa",
    ReadAs.LINE_BY_LINE, 
    Map.empty[String, String])
 
val trainData = nerReader.
	readDataset(
		ResourceHelper.spark, 
		trainFile.path)

val testData = nerReader.
	readDataset(
		ResourceHelper.spark, 
		testFile.path)

/*
val embeddingsTrainData = embeddings.transform(trainData)
val embeddingsTestData = embeddings.transform(testData)
val preparedDataset = embeddingsTrainData.randomSplit(Array(0.85, 0.15), seed = 11L)
*/

val preparedTrainDataset = trainData.randomSplit(Array(0.85, 0.15), seed = 11L)
val preparedTestDataset = testData

embeddings.transform(preparedTrainDataset(1)).
	write.
	mode("overwrite").
	format("parquet").
	save(          "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/CONLL-pubtator.tmp.parquet")

// PIPELINE
val embeddings = BertEmbeddings.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/uncased_L-24_H-1024_A-16_M-128_B-32").
	setDimension(1024).
	setMaxSentenceLength(512).
	setInputCols(Array("sentence", "token")).
	setOutputCol("embeddings")

// ENTRENAMIENTO
val nerTagger =  new NerDLApproach().

	// CONFIGURACION TENSORFLOW
	// setConfigProtoBytes(bytes). // ConfigProto from tensorflow, serialized into byte array
	// setGraphFolder(path). // Folder path that contain external graph files
    setRandomSeed(0). // Random seed
    setMinEpochs(1). // Minimum number of epochs to train
    setMaxEpochs(1). // Maximum number of epochs to train
    setBatchSize(32). // Batch size

    // ENTRENAMIENTO TENSORFLOW
    setLr(1e-3f). // Learning Rate
    setPo(0.005f). // Learning rate decay coefficient. Real Learning Rage = lr / (1 + po * epoch)
    // setDropout(5e-1f). // Dropout coefficient
	setGraphFolder("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/tensorflow").

	// VALIDACIONES
	// setValidationSplit(validationSplit). //Choose the proportion of training dataset to be validated against the model on each Epoch. The value should be between 0.0 and 1.0 and by default it is 0.0 and off.
	setIncludeConfidence(true). // whether to include confidence scores in annotation metadata
	// setTestDataset("tmvar.test"). // Path to test dataset. If set used to calculate statistic on it during training.
	setTestDataset("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/CONLL-pubtator.tmp.parquet").

	// MEDIDAS
	setEnableOutputLogs(true). // Whether to output to annotators log folder
	setEvaluationLogExtended(true). // Whether logs for validation to be extended: it displays time and evaluation of each label. Default is false.
	setIncludeConfidence(true). // whether to include confidence scores in annotation metadata"

	// CONFIGURACI�N NERDLMODEL
	// setUseContrib(false) // whether to use contrib LSTM Cells. Not compatible with Windows
	// setVerbose(2). // Level of verbosity during training
	setEntities(Array("MUT-DNA", "MUT-PRO", "MUT_SNP")). // Entities to recognize
	setInputCols(Array("sentence", "token", "embeddings")).
	setOutputCol("ner").
	setLabelColumn("label") // Column with label per each token

// MODELO OK, APLICACI�N SOBRE EL CONJUNTO DE TEST
val converter = new NerConverter().
	setInputCols("sentence", "token", "ner").
	setOutputCol("chunk")

val finisher = new Finisher().
    setInputCols("document", "sentence", "token", "pos", "embeddings", "ner", "chunk", "label").
    setIncludeMetadata(true).
    setCleanAnnotations(false)

import spark.implicits._
val emptyData = spark.emptyDataset[String].toDF("text")
val stages = Array(
	embeddings,
    nerTagger,
    converter,
    finisher
)

val trainPipeline = new RecursivePipeline().setStages(stages)

println(java.time.LocalTime.now + ": NER-TRAIN: begin train")

val trainModel = trainPipeline.fit(preparedTrainDataset(0))

println(java.time.LocalTime.now + ": NER-TRAIN: begin trained")

val ner = trainModel.
    stages.
    filter(s => s.isInstanceOf[NerDLModel]).
    head.
    asInstanceOf[NerDLModel]

println(java.time.LocalTime.now + ": NER-TRAIN: located")

ner.  
    write.
    overwrite.
    save("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/tfm_ner_pubtator")

println(java.time.LocalTime.now + ": NER-TRAIN: saved")

val trainedTrainData = trainModel.transform(preparedTrainDataset)
val trainedTestData = trainModel.transform(preparedTestDataset)

// Nos hace falta un grafo para con mas tags al incluir B-, I-, E- para cada mutacion 4
// Tenemos 14 4x3 + O + "" 
val trainLabels = NerTagged.collectTrainingInstances(trainedTrainData.toDF(), Array("sentence", "token", "embeddings"), "label")
val labels = trainLabels.flatMap(r => r._1.labels).distinct
println(labels.length)

// Tenemos 768 dimensiones (procede del modelo BERT), ahora 1024
val trainSentences = trainLabels.map(r => r._2)
val embeddingsDim = nerTagger.calculateEmbeddingsDim(trainSentences)
println(embeddingsDim)

// Tenemos 88 caracteres diferentes, ahora 62
val chars = trainLabels.flatMap(r => r._2.tokens.flatMap(token => token.token.toCharArray)).distinct
println(chars.length)

// ESTAD�STICAS DEL CONJUNTO DE TEST
val labeledTrain = NerTagged.
	collectTrainingInstances(
		trainedTrainData, 
		Seq("sentence", "token", "embeddings"), 
		"label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel labeled")
ner.getModelIfNotSet.measure(labeledTrain, (s: String) => System.out.println(s), true, 0)
	

// ESTAD�STICAS DEL CONJUNTO DE TEST
val labeledTest = NerTagged.
	collectTrainingInstances(
		trainedTestData, 
		Seq("sentence", "token", "embeddings"), 
		"label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel labeled")
ner.getModelIfNotSet.measure(labeledTest, (s: String) => System.out.println(s), true, 0)


// DATOS
val actualListOfNamedEntitiesMap = trainedTrainData.
	select("finished_ner").
	collectAsList().
	toArray.
	map(x=>x.toString.drop(1).dropRight(1).split("@")).
	map(keyValuePair=>keyValuePair.
		map(x=>(x.split("->").lastOption.get,x.slice(x.indexOf("->")+2,x.indexOf("#")))).
		filter(!_._1.equals("O")).
		groupBy(_._1).
		mapValues(_.map(_._2).toList))


val length=actualListOfNamedEntitiesMap.length
for(index<-0 until length){
	println("Keys present in actualOutputMap but not in actualOutputMap:  %s".format(actualListOfNamedEntitiesMap(index)))
}
	
labeledTrain(0)._1    
trainedTrainData.limit(1).select("text", "finished_ner").foreach { row => { 
	row.toSeq.foreach { col => println(col) } 
} }

labeledTest(0)._1    
trainedTestData.limit(1).select("text", "finished_ner").foreach { row => { 
	row.toSeq.foreach { col => println(col) } 
} }


trainedTrainData.limit(1).select("finished_token", "finished_ner").foreach { row => { 
	val a = Array(
		row.getSeq[String](0).toArray, 
		row.getSeq[String](1).toArray
	)
	val columns = a(0) zip a(1)
	columns.foreach{ col => println(col) } 
}}

trainedTestData.limit(1).select("finished_token", "finished_ner").foreach { row => { 
	val a = Array(
		row.getSeq[String](0).toArray, 
		row.getSeq[String](1).toArray
	)
	val columns = a(0) zip a(1)
	columns.foreach{ col => println(col) } 
} }
































	
// MODELO OK, APLICACI�N SOBRE EL CONJUNTO DE TEST
val converter = new NerConverter().
	setInputCols("sentence", "token", "ner").
	setOutputCol("chunk")

val finisher = new Finisher().
	setInputCols("sentence", "token", "ner", "chunk").
	setIncludeMetadata(true).
  setCleanAnnotations(false)
//  .
//  setOutputAsArray(false).
//  setValueSplitSymbol(";")


// TRAIN
var nerTrainData = ner.transform(preparedTrainData)
var converterTrainData = converter.transform(nerTrainData)
var finisherTrainData = finisher.transform(converterTrainData)

finisherTrainData.show


// ESTAD�STICAS DEL CONJUNTO DE TEST
val labeledTrain = NerTagged.
  collectTrainingInstances(
    finisherTrainData, 
    Seq("sentence", "token", "embeddings"), 
    "label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel labeled")
ner.getModelIfNotSet.measure(labeledTrain, (s: String) => System.out.println(s), true, 0)
	

// TEST
var nerTestData = ner.transform(preparedTestData)
var converterTestData = converter.transform(nerTestData)
var finisherTestData = finisher.transform(converterTestData)

finisherTestData.show


// ESTAD�STICAS DEL CONJUNTO DE TEST
val labeledTest = NerTagged.
  collectTrainingInstances(
    finisherTestData, 
    Seq("sentence", "token", "embeddings"), 
    "label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel labeled")
ner.getModelIfNotSet.measure(labeledTest, (s: String) => System.out.println(s), true, 0)


// DATOS
	
labeledTrain(0)._1    
finisherTrainData.limit(1).select("text", "finished_ner").foreach { row => { 
  row.toSeq.foreach { col => println(col) } 
} }

labeledTest(0)._1    
finisherTestData.limit(1).select("text", "finished_ner").foreach { row => { 
  row.toSeq.foreach { col => println(col) } 
} }
















val document = new DocumentAssembler().
	setInputCol("text").
	setOutputCol("document").
	setCleanupMode("shrink")

val sentences = new SentenceDetector().
	setInputCols(Array("document")).
	setOutputCol("sentence")

val token = new Tokenizer().
	setInputCols(Array("sentence")).
	setOutputCol("token")

val pos = PerceptronModel.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/pos/pos_anc_en_2.0.2_2.4_1556659930154").
	setInputCols(Array("sentence", "token")).
	setOutputCol("pos")














import spark.implicits._
val emptyData = spark.emptyDataset[String].toDF("text")
val stages = Array(
		document,
		sentences,
		token,
		pos,
		embeddings
)

val preparedPipeline = new RecursivePipeline().setStages(stages)
val preparedModel = preparedPipeline.fit(emptyData)
val preparedTrainData = preparedModel.transform(trainData)
val preparedTestData = preparedModel.transform(testData)


//COSAS DEL TRAIN
val preparedDataset = preparedTrainData.randomSplit(Array(0.85, 0.15), seed = 11L)
preparedDataset(1).write.mode("overwrite").parquet("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/CONLL-test.tmp.parquet")



// Nos hace falta un grafo para con mas tags al incluir B-, I-, E- para cada mutacion 4
// Tenemos 14 4x3 + O + "" 
val trainLabels = NerTagged.collectTrainingInstances(preparedTrainData.toDF(), Array("sentence", "token", "embeddings"), "label")
val labels = trainLabels.flatMap(r => r._1.labels).distinct

// Tenemos 768 dimensiones (procede del modelo BERT), ahora 1024
val trainSentences = trainLabels.map(r => r._2)
val embeddingsDim = nerTagger.calculateEmbeddingsDim(trainSentences)

// Tenemos 88 caracteres diferentes, ahora 62
val chars = trainLabels.flatMap(r => r._2.tokens.flatMap(token => token.token.toCharArray)).distinct
	
	
// MODELO OK, APLICACI�N SOBRE EL CONJUNTO DE TEST
val converter = new NerConverter().
	setInputCols("sentence", "token", "ner").
	setOutputCol("chunk")

val finisher = new Finisher().
	setInputCols("sentence", "token", "ner", "chunk").
	setIncludeMetadata(true).
  setCleanAnnotations(false)
//  .
//  setOutputAsArray(false).
//  setValueSplitSymbol(";")


// TRAIN
var nerTrainData = ner.transform(preparedTrainData)
var converterTrainData = converter.transform(nerTrainData)
var finisherTrainData = finisher.transform(converterTrainData)

finisherTrainData.show


// ESTAD�STICAS DEL CONJUNTO DE TEST
val labeledTrain = NerTagged.
  collectTrainingInstances(
    finisherTrainData, 
    Seq("sentence", "token", "embeddings"), 
    "label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel labeled")
ner.getModelIfNotSet.measure(labeledTrain, (s: String) => System.out.println(s), true, 0)
	

// TEST
var nerTestData = ner.transform(preparedTestData)
var converterTestData = converter.transform(nerTestData)
var finisherTestData = finisher.transform(converterTestData)

finisherTestData.show


// ESTAD�STICAS DEL CONJUNTO DE TEST
val labeledTest = NerTagged.
  collectTrainingInstances(
    finisherTestData, 
    Seq("sentence", "token", "embeddings"), 
    "label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel labeled")
ner.getModelIfNotSet.measure(labeledTest, (s: String) => System.out.println(s), true, 0)


// DATOS
	
labeledTrain(0)._1    
finisherTrainData.limit(1).select("text", "finished_ner").foreach { row => { 
  row.toSeq.foreach { col => println(col) } 
} }

labeledTest(0)._1    
finisherTestData.limit(1).select("text", "finished_ner").foreach { row => { 
  row.toSeq.foreach { col => println(col) } 
} }


/*
val actualListOfNamedEntitiesMap=result.select("finished_ner").collectAsList().toArray
        .map(x=>x.toString.drop(1).dropRight(1).split("@")).map(keyValuePair=>keyValuePair
        .map(x=>(x.split("->").lastOption.get,x.slice(x.indexOf("->")+2,x.indexOf("#")))).filter(!_._1.equals("O"))
        .groupBy(_._1).mapValues(_.map(_._2).toList))


        val length=actualListOfNamedEntitiesMap.length
        for(index<-0until length){
        println("Keys present in actualOutputMap but not in actualOutputMap:  %s".format(actualListOfNamedEntitiesMap(index)))
        }
 */
var finisherTrainData = finisher.transform(converterTrainData)

finisherTrainData.show


// ESTAD�STICAS DEL CONJUNTO DE TEST
val labeledTrain = NerTagged.
  collectTrainingInstances(
    finisherTrainData, 
    Seq("sentence", "token", "embeddings"), 
    "label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel labeled")
ner.getModelIfNotSet.measure(labeledTrain, (s: String) => System.out.println(s), true, 0)
	

// TEST
var nerTestData = ner.transform(preparedTestData)
var converterTestData = converter.transform(nerTestData)
var finisherTestData = finisher.transform(converterTestData)

finisherTestData.show


// ESTAD�STICAS DEL CONJUNTO DE TEST
val labeledTest = NerTagged.
  collectTrainingInstances(
    finisherTestData, 
    Seq("sentence", "token", "embeddings"), 
    "label")

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel labeled")
ner.getModelIfNotSet.measure(labeledTest, (s: String) => System.out.println(s), true, 0)


// DATOS
	
labeledTrain(0)._1    
finisherTrainData.limit(1).select("text", "finished_ner").foreach { row => { 
  row.toSeq.foreach { col => println(col) } 
} }

labeledTest(0)._1    
finisherTestData.limit(1).select("text", "finished_ner").foreach { row => { 
  row.toSeq.foreach { col => println(col) } 
} }


/*
val actualListOfNamedEntitiesMap=result.select("finished_ner").collectAsList().toArray
        .map(x=>x.toString.drop(1).dropRight(1).split("@")).map(keyValuePair=>keyValuePair
        .map(x=>(x.split("->").lastOption.get,x.slice(x.indexOf("->")+2,x.indexOf("#")))).filter(!_._1.equals("O"))
        .groupBy(_._1).mapValues(_.map(_._2).toList))


        val length=actualListOfNamedEntitiesMap.length
        for(index<-0until length){
        println("Keys present in actualOutputMap but not in actualOutputMap:  %s".format(actualListOfNamedEntitiesMap(index)))
        }
 */