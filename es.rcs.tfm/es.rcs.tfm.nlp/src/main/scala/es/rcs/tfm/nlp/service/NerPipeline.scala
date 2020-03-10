package es.rcs.tfm.nlp.service

import com.johnsnowlabs.nlp.RecursivePipeline

import org.apache.spark.{SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row}
import org.apache.spark.sql.types.{StructType}

import java.util.List

import es.rcs.tfm.nlp.model.TfmType
import es.rcs.tfm.nlp.util.TfmHelper

/**
 * Ejecuta un pipeline de extracción de etiquetas (NER) sobre una lista de filas
 */
class NerPipeline(
		sc: SparkContext,
		spark: SparkSession,
		posModelDirectory: String,
		bertModelDirectory: String,
		nerModelDirectory: String,
		maxSentenceLength: Integer = 512,
		bertDimension: Integer = 768,
		bertCaseSensitive: Boolean = false,
		bertBatchSize: Integer = 32) {

	println(java.time.LocalTime.now + ": NER-PIPELINE constructor")

	import spark.implicits._
	val emptyData = spark.emptyDataset[String].toDF(TfmType.TEXT)

	val pipeline = new RecursivePipeline().
		setStages(TfmHelper.productionPipelineStages(
			this.posModelDirectory,
			this.bertModelDirectory,
			this.nerModelDirectory,
			this.maxSentenceLength,
			this.bertDimension,
			this.bertCaseSensitive,
			this.bertBatchSize))

	val model = pipeline.fit(emptyData)

	println(java.time.LocalTime.now + ": NER-PIPELINE constructor created")

   /**
    * Ejecuta un pipeline sobre un dataset con una columna denominada text
    * @param rows El dataset
    * @param structType La estructura de datos
    * @return Un dataset etiquetado con document, sentences, token, pos, word_embedding, named_entity, ner_chunk, sus finished y sus finished metadata
    */
	def execute(
			rows: List[Row],
			structType: StructType,
			pipelineModelDirectory: String): DataFrame = {

		println(java.time.LocalTime.now + ": NER-PIPELINE execute init")

		val data = spark.
			createDataFrame(
				rows,
				structType)

		println(java.time.LocalTime.now + ": NER-PIPELINE execute df created")

		val result = model.transform(data)

		println(java.time.LocalTime.now + ": NER-PIPELINE execute transformed")

		result

	}

}