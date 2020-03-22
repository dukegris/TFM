package es.rcs.tfm.nlp.service

import com.johnsnowlabs.nlp.training.CoNLL
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach}

import org.apache.spark.{SparkContext}
import org.apache.spark.sql.{SparkSession}

import es.rcs.tfm.nlp.model.TfmType
import es.rcs.tfm.nlp.util.{TfmHelper, TfmSave, TfmMeasure}

class NerTrain(
		sc: SparkContext,
		spark: SparkSession,
		bertModelDirectory: String,
		bertMaxSentenceLength: Integer = 512,
		bertDimension: Integer = 768,
		bertBatchSize: Integer = 32,
		bertCaseSensitive: Boolean = false,
		bertPoolingLayer: Integer = -1) {


	val embeddings = TfmHelper.prepareBert(
			bertModelDirectory,
			bertMaxSentenceLength,
			bertDimension,
			bertBatchSize,
			bertCaseSensitive,
			bertPoolingLayer)

			
	def trainTest(
			trainFileName: String,
			testFileName: String,
			nerModelDirectory: String,
			nerTfGraphDirectory: String,
			nerTfMinEpochs: Int = 1,
			nerTfMaxEpochs: Int = 1,
			nerTfLr: Float = 1e-3f,
			nerTfPo: Float = 5e-3f,
			nerTfDropOut: Float = 0.5f,
			nerTfValidationSplit: Float = 0.20f,
			nerTfBatchSize: Integer = 32) = {

		val trainingConll=embeddings.transform(CoNLL().readDataset(
			spark,
			trainFileName))

		val testConll=embeddings.transform(CoNLL().readDataset(
			spark,
			testFileName))

		TfmSave.saveParquetDataSet(
			testConll,
			testFileName + ".parquet"
		)

		val nerApproach = TfmHelper.prepareNerDLApproach(
			testFileName + ".parquet",
			nerTfGraphDirectory,
			nerTfMinEpochs,
			nerTfMaxEpochs,
			nerTfLr,
			nerTfPo,
			nerTfDropOut,
			nerTfValidationSplit,
			nerTfBatchSize)

		val nerTagger = nerApproach.
			fit(trainingConll)

		TfmSave.saveModel(
			nerTagger, 
			nerModelDirectory)

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

	}

	def train(
			conllTrainDirectory: String,
			nerModelDirectory: String,
			nerTfGraphDirectory: String,
			nerTfMinEpochs: Int = 1,
			nerTfMaxEpochs: Int = 1,
			nerTfLr: Float = 1e-3f,
			nerTfPo: Float = 5e-3f,
			nerTfDropOut: Float = 0.5f,
			nerTfValidationSplit: Float = 0.20f,
			nerTfBatchSize: Integer = 32) = {

		val nerApproach = TfmHelper.prepareNerDLApproach(
				null,
				nerTfGraphDirectory,
				nerTfMinEpochs,
				nerTfMaxEpochs,
				nerTfLr,
				nerTfPo,
				nerTfDropOut,
				nerTfValidationSplit,
				nerTfBatchSize)

	}

}