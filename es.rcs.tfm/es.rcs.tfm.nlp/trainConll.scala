// spark-shell --packages JohnSnowLabs:spark-nlp:2.3.4 --executor-memory=8g --executor-cores=24 --driver-memory=8g
// spark-shell --packages JohnSnowLabs:spark-nlp:2.3.4 --executor-memory=8g --executor-cores=6 --driver-memory=8g
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
    "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/ner_txt_train.conll", 
    ReadAs.LINE_BY_LINE, 
    Map.empty[String, String])

val testFile = ExternalResource(
    "/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/ner_txt_test.conll",
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
 
val embeddings = BertEmbeddings.
	load("file:///home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/bert_base_cased_en_2.2.0_2.4_1566671427398").
	setDimension(768).
	setMaxSentenceLength(512).
	setInputCols(Array("sentence", "token")).
	setOutputCol("embeddings")

import spark.implicits._
val emptyData = spark.emptyDataset[String].toDF("text")
val stages = Array(
		document,
		sentences,
		token,
		pos,
		embeddings
)
val preparedTrain = new RecursivePipeline().setStages(stages)
val preparedModel = preparedTrain.fit(emptyData)

val preparedTrainData = preparedModel.transform(trainData)
val preparedTestData = preparedModel.transform(testData)

preparedTestData.write.mode("overwrite").parquet("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/tmp_conll_validate")

 val nerTagger =  new NerDLApproach().

    // CONFIGURACION TENSORFLOW
    // setConfigProtoBytes(bytes). // ConfigProto from tensorflow, serialized into byte array
    // setGraphFolder(path). // Folder path that contain external graph files
    setRandomSeed(0). // Random seed
    setMinEpochs(10). // inimum number of epochs to train
    setMaxEpochs(100). // Maximum number of epochs to train
    setBatchSize(32). // Batch size

    // ENTRENAMIENTO TENSORFLOW
    setLr(0.1f). // Learning Rate
    setPo(0.01f). // Learning rate decay coefficient. Real Learning Rage = lr / (1 + po * epoch)
    // setDropout(5e-1f). // Dropout coefficient

    // VALIDACIONES
    // setValidationSplit(validationSplit). //Choose the proportion of training dataset to be validated against the model on each Epoch. The value should be between 0.0 and 1.0 and by default it is 0.0 and off.
    setIncludeConfidence(true). // whether to include confidence scores in annotation metadata
    // setTestDataset("tmvar.test"). // Path to test dataset. If set used to calculate statistic on it during training.
    setTestDataset("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner/tmp_conll_validate").
//    setTestDataset(preparedTestData).
    
    // MEDIDAS
    setEnableOutputLogs(true). // Whether to output to annotators log folder
    setEvaluationLogExtended(true). // Whether logs for validation to be extended: it displays time and evaluation of each label. Default is false.
    setIncludeConfidence(true). // whether to include confidence scores in annotation metadata"

    // CONFIGURACIÓN NERDLMODEL
    // setUseContrib(false) // whether to use contrib LSTM Cells. Not compatible with Windows
    // setVerbose(2). // Level of verbosity during training
    setEntities(Array("MUT-DNA", "MUT-PRO", "MUT_SNP")). // Entities to recognize
  	setInputCols(Array("sentence", "token", "embeddings")).
  	setOutputCol("ner").
    setLabelColumn("label") // Column with label per each token
    val chars = trainDataset.flatMap(r => r._2.tokens.flatMap(token => token.token.toCharArray)).distinct
    
// Nos hace falta un grafo para con mas tags al incluir B-, I-, E- para cada mutacion 4
// Tenemos 14 4x3 + O + "" 
val trainDataset = NerTagged.collectTrainingInstances(preparedTrainData.toDF(), Array("sentence", "token", "embeddings"), "label")
val labels = trainDataset.flatMap(r => r._1.labels).distinct

// Tenemos 768 dimensiones (procede del modelo BERT
val trainSentences = trainDataset.map(r => r._2)
val embeddingsDim = nerTagger.calculateEmbeddingsDim(trainSentences)

// Tenemos 88 caracteres diferentes
val chars = trainDataset.flatMap(r => r._2.tokens.flatMap(token => token.token.toCharArray)).distinct

// val labels = preparedTrainData.flatMap(r => r._1.labels).distinct
//  val labeled = NerTagged.collectTrainingInstances(transformed, Seq("sentence", "token", "glove"), "label")
val ner = nerTagger.fit(preparedTrainData)

val trainedNer = prepareTrain.
  stages.
  filter(s => s.isInstanceOf[NerDLModel]).
  head.
  asInstanceOf[NerDLModel].
  getModelIfNotSet
