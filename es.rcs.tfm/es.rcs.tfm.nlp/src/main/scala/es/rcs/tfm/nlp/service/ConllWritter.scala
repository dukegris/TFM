package es.rcs.tfm.nlp.service

import java.io.File

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.ml.param.{BooleanParam, Param, ParamMap, StringArrayParam}

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

/**
 * Clase encargada de mezclar en un fichero CONLL2003 el resultado de un 
 * procesado generico de extraccion de entidades con las entidades detectadas 
 * por procesos externos 8generalmente procedentes de papers
 *
 * @author raul
 *
 */
class ConllWritter(spark: SparkSession) {

  /**
   * Exporta un dataset procesador por NerDL a formato CONLL2003 
   * @param spark Sesion en la que se realiza la transformacion
   * @param data Datos a exportar
   * @param outputPath Directorio destino de la exportacion
   */
  def exportConllFiles(data: DataFrame, outputPath: String): Double = {
    
    import data.sparkSession.implicits._ // for row casting

    // ---------------------------------------------------------------------------------------
    // Preparacion de los datos para su procesamiento
    val conllData = data.select (
    		"id",
    		"text",
    		"notes",
    		"token", 
    		"finished_token", 
    		"finished_pos",
    		"finished_named_entity",
    		"finished_token_metadata"
    	).as[(
    		String,
    		String, 
    		Array[String], 
    		Array[String], 
    		Array[String], 
    		Array[String], 
    		Array[String], 
    		Array[(String, String)])]
    
    // ---------------------------------------------------------------------------------------
    // Expresiones regulares para tratar las tuplas
    val NOTES_PTR:Regex = raw"\[(\d+)\s*,\s*(\d+)\s*,\s*(.+)\s*,\s*(\w+)\s*\]".r
    val IOB_PTR:Regex = raw"\[(\w+)\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*(.+)\s*,\s*.*\]".r

    // ---------------------------------------------------------------------------------------
    // RECORRER CADA FILA GENERANDO UN DOCUMENTO
    //val CoNLLDataset = conllData.limit(6).flatMap(row => {
    var docId = 0
    val CoNLLDataset = conllData.flatMap(row => {

      // Inicio del documento
      val conllDoc: ArrayBuffer[(String, String, String, String, Int, Int, Int, Int)] = ArrayBuffer()
      conllDoc.append(("-DOCSTART-", "-X-", "-X-", "O", docId, 0, 0, 0))
      conllDoc.append((null, null, null, null, docId, 0, 0, 0))
      
      // Construye la tupla: ((1127,1135),Asp506Gly,NNP,O,9)
      val dataPrepared = (
      	row._4.map {
      		case IOB_PTR(iob, start, end, word) => (start.toInt, end.toInt) 
      		case _ => (-1, -1)} zip row._5 zip row._6 zip row._7 zip row._8.map(_._2.toInt) map {
      		case (((((begin, end), text), iob), ner), sentence) => ((begin, end), text, iob, ner, sentence)})
      		
      // Para cada tupla
      var sentenceId = 0
      // var encontrados = 0;
      dataPrepared.foreach(a => {
        
        // Si hay cambio de frase se induce una linea en blanco
        if (a._5 != sentenceId){
          conllDoc.append((null, null, null, null, docId, 0, 0, 0))
          sentenceId = a._5
        }
        
        // Busca la localizacion por si hay un NER en los datos de train
        // row._3 tiene [655, 661, G13513A, MUT_DMA]
        val DIFF = 2
        var enc = 0
        val iob = row._3.map(_ match {
        	case NOTES_PTR(start: String, end: String, word: String, iob: String) => (start.toInt, end.toInt, word, iob) 
        	case _ => (-2, -2, "", "")}).filter(i => (
        	    (a._1._1>=i._1-DIFF) && 
        	    (a._1._1<=i._1+DIFF) && 
        	    (a._1._2>=i._2-DIFF) &&
        	    (a._1._2<=i._2+DIFF) &&
        	    (a._2.indexOf(i._3)>=0)))

        var str = a._4 
        if ((iob != null) && (iob.size > 0)) {
          str = iob(0)._4
          enc = 1
        }
 
        // Linea del fichero CONLL
        conllDoc.append((a._2, a._3, a._3, str, docId, sentenceId, enc, row._3.size))

      })

      // Final del documento

      docId += 1

      conllDoc.append((null, null, null, null, 0, 0, 0, 0))
      conllDoc
      
    })

    // ---------------------------------------------------------------------------------------
    // Exportar los datos a un fichero CONLL
    //saveDsToCsv(ds = CoNLLDataset.select("_c0", "_c1", "_c2", "_c3"), sep = " ", targetFile = outputPath)
    saveDsToCsv(ds = CoNLLDataset, sep = " ", targetFile = outputPath)
    
    val precission = CoNLLDataset.select("_5", "_6", "_7", "_8").groupBy("_5").agg(
        "_5" -> "count",
        "_6" -> "count",
        "_7" -> "sum",
        "_8" -> "max").agg(
        "count(_5)" -> "sum", // docs
        "count(_6)" -> "sum", // sentences
        "sum(_7)" -> "sum", // encontrados
        "max(_8)" -> "max") // total  
        
    var docs:Integer = precission.select("_1").first().getInt(0)
    var sentences:Integer = precission.select("_2").first().getInt(0)
    var items:Integer = precission.select("_3").first().getInt(1)
    var total:Integer = precission.select("_4").first().getInt(2)

    var result = 1.0
    if (total != 0) {
      result = items / total
    }
    println(result + ": encontrados " + items + " de " + total + " en " + docs + " documents!")
    result

  }

  // https://fullstackml.com/2015/12/21/how-to-export-data-frame-from-apache-spark/
  def saveDsToCsv(
      ds: Dataset[_], 
      targetFile: String,
      sep: String = ",", 
      header: Boolean = false): Unit = {

    val tmpParquetDir = "CONLL.tmp.parquet"

    ds.
      repartition(1).
      write.
      mode("overwrite").
      format("com.databricks.spark.csv").
      option("header", header.toString).
      option("delimiter", sep).
      save(tmpParquetDir)

    val dir = new File(tmpParquetDir)
    dir.listFiles.foreach(f => {
      if (f.getName().startsWith("part-00000")) {
        f.renameTo(new File(targetFile))
      } else {
        f.delete
      }
    })
   
    dir.delete
    
  }  
  
  
  /**
   * CODIGO DE RESERVA
    conllData.select("notes").as[(Array[String])].foreach(r => r.map{
      case NOTES_PTR(start, end) => (start.toInt, end.toInt)
      case _ => (-2, -2)}.foreach(println))
      val conllPrepared = conllData.map(a => (
      	a._1, a._2, 
      	a._3.map (_ match {
      		case PTR(iob: String, start: String, end: String, word: String) => (start.toInt, end.toInt) 
      		case _ => (-1, -1)}) zip a._4 zip a._5 zip a._6 zip a._7.map(_._2.toInt) map {
      		case (((((begin, end), text), iob), ner), sentence) => ((begin, end), text, iob, ner, sentence)}))
      conllPrepared.select("_3").as[Array[((Int, Int), String, String, String, Int)]].foreach(r => r.map{case ((a1,a2),b,c,d,e)=>(a1,a2)}.foreach(println))


    val dataPrepared = conllData.map(a => (
    	a._1, a._2, a._3,
    	a._4.map ( match {
    		case PTR(start, end) => (start.toInt, end.toInt) 
    		case _ => (-1, -1)}) zip a._5 zip a._6 zip a._7 zip a._8.map(_._2.toInt) map {
    		case (((((a1, a2),b), c), d), e) => ((a1,a2),b,c,d,e)}))
		*/
    //token: array<struct<annotatorType:string,begin:int,end:int,result:string,metadata:map<string,string>,embeddings:array<float>,sentence_embeddings:array<float>>>
    //pos:   array<struct<annotatorType:string,begin:int,end:int,result:string,metadata:map<string,string>,embeddings:array<float>,sentence_embeddings:array<float>>>

}