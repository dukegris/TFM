package es.rcs.tfm.nlp.service

import org.apache.spark.{SparkContext}
import org.apache.spark.sql.{SparkSession}

class NerTrain(
		sc: SparkContext,
		spark: SparkSession,
		bertModelDirectory: String,
		nerTfGraphDirectory: String,
		bertMaxSentenceLength: Integer = 512,
		bertDimension: Integer = 768,
		bertCaseSensitive: Boolean = false,
		bertBatchSize: Integer = 32,
		nerTfMinEpochs: Int = 1,
		nerTfMaxEpochs: Int = 1,
		nerTfLr: Float = 1e-3f,
		nerTfPo: Float = 5e-3f,
		nerTfDropOut: Float = 0.5f,
		nerTfValidationSplit: Float = 0.20f,
		nerTfBatchSize: Integer = 32) {

	def train(
			conllTrainDirectory: String,
			nerModelDirectory: String,
			pipelineModelDirectory: String) = {

	}

/*
			trainDirectory: String,
			modelDirectory: String,
			pipelineModelDirectory: String,
			predictionsCsvFileName: String): PipelineModel = {



def measureDL(
ner: NerDLModel,
data: Dataset[_]
): Unit = {

if (DEBUG) println(java.time.LocalTime.now + ": TFM-MEASURE: BEGIN measureDL")

val model = prepareMeasureNerPipeline(ner, data)
val result = model.transform(data)

if (DEBUG) println(java.time.LocalTime.now + ": TFM-MEASURE: END   measureDL")

measure(result, 10)

// ESTADISTICAS DEL CONJUNTO DE ENTRENAMIENTO
val labeledData = NerTagged.
collectTrainingInstances(
result,
Seq(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS),
TfmType.LABEL)

if (DEBUG) println(java.time.LocalTime.now + ": TFM-MEASURE: measureNerModel")

ner.getModelIfNotSet.measure(labeledData, (s: String) => System.out.println(s), true, 0)

if (DEBUG) println(java.time.LocalTime.now + ": TFM-MEASURE: measureNerModel")

}


def measureCRF(
ner: NerCrfModel,
data: Dataset[_]
): Unit = {

if (DEBUG) println(java.time.LocalTime.now + ": TFM-MEASURE: BEGIN measureCRF")

val model = prepareMeasureNerPipeline(ner, data)
val result = model.transform(data)

if (DEBUG) println(java.time.LocalTime.now + ": TFM-MEASURE: END   measureCRF")

measure(result, 10)
}

def prepareMeasureNerPipeline (
nerTagger: AnnotatorModel[_],
data: Dataset[_]
) : PipelineModel = {

if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareMeasureNerPipeline")

val converterLabel = new NerConverter().
setInputCols(TfmType.SENTENCES, TfmType.TOKEN, TfmType.LABEL).
setOutputCol(TfmType.CONVERTER_LABEL)

val converterNer = new NerConverter().
setInputCols(TfmType.SENTENCES, TfmType.TOKEN, TfmType.NAMED_ENTITY). // BUG
setOutputCol(TfmType.CONVERTER_NAMED_ENTITY)

val finisher = new Finisher().
setInputCols(
TfmType.DOCUMENT,
TfmType.SENTENCES,
TfmType.TOKEN,
TfmType.POS,
TfmType.WORD_EMBEDDINGS,
TfmType.NAMED_ENTITY,
TfmType.CONVERTER_NAMED_ENTITY,
TfmType.LABEL,
TfmType.CONVERTER_LABEL).
setIncludeMetadata(true).
setCleanAnnotations(false)

//import spark.implicits._
import data.sparkSession.implicits._ // for row casting
val stages = Array(
nerTagger,
converterLabel,
converterNer,
finisher
)

val trainPipeline = new RecursivePipeline().setStages(stages)
val model = trainPipeline.fit(data)

if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareMeasureNerPipeline")

model

}

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerTraining")
val nerReader = CoNLL()
val trainFile = ExternalResource(trainFileName, ReadAs.TEXT, Map.empty[String, String])
val testFile = ExternalResource(testFileName, ReadAs.TEXT, Map.empty[String, String])
val trainDataset = nerReader.readDataset(spark, trainFile.path)
val testDataset = nerReader.readDataset(spark, testFile.path)

println(java.time.LocalTime.now + s": NER-TRAIN: Lectura en ${(System.nanoTime() - System.nanoTime())/1e9}\n")
val pipelineModel = trainNerModel(nerReader, trainDataset, testDataset)

// Guardar el pipeline
pipelineModel.write.overwrite().save(pipelineModelDirectory)

// Guardar el modelo
TfmHelper.saveDLModel(pipelineModel, modelDirectory)

// Medir el entrenamiento del modelo generado sobre los conjuntos de train y de test
val trainTransformed = measureNerModel(pipelineModel, trainDataset, predictionsCsvFileName + "_train.csv", false)
val testTransformed = measureNerModel(pipelineModel, testDataset, predictionsCsvFileName + "_test.csv", false)

// Medir el entrenamiento del modelo NER guardado
TfmHelper.measure(trainTransformed, 10)
TfmHelper.measure(testTransformed, 10)

val dir = new File("./CONLL-test.tmp.parquet")
dir.listFiles.foreach(f => {
f.delete
})
dir.delete

pipelineModel

}


def trainNerModel(
nerReader: CoNLL,
trainDataset: Dataset[_],
testDataset: Dataset[_]): PipelineModel = {

println(java.time.LocalTime.now + ": NER-TRAIN: trainNerModel")

// TODO En vez de testdata se debiera repartir el traindata en un 85-15
val preparedDataset = trainDataset.randomSplit(Array(0.85, 0.15), seed = 11L)
val tmpParquetDir = preparedDataset(1).write.mode("overwrite").format("parquet").save("./CONLL-test.tmp.parquet")
val stages = createPipelineStagesDl("./CONLL-test.tmp.parquet")

val pipeline = new RecursivePipeline().setStages(stages)
val model = pipeline.fit(preparedDataset(0))

model

}

def measureNerModel(
model: PipelineModel,
df: Dataset[_],
csvFile: String,
extended: Boolean = true,
errorsToPrint: Int = 0): DataFrame = {

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel")
val nerTF = model.
stages.
filter(s => s.isInstanceOf[NerDLModel]).
head.
asInstanceOf[NerDLModel].
getModelIfNotSet

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel readed")
val transformed = model.transform(df)

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel collect")
val annotation = Annotation.collect(transformed, TfmType.CONVERTER_NAMED_ENTITY)
TfmHelper.saveNerSpanTags(annotation, csvFile)

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel transformed")
val labeled = NerTagged.
collectTrainingInstances(
transformed,
Seq(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS), TfmType.LABEL)

println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel labeled")
nerTF.measure(labeled, (s: String) => System.out.println(s), extended, errorsToPrint)

transformed

}
*/
/*

def prepareMeasureNerPipeline (
nerTagger: AnnotatorModel[_],
data: Dataset[_]
) : PipelineModel = {

if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareMeasureNerPipeline")

val converterLabel = prepareLabelConverter()
val converterNer = prepareNerConverter()
val finisher = prepareTrainFinisher()

//import spark.implicits._
import data.sparkSession.implicits._ // for row casting
val stages = Array(
nerTagger,
converterLabel,
converterNer,
finisher
)

val trainPipeline = new RecursivePipeline().setStages(stages)
val model = trainPipeline.fit(data)

if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareMeasureNerPipeline")

model

}
*/
/*
def createPipelineStagesDl(
testDataset: String) = {

println(java.time.LocalTime.now + ": NER-TRAIN: createPipelineStagesDl")
val document = new DocumentAssembler().
setInputCol(TfmType.TEXT).
setOutputCol(TfmType.DOCUMENT).
setCleanupMode("shrink")

val sentence = new SentenceDetector().
setInputCols(Array(TfmType.DOCUMENT)).
setOutputCol(TfmType.SENTENCES).
setMaxLength(maxSentenceLength)

val token = new Tokenizer().
setInputCols(Array(TfmType.SENTENCES)).
setOutputCol(TfmType.TOKEN)

val pos = PerceptronModel.
load(this.posModelDirectory).
setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN)).
setOutputCol(TfmType.POS)

val embeddings = BertEmbeddings.
load(this.bertModelDirectory).
setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN)).
setOutputCol(TfmType.WORD_EMBEDDINGS).
setMaxSentenceLength(maxSentenceLength).
setDimension(dimension).
setCaseSensitive(caseSensitive).
setBatchSize(batchSize)

val ner =  new NerDLApproach().

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
setGraphFolder(tensorflowModelDirectory).

// VALIDACIONES
// setValidationSplit(validationSplit). //Choose the proportion of training dataset to be validated against the model on each Epoch. The value should be between 0.0 and 1.0 and by default it is 0.0 and off.
setIncludeConfidence(true). // whether to include confidence scores in annotation metadata
// setTestDataset("tmvar.test"). // Path to test dataset. If set used to calculate statistic on it during training.
setTestDataset(testDataset).

// MEDIDAS
setEnableOutputLogs(true). // Whether to output to annotators log folder
setEvaluationLogExtended(true). // Whether logs for validation to be extended: it displays time and evaluation of each label. Default is false.
setIncludeConfidence(true). // whether to include confidence scores in annotation metadata"

// CONFIGURACION NERDLMODEL
// setUseContrib(false) // whether to use contrib LSTM Cells. Not compatible with Windows
// setVerbose(2). // Level of verbosity during training
setEntities(entities). // Entities to recognize
setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS)).
setOutputCol(TfmType.NAMED_ENTITY).
setLabelColumn(TfmType.LABEL) // Column with label per each token

val converter = new NerConverter().
setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.NAMED_ENTITY)).
setOutputCol(TfmType.CONVERTER_NAMED_ENTITY)

val labelConverter = new NerConverter()
.setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.LABEL))
.setOutputCol(TfmType.CONVERTER_LABEL)

val finisher = new Finisher().
setInputCols(Array(
TfmType.DOCUMENT,
TfmType.SENTENCES,
TfmType.TOKEN,
//TfmType.STEM,
//TfmType.NORMAL,
TfmType.POS,
TfmType.WORD_EMBEDDINGS,
TfmType.NAMED_ENTITY,
TfmType.CONVERTER_NAMED_ENTITY,
TfmType.LABEL,
TfmType.CONVERTER_LABEL)).
setIncludeMetadata(true).
setCleanAnnotations(false)

Array(
document,
sentence,
token,
pos,
embeddings,
ner,
converter,
labelConverter
)

}
*/
}