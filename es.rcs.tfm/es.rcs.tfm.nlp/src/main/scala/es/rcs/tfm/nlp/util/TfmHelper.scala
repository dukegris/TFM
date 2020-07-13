package es.rcs.tfm.nlp.util

import com.johnsnowlabs.nlp.{DocumentAssembler, Finisher}
import com.johnsnowlabs.nlp.annotators.{Tokenizer}
import com.johnsnowlabs.nlp.annotators.ner.{NerConverter, Verbose}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach}
import com.johnsnowlabs.nlp.annotators.ner.crf.{NerCrfModel, NerCrfApproach}
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.embeddings.{BertEmbeddings, AlbertEmbeddings}
import com.johnsnowlabs.nlp.training.CoNLL
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ReadAs, ResourceHelper}

import org.apache.spark.sql.{Dataset}
import java.io.{File}

import es.rcs.tfm.nlp.model.TfmType

object TfmHelper {

	val DEBUG = false

	val nerReader = CoNLL(explodeSentences = false)


	def prepareData (
			nerReader: CoNLL,
			fileName: String): Dataset[_] = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareData " + fileName)

		val file = ExternalResource(
				fileName,
				ReadAs.TEXT,
				Map.empty[String, String])

		val data = nerReader.
				readDataset(
				ResourceHelper.spark,
				file.path)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareData " + fileName)

		data

	}


	def prepareDocument (): DocumentAssembler = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareDocument ")

		val document = new DocumentAssembler().
				//setCleanupMode("shrink") // disabled, inplace, inplace_full, shrink, shrink_full
				setInputCol(TfmType.TEXT).
				setOutputCol(TfmType.DOCUMENT)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareDocument ")

		document

	}


	def prepareSentence (
			maxSentenceLength: Integer = 512): SentenceDetector = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareSentence ")

		val sentence = new SentenceDetector().
				setMaxLength(maxSentenceLength).
				setInputCols(Array(TfmType.DOCUMENT)).
				setOutputCol(TfmType.SENTENCES)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareSentence ")

		sentence

	}


	def prepareToken (): Tokenizer = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareToken ")

		val token = new Tokenizer().
				setInputCols(Array(TfmType.SENTENCES)).
				setOutputCol(TfmType.TOKEN)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareToken ")

		token

	}


	def preparePos (
			modelDirectory: String): PerceptronModel = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN preparePos " + modelDirectory)

		val pos = PerceptronModel.
				load(modelDirectory).
				setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN)).
				setOutputCol(TfmType.POS)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   preparePos " + modelDirectory)

		pos

	}


	def prepareBert (
			modelDirectory: String,
			maxSentenceLength: Integer = 512,
			dimension: Integer = 768,
			batchSize: Integer = 32,
			caseSensitive: Boolean = false,
			poolingLayer: Integer = -1): BertEmbeddings = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareBert " + modelDirectory)

		val embeddings = BertEmbeddings.
				load(modelDirectory).
				setDimension(dimension).
				setMaxSentenceLength(maxSentenceLength).
				setBatchSize(batchSize).
				setCaseSensitive(caseSensitive).
				setPoolingLayer(poolingLayer).
				setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN)).
				setOutputCol(TfmType.WORD_EMBEDDINGS)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareBert " + modelDirectory)

		embeddings

	}


	def prepareAlbert (
			modelDirectory: String,
			maxSentenceLength: Integer = 512,
			dimension: Integer = 768,
			batchSize: Integer = 32,
			caseSensitive: Boolean = false,
			poolingLayer: Integer = -1): AlbertEmbeddings = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareAlbert " + modelDirectory)

		val embeddings = AlbertEmbeddings.
				load(modelDirectory).
				setDimension(dimension).
				setMaxSentenceLength(maxSentenceLength).
				setBatchSize(batchSize).
				setCaseSensitive(caseSensitive).
				setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN)).
				setOutputCol(TfmType.WORD_EMBEDDINGS)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareAlbert " + modelDirectory)

		embeddings

	}


	def prepareNerCrfApproach (
			minEpochs: Int = 1,
			maxEpochs: Int = 100,
			l2: Float = 1f,
			lossEps: Double = 1e-3,
			c0: Int = 2250000,
			minW: Double = 0.0): NerCrfApproach = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareNerDL")

		val nerTagger =  new NerCrfApproach().

				// CONFIGURACION TENSORFLOW
				// setConfigProtoBytes(bytes). // ConfigProto from tensorflow, serialized into byte array
				// setGraphFolder(path). // Folder path that contain external graph files
				setRandomSeed(0). // Random seed
				setMinEpochs(minEpochs). // Minimum number of epochs to train
				setMaxEpochs(maxEpochs). // Maximum number of epochs to train
				//setBatchSize(32). // Batch size

				// ENTRENAMIENTO CRF
				setL2(l2). // L2 regularization coefficient for CRF
				setC0(c0). // c0 defines decay speed for gradient
				setLossEps(lossEps). // If epoch relative improvement lass than this value, training is stopped
				setMinW(minW). // Features with less weights than this value will be filtered out

				// VALIDACIONES
				//setDicts("").
				//setExternalFeatures(path, delimiter, readAs, options): Path to file or folder of line separated file that has something like this: Volvo:ORG with such delimiter, readAs LINE_BY_LINE or SPARK_DATASET with options passed to the latter.
				// MEDIDAS
				// setEnableOutputLogs(true). // Whether to output to annotators log folder
				// setEvaluationLogExtended(true). // Whether logs for validation to be extended: it displays time and evaluation of each label. Default is false.
				setIncludeConfidence(true). // whether to include confidence scores in annotation metadata

				// CONFIGURACION NERDLMODEL
				// setUseContrib(false) // whether to use contrib LSTM Cells. Not compatible with Windows
				// setVerbose(2). // Level of verbosity during training
				//setEntities(Array("MUT-DNA", "MUT-PRO", "MUT_SNP")). // Entities to recognize
				setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.POS, TfmType.WORD_EMBEDDINGS)).
				setOutputCol(TfmType.NAMED_ENTITY).
				setLabelColumn(TfmType.LABEL) // Column with label per each token

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareNerDL")

		nerTagger

	}


	def prepareNerDLApproach (
			testDataset: String,
			tfGraphDirectory: String,
			minEpochs: Int = 1,
			maxEpochs: Int = 1,
			lr: Float = 1e-3f,
			po: Float = 5e-3f,
			dropout: Float = 0.5f,
			validationSplit: Float = 0.20f,
			batchSize: Int = 512): NerDLApproach = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareNerDL")

		val nerTagger = new NerDLApproach().

				// CONFIGURACION TENSORFLOW
				// setConfigProtoBytes(bytes). // ConfigProto from tensorflow, serialized into byte array
				// setGraphFolder(path). // Folder path that contain external graph files
				setRandomSeed(0). // Random seed
				setMinEpochs(minEpochs). // Minimum number of epochs to train
				setMaxEpochs(maxEpochs). // Maximum number of epochs to train
				setBatchSize(batchSize). // Batch size

				// ENTRENAMIENTO TENSORFLOW
				setLr(lr). // Learning Rate
				setPo(po). // Learning rate decay coefficient. Real Learning Rage = lr / (1 + po * epoch)
				setDropout(dropout). // Dropout coefficient

				// VALIDACIONES
				// setValidationSplit(validationSplit). //Choose the proportion of training dataset to be validated against the model on each Epoch. The value should be between 0.0 and 1.0 and by default it is 0.0 and off.
				setValidationSplit(validationSplit). //Si no hay conjunto de test, divide el de entrenamiento
				// setTestDataset("tmvar.test"). // Path to test dataset. If set used to calculate statistic on it during training.
				//setTestDataset("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/" + testDataSet).

				// MEDIDAS
				setEnableOutputLogs(true). // Whether to output to annotators log folder
				setEvaluationLogExtended(true). // Whether logs for validation to be extended: it displays time and evaluation of each label. Default is false.
				// TODO Ponemos a false por un BUG https://github.com/JohnSnowLabs/spark-nlp/issues/918
				setIncludeConfidence(false). // whether to include confidence scores in annotation metadata"

				// CONFIGURACION NERDLMODEL
				// setUseContrib(false) // whether to use contrib LSTM Cells. Not compatible with Windows
				setVerbose(Verbose.PerStep). // Level of verbosity during training
				//setEntities(Array("MUT-DNA", "MUT-PRO", "MUT_SNP")). // Entities to recognize
				setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS)).
				setOutputCol(TfmType.NAMED_ENTITY).
				setLabelColumn(TfmType.LABEL) // Column with label per each token

		if (tfGraphDirectory != null) nerTagger.setGraphFolder(tfGraphDirectory)
		if (testDataset != null) nerTagger.setTestDataset(testDataset)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareNerDL")

		nerTagger

	}


	def prepareNer (
			modelDirectory: String) : NerDLModel = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareNer " + modelDirectory)

		val ner = NerDLModel.
				//pretrained(TfmType.PRETRAINED_NER_BERT).
				load(modelDirectory).
				setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS)).
				setOutputCol(TfmType.NAMED_ENTITY)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareNer " + modelDirectory)

		ner

	}


	def prepareNerConverter (): NerConverter = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareNerConverter ")

		val converterNer = new NerConverter().
				setInputCols(TfmType.SENTENCES, TfmType.TOKEN, TfmType.NAMED_ENTITY). // BUG
				setOutputCol(TfmType.CONVERTER_NAMED_ENTITY)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareNerConverter ")

		converterNer

	}


	def prepareLabelConverter (): NerConverter = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareLabelConverter ")

		val converterLabel = new NerConverter().
				setInputCols(TfmType.SENTENCES, TfmType.TOKEN, TfmType.LABEL). // BUG
				setOutputCol(TfmType.CONVERTER_LABEL)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareLabelConverter ")

		converterLabel

	}


	def prepareProductionFinisher (): Finisher = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareProductionFinisher ")

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
						TfmType.CONVERTER_NAMED_ENTITY)).
				setIncludeMetadata(true).
				setCleanAnnotations(false)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareProductionFinisher ")

		finisher

	}


	def productionPipelineStages(
			posModelDirectory: String,
			bertModelDirectory: String,
			nerModelDirectory: String,
			maxSentenceLength: Integer = 512,
			bertDimension: Integer = 768,
			bertBatchSize: Integer = 32,
			bertCaseSensitive: Boolean = false,
			bertPoolingLayer: Integer = -1) = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN productionPipelineStages ")

		val stages = Array(
				prepareDocument(),
				prepareSentence(
						maxSentenceLength),
				prepareToken(),
				// stemmer,
				// normalizer,
				preparePos(
						posModelDirectory),
				prepareBert(
						bertModelDirectory,
						maxSentenceLength,
						bertDimension,
						bertBatchSize,
						bertCaseSensitive,
						bertPoolingLayer),
				prepareNer(
				    nerModelDirectory),
				prepareNerConverter(),
				prepareProductionFinisher())

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   productionPipelineStages ")

		stages

	}


	def prepareTrainFinisher (): Finisher = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareTrainFinisher ")

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

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareTrainFinisher ")

		finisher

	}


	def trainPipelineFromStages(
			conllTrainDirectory: String,
			bertModelDirectory: String,
			nerTfGraphDirectory: String,
			bertMaxSentenceLength: Integer = 512,
			bertDimension: Integer = 768,
			bertBatchSize: Integer = 32,
			bertCaseSensitive: Boolean = false,
			bertPoolingLayer: Integer = -1,
			nerTfMinEpochs: Int = 1,
			nerTfMaxEpochs: Int = 1,
			nerTfLr: Float = 1e-3f,
			nerTfPo: Float = 5e-3f,
			nerTfDropOut: Float = 0.5f,
			nerTfValidationSplit: Float = 0.20f,
			nerTfBatchSize: Integer = 32) = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN trainPipelineStages ")

		val d = new File(conllTrainDirectory)
		if (d.exists && d.isDirectory) {

			val embeddings = TfmHelper.prepareBert(
					bertModelDirectory,
					bertMaxSentenceLength,
					bertDimension,
					bertBatchSize,
					bertCaseSensitive,
					bertPoolingLayer)

			var trainData: Dataset[_] = d.
					listFiles.
					filter(_.isFile).
					filter(_.getName().toUpperCase().endsWith("CONLL")).
					map(f => prepareData(nerReader, f.getAbsolutePath()).asInstanceOf[Dataset[Any]]).
					reduce(_ union _)

			trainData.coalesce(1)

			val trainDataEmbeddings = embeddings.transform(trainData)

			val nerTagger = TfmHelper.prepareNerDLApproach(
					null,
					nerTfGraphDirectory,
					nerTfMinEpochs,
					nerTfMaxEpochs,
					nerTfLr,
					nerTfPo,
					nerTfDropOut,
					nerTfValidationSplit,
					nerTfBatchSize)

			val nerTrained = nerTagger.fit(trainDataEmbeddings)

			val stages = Array(
					nerTrained,
					prepareNerConverter(),
					prepareLabelConverter(),
					prepareTrainFinisher())

		} else {

			if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: ERROR trainPipelineStages ")

		}

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-HELPER: END   trainPipelineStages ")

	}

}