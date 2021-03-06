package es.rcs.tfm.nlp.service

import org.apache.spark.sql.{SparkSession, DataFrame, Dataset, Row}

import java.io.{File}

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

import es.rcs.tfm.nlp.model.TfmType
import es.rcs.tfm.nlp.util.TfmSave

/**
 * Clase encargada de mezclar en un fichero CONLL2003 el resultado de un
 * procesado generico de extraccion de entidades con las entidades detectadas
 * por procesos externos generalmente procedentes de papers
 *
 * @author raul
 *
 */
class CoNLL2003Generator(spark: SparkSession) {

	/**
	 * Genera un dataset procesado por NerDL a formato CONLL2003
	 * @param data Datos a exportar
	 * @param mantainNerFromGenericModel Mantener los IOB obtenidos del modelo generico de NER
	 */
	def generate(data: DataFrame, mantainNerFromGenericModel: Boolean): DataFrame = {

		import data.sparkSession.implicits._ // for row casting

		println(java.time.LocalTime.now + ": NER-CONLL2003: generate init")
		// ---------------------------------------------------------------------------------------
		// Preparacion de los datos para su procesamiento
		val dataSelect = data.select (
				TfmType.ID,
				TfmType.TEXT,
				TfmType.NOTES,
				TfmType.TOKEN,
				TfmType.FINISHED_TOKEN,
				TfmType.FINISHED_POS,
				TfmType.FINISHED_NAMED_ENTITY,
				TfmType.FINISHED_TOKEN_METADATA).
			as[(
				String,
				String,
				Array[String],
				Array[String],
				Array[String],
				Array[String],
				Array[String],
				Array[(String, String)])]

		// ---------------------------------------------------------------------------------------
		// Expresiones regulares para tratar las tuplas.
		// note [655, 661, G13513A, MUT_DMA]
		val NOTES_PTR:Regex = raw"\[\s*(\d+)\s*,\s*(\d+)\s*,\s*(.+)\s*,\s*(\w+)\s*\]".r

		// [token,1509,1511,the,Map(sentence -> 10),WrappedArray()]
		// Esta expresion procesa este texto con case en vez de utilizar el esquema de anotaciones de SparkNLP
		val TOKEN_PTR:Regex = raw"\[(\w+)\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*(.+)\s*,\s*.*\]".r

		// ---------------------------------------------------------------------------------------
		// RECORRER CADA FILA GENERANDO UN DOCUMENTO
		var docId = 0

		// ROW: (id, text, notes, token, f_token, f_ner, m_token)
		val conllData = dataSelect.
			flatMap(row => {
				// row._1 -> id
				// row._2 -> text
				// row._3 -> notas [655, 661, G13513A, MUT_DMA]
				// row._4 -> token
				// row._5 -> token procesado
				// row._6 -> ner procesado
				// row._7 -> metadata del token

				// println(java.time.LocalTime.now + ": generate doc: " + row._1)
				// Inicio del documento
				val conllDoc: ArrayBuffer[(String, String, String, String, String, Integer, Integer, Integer, Integer, String)] = ArrayBuffer()
				conllDoc.append(("-DOCSTART-", "-X-", "-X-", "-X-", "O", docId, 0, 0, 0, ""))
				conllDoc.append((null, null, null, null, null, docId, 0, 0, 0, ""))

				// Tuplas con el formato ((begin, end), text, pos, ner, sentence)
				// Construye la tupla: ((1127,1135),Asp506Gly,NNP,O,9)
				val dataPrepared = (
					row._4. // row._4 token
						map {
							case TOKEN_PTR(tk, start, end, word) => (start.toInt, end.toInt)
							case _ => (-1, -1)
						} zip row._5 zip row._6 zip row._7 zip row._8.map(_._2.toInt) map {
							case (((((begin, end), text), pos), ner), sentence) => ((begin, end), text, pos, ner, sentence)
						})


				// Para cada tupla
				var sentenceId = 0
				// TUPLA: ((begin, end), text, pos, ner, sentence)
				dataPrepared.
					foreach(tupla => {

						// tupla._1 -> (begin, end)
						// tupla._2 -> text
						// tupla._3 -> pos
						// tupla._4 -> ner
						// tupla._5 -> sentence

						// Si hay cambio de frase se induce una linea en blanco
						if (tupla._5 != sentenceId) {
							conllDoc.append((null, null, null, null, null, docId, 0, 0, 0, ""))
							sentenceId = tupla._5
						}

						// Busca la localizacion por si hay un NER en los tokens
						// row._3 tiene las notas [655, 661, G13513A, MUT_DMA]
						// tupla._4 El token detectado por los procesos ner. Si hay coincidencia en la nota, pondra el iob de la nota
						var str:String = tupla._4
						if (!mantainNerFromGenericModel) str = "O"

						val DIFF = 2
						var enc = 0
						var coords:String = "(" + tupla._1._1 + ", " + (tupla._1._2 + 1) + ")"

						if ((row._3 != null) && (row._3.length>0)) {

							var iob = row._3.
								//map(note => {
								//    note match { // Obtenemos las notas
								//     case NOTES_PTR(start: String, end: String, word: String, iob: String) => (start.toInt, end.toInt, word, iob)
								//     case _ => (-2, -2, "", "")}}).
								map ({
									case NOTES_PTR(start: String, end: String, word: String, iob: String) => (start.toInt, end.toInt, word, iob)
									case _ => (-2, -2, "", "") }).
								filter(nota => ( (
									(tupla._1._1 < nota._2) &&
									(tupla._1._2 > nota._1) )
									/*
									// CASO DE UNA MUTACION INCLUIDA EN EL NER
									// los datos de NER y los de substring de java funcionan diferentes ya que el rango en ner incluye al ultimo caracter:
									(tupla._1._1     >= nota._1) &&
									(tupla._1._2 + 1 <= nota._2) ) || (
									//(tupla._1._1-DIFF<=nota._1) &&
									//(tupla._1._2+DIFF>=nota._2) &&
									//(tupla._2 != null) &&
									//(tupla._2.indexOf(nota._3)>=0)) ||
									// CASO DE UNA MUTACION UNIDA A MAS TEXTO EN UN SOLO TOKEN
									(tupla._1._1     <  nota._1) &&
									(tupla._1._2 + 1 >= nota._2) ) || (
									// CASO DE UNA MUTACION QUE ARRANCA EN EL TOKEN
									(tupla._1._1     <= nota._1) &&
									(tupla._1._2 + 1 >  nota._1) ) || (
									// CASO DE UNA MUTACION QUE ACABA EN EL TOKEN
									(tupla._1._1     <= nota._2) &&
									(tupla._1._2 + 1 >  nota._2) )
									*/
									))

							// Nos quedamos con la primera anotacion encontrada y si la marcacion esta en varios token se pone
							// en que apartado del IOB estamos B- I-
							// This tagging scheme is the IOB scheme originally put forward by Ramshaw and Marcus (1995). 
							// los datos de NER y los de substring de java funcionan diferentes ya que el rango en ner incluye al ultimo caracter:
							// A11470C NNP O "(734,740) - (734,741)
							if ((iob != null) && (iob.size > 0) && (iob(0)._4 != null) && (iob(0)._4.size > 0)) {
								enc = 0
								// CASO DE UNA MUTACION COINCIDENTE COIN UN TOKEN
								if         ((tupla._1._1 == iob(0)._1) && (tupla._1._2 + 1 == iob(0)._2) ) {
									enc = iob.length
									str = "I-" + iob(0)._4 // Forzado por SparkNLP
								// CASO DE UNA MUTACION QUE ARRANCA CON UN NER
								} else if  ((tupla._1._1 <= iob(0)._1) && (tupla._1._2 + 1 >  iob(0)._1) ) {
									enc = iob.length
									str = "B-" + iob(0)._4
								// CASO DE UNA MUTACION QUE ACABA CON UN NER
								} else if  ((tupla._1._1 <  iob(0)._2) && (tupla._1._2 + 1 >= iob(0)._2) ) {
									str = "I-" + iob(0)._4 // E- para formatos IOB2
								// CASO DE UNA MUTACION QUE DENTRO DE UN NER
								} else if  ((tupla._1._1 > iob(0)._1)  && (tupla._1._2 + 1 <  iob(0)._2) ) {
									str = "I-" + iob(0)._4
								} else {
									enc = iob.length
									str = "I-" + iob(0)._4 // Forzado por SparkNLP
								}
								coords = coords + " - (" + iob(0)._1 + ", " + iob(0)._2 + ")" + " encontradas " + iob.length + " notas"

							}

						}

						// Linea del fichero CONLL
						var totalNotas = 0;
						if ((row._3 != null) && row._3.size != null) totalNotas = row._3.size
						// TODO se graba dos veces la tupla 3 (POS) ya que no se dispone del CHUNK
						conllDoc.append((tupla._2, tupla._3, tupla._3, tupla._4, str, row._1.toInt, sentenceId, enc, totalNotas, coords))

					})

				// Final del documento
				conllDoc.append((null, null, null, null, null, docId, 0, 0, 0, ""))
				docId += 1

				conllDoc

		})

		println(java.time.LocalTime.now + ": NER-CONLL2003: generate coalesce")

		val result = conllData.coalesce(1).toDF()

		println(java.time.LocalTime.now + ": NER-CONLL2003: generate end")

		result

	}


	/**
	 * Guarda un dataset procesado por NerDL a formato CONLL2003
	 * @param data Datos a exportar
	 * @param outputPath Directorio destino de la exportacion
	 */
	def save(data: DataFrame, outputPath: String): Double = {

		println(java.time.LocalTime.now + ": NER-CONLL2003: save " + outputPath)

		import data.sparkSession.implicits._ // for row casting

		// ---------------------------------------------------------------------------------------
		// Exportar los datos a un fichero CONLL
		//val conll = data.coalesce(1)
		val conll = data
			// conll._1 -> text
			// conll._2 -> pos
			// conll._3 -> posTODO Falta el syntactic chunk tag
			// conll._4 -> ner
			// conll._5 -> str contiene la marcacion de la nota
			// conll._6 -> docId
			// conll._7 -> sentenceId
			// conll._8 -> enc si se ha encontrado una mutacion
			// conll._9 -> numNotas

		// CONLL2003: the word, its partof-speech tag, its chunk tag and its named entity tag
		TfmSave.saveDsToCsv(ds = conll.select("_1", "_2", "_3", "_5"), sep = " ", targetFile = outputPath)
		TfmSave.saveDsToCsv(ds = conll, sep = " ", targetFile = outputPath+".all")

		val enc = conll.
			select("_6", "_7", "_8", "_9").
			groupBy("_6").
			agg(// _6 DOCID
				"_6" -> "count",
				"_7" -> "count",
				"_8" -> "sum",
				"_9" -> "max")

		enc.
			filter(d => (
				d.getLong(3) <
				d.getInt(4))).
			foreach(d => println("NER-CONLL2003: Error en docId: " + d.get(0) + " encontrados " + d.get(3) + " de " + d.get(4) + " mutaciones"))

		val precission = enc.
			agg(// (_6, count(_6), count(_7), sum(_8), max(_9))
				"_6" -> "count", // docs
				"count(_7)" -> "sum", // sentences
				"sum(_8)" -> "sum", // mutaciones encontradas
				"max(_9)" -> "sum") // total de notas

		val precRow = precission.first()
		var docs:Long = precRow.getLong(0)
		var sentences:Long = precRow.getLong(1)
		var mutaciones:Long = precRow.getLong(2)
		var total:Long = precRow.getLong(3)

		var result:Double = 1.0
		if (total > 0) {
			result = mutaciones.toDouble / total.toDouble
		}
		println("NER-CONLL2003: Marcados " + mutaciones + " de " + total + " en " + docs + " documentos. PRECISION = " + result)

		println(java.time.LocalTime.now + ": NER-CONLL2003: saved " + outputPath)


		result

	}

}