package es.rcs.tfm.nlp.util

import com.johnsnowlabs.nlp.{Annotation, AnnotatorModel}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel}
import com.johnsnowlabs.nlp.annotators.ner.crf.{NerCrfModel}

import org.apache.spark.sql.{DataFrame, Dataset}
import org.apache.spark.ml.{PipelineModel}

import java.io.{BufferedWriter, File, FileWriter}

object TfmSave {

	val DEBUG = false


	def saveModel[_](
			model: AnnotatorModel[_],
			modelDirectory: String): AnnotatorModel[_] = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: BEGIN saveModel" + modelDirectory)

		model.
				write.
				overwrite.
				save(modelDirectory)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: END   saveModel")

		model

	}


	def saveParquetDataSet (
			data: DataFrame,
			dataDirectory: String = "CONLL-eng.tmp.parquet"): Unit = {

		// PREPARAR DATOS DE VALIDACION PARA NerDLApproach

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: BEGIN saveParquetDataSet")

		data.
				write.
				mode("overwrite").
				format("parquet").
				save(dataDirectory)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: END   saveParquetDataSet")

	}


	def saveDLModel(
			model: PipelineModel,
			nerDirectory: String): NerDLModel = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: BEGIN saveDLModel")

		// GUARDAR EL MODELO
		val ner = model.
				stages.
				filter(s => s.isInstanceOf[NerDLModel]).
				head.
				asInstanceOf[NerDLModel]

		saveModel(ner, nerDirectory)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: END   saveDLModel")

		ner

	}


	def saveCrfModel(
			model: PipelineModel,
			nerDirectory: String): NerCrfModel = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: BEGIN saveCRFModel")

		// GUARDAR EL MODELO
		val ner = model.
				stages.
				filter(s => s.isInstanceOf[NerCrfModel]).
				head.
				asInstanceOf[NerCrfModel]

		saveModel(ner, nerDirectory)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: END   saveCRFModel")

		ner

	}


	/**
		* Print top n Named Entity annotations
		*/
	def print(
			annotations: Seq[Annotation],
			n: Int): Unit = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: BEGIN print")

		for (a <- annotations.take(n)) {
			System.out.println(s"${a.begin}, ${a.end}, ${a.result}, ${a.metadata("text")}")
		}

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: END   print")

	}


	/**
		* Saves ner results to csv file
		* @param annotations
		* @param file
		*/
	def saveNerSpanTags(
			annotations: Array[Array[Annotation]],
			targetFile: String,
			sep: String = "\t",
			header: Boolean = false): Unit = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: BEGIN saveNerSpanTags")

		val bw = new BufferedWriter(new FileWriter(new File(targetFile)))

		if (header) bw.write(s"start${sep}end${sep}tag${sep}text\n")
		for (i <- 0 until annotations.length) {
			for (a <- annotations(i))
				bw.write(s"${a.begin}${sep}${a.end}${sep}${a.result}${sep}${a.metadata("entity").replace("\n", " ")}\n")
		}

		bw.close()

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: END   saveNerSpanTags")

	}


	// https://fullstackml.com/2015/12/21/how-to-export-data-frame-from-apache-spark/
	def saveDsToCsvInDatabricks(
			ds: Dataset[_],
			targetFile: String,
			sep: String = ",",
			header: Boolean = false): Unit = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: BEGIN saveDsToCsvInDatabricks")

		val tmpParquetDir = "saveDsToCsv.tmp.parquet"

		ds.
			// repartition(1).
			write.
			mode("overwrite").
			format("com.databricks.spark.csv").
			option("header", header.toString).
			option("delimiter", sep).
			save(tmpParquetDir)

		val dir = new File(tmpParquetDir)
		dir.
			listFiles.
			foreach(f => {
				if (f.getName().startsWith("part-00000")) {
					f.renameTo(new File(targetFile))
				} else {
					f.delete
				}
			})

		dir.delete

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: END   saveDsToCsvInDatabricks")

	}


	// https://fullstackml.com/2015/12/21/how-to-export-data-frame-from-apache-spark/
	def saveDsToCsv(
			ds: Dataset[_],
			targetFile: String,
			sep: String = ",",
			header: Boolean = false): Unit = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: BEGIN saveDsToCsv")

		ds.
			//repartition(1).
      //coalesce(1).
			write.
			mode("overwrite").
			format("csv").
			option("header", header.toString).
			option("delimiter", sep).
			option("nullValue", "").
			option("emptyValue", "").
			save(targetFile)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-SAVE: END   saveDsToCsv")

	}

}