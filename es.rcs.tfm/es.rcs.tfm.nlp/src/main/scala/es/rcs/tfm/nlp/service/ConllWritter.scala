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
   * Genera un dataset procesado por NerDL a formato CONLL2003 
   * @param data Datos a exportar
   * @param outputPath Directorio destino de la exportacion
   */
  def generateConll(data: DataFrame): DataFrame = {
    
    import data.sparkSession.implicits._ // for row casting

println(java.time.LocalTime.now + ": generate init")
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
    		//TfmType.FINISHED_NAMED_ENTITY_CHUNK,
    		TfmType.FINISHED_TOKEN_METADATA
    	).as[(
    		String,
    		String, 
    		Array[String], 
    		Array[String], 
    		Array[String], 
    		Array[String], 
    		Array[String], 
    		//Array[String], 
    		Array[(String, String)])]
    
    // ---------------------------------------------------------------------------------------
    // Expresiones regulares para tratar las tuplas
    val NOTES_PTR:Regex = raw"\[\s*(\d+)\s*,\s*(\d+)\s*,\s*(.+)\s*,\s*(\w+)\s*\]".r
    val IOB_PTR:Regex = raw"\[(\w+)\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*(.+)\s*,\s*.*\]".r

    // ---------------------------------------------------------------------------------------
    // RECORRER CADA FILA GENERANDO UN DOCUMENTO
    var docId = 0
    val conllData = dataSelect.flatMap(row => {

println(java.time.LocalTime.now + ": doc_start " + docId)

// Inicio del documento
      val conllDoc: ArrayBuffer[(String, String, String, String, Integer, Integer, Integer, Integer)] = ArrayBuffer()
      conllDoc.append(("-DOCSTART-", "-X-", "-X-", "O", docId, 0, 0, 0))
      conllDoc.append((" ", " ", " ", " ", docId, 0, 0, 0))

      // Construye la tupla: ((1127,1135),Asp506Gly,NNP,O,9)
      val dataPrepared = (
      	row._4.map { // Partimos de los tokens
      		case IOB_PTR(iob, start, end, word) => (start.toInt, end.toInt) 
      		//case _ => (-1, -1)} zip row._5 zip row._6 zip row._7 zip row._8 zip row._9.map(_._2.toInt) map {
      		  //case ((((((begin, end), text), pos), chunk), ner), sentence) => ((begin, end), text, pos, chunk, ner, sentence)})
      		case _ => (-1, -1)} zip row._5 zip row._6 zip row._7 zip row._8.map(_._2.toInt) map {
      		  case (((((begin, end), text), pos), ner), sentence) => ((begin, end), text, pos, ner, sentence)})
      		
println(row._4.size)
println(row._5.size)
println(row._6.size)
println(row._7.size)
      // Para cada tupla
      var sentenceId = 0
      dataPrepared.foreach(token => { // FILA: ((begin, end), text, pos, ner, sentence)
//println(java.time.LocalTime.now + ": token " + token.toString())
        
        // Si hay cambio de frase se induce una linea en blanco
        if (token._5 != sentenceId){
          conllDoc.append((" ", " ", " ", " ", docId, 0, 0, 0))
          sentenceId = token._5
        }
        
        // Busca la localizacion por si hay un NER en los tokens
        // fila._2 tiene el token
        // row._3 tiene las notas [655, 661, G13513A, MUT_DMA]
        val DIFF = 2
        var str = token._4 // El token detectado por los procesos ner
        var enc = 0
        if ((row._3 != null) && (row._3.length>0)) {

          var iob = row._3.map(note => {
//println(java.time.LocalTime.now + ": note " + note)
            note match { // Obtenemos las notas
          	case NOTES_PTR(start: String, end: String, word: String, iob: String) => (start.toInt, end.toInt, word, iob) 
          	case _ => (-2, -2, "", "")}}).filter(nota => (
          	    (token._1._1-DIFF<=nota._1) && 
          	    (token._1._2+DIFF>=nota._2) &&
          	    (token._2 != null) &&
          	    (token._2.indexOf(nota._3)>=0))) // Busca word de nota en token
          if ((iob != null) && (iob.size > 0) && (iob(0)._4 != null) && (iob(0)._4.size > 0)) {
//println("encontrado")            
            str = iob(0)._4 
            enc = 1
          }
        }        	    
 
        // Linea del fichero CONLL
        var size = 0;
        if ((row._3 != null) && row._3.size != null) size = row._3.size
        conllDoc.append((token._2, token._3, token._3, str, docId, sentenceId, enc, size))

      })

      // Final del documento
      conllDoc.append((" ", " ", " ", " ", docId, 0, 0, 0))
println(java.time.LocalTime.now + ": doc_end " + docId)
      docId += 1

      conllDoc
      
    })
    
println(java.time.LocalTime.now + ": generate")
    val result = conllData.coalesce(1).toDF()
println(java.time.LocalTime.now + ": coalesce")
    result
    
  }
  
  /**
   * Guarda un dataset procesado por NerDL a formato CONLL2003 
   * @param data Datos a exportar
   * @param outputPath Directorio destino de la exportacion
   */
  def saveConll(data: DataFrame, outputPath: String): Double = {

    import data.sparkSession.implicits._ // for row casting

    // ---------------------------------------------------------------------------------------
    // Exportar los datos a un fichero CONLL
    //val conll = data.coalesce(1)
    val conll = data
println(java.time.LocalTime.now + ": saveConll")    
    saveDsToCsv(ds = conll.select("_1", "_2", "_3", "_4"), sep = " ", targetFile = outputPath)
println(java.time.LocalTime.now + ": saveConll-ALL")    
    saveDsToCsv(ds = conll, sep = " ", targetFile = outputPath+".all")
println(java.time.LocalTime.now + ": saveConll-STATS")    
    val precission = conll.select("_5", "_6", "_7", "_8").groupBy("_5").agg( // _5 DOCID
        "_5" -> "count",
        "_6" -> "count",
        "_7" -> "sum",
        "_8" -> "max").agg( // (_5, count(_5), count(_6), sum(_7), max(_8))
        "_5" -> "count", // docs
        "count(_6)" -> "sum", // sentences
        "sum(_7)" -> "sum", // encontrados
        "max(_8)" -> "sum") // total  

    val precRow = precission.first()
    var docs:Long = precRow.getLong(0)
    var sentences:Long = precRow.getLong(1)
    var items:Long = precRow.getLong(2)
    var total:Long = precRow.getLong(3)

    var result = 1.0
    if (total != 0) {
      result = items / total
    }
    println(result + ": encontrados " + items + " de " + total + " en " + docs + " documents!")
println(java.time.LocalTime.now + ": saveConll-END")    
    result

  }

  // https://fullstackml.com/2015/12/21/how-to-export-data-frame-from-apache-spark/
  def saveDsToCsv(
      ds: Dataset[_], 
      targetFile: String,
      sep: String = ",", 
      header: Boolean = false): Unit = {

    val tmpParquetDir = "CONLL.tmp.parquet"
println(java.time.LocalTime.now + ": saveDsToCsv - save")
    ds.
      // repartition(1).
      write.
      mode("overwrite").
      format("com.databricks.spark.csv").
      option("header", header.toString).
      option("delimiter", sep).
      save(tmpParquetDir)
println(java.time.LocalTime.now + ": saveDsToCsv - rename")
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