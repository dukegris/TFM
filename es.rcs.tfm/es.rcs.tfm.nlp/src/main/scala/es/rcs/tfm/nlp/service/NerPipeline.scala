package es.rcs.tfm.nlp.service

import com.johnsnowlabs.nlp.{SparkNLP, DocumentAssembler, Finisher, AnnotatorType}
import com.johnsnowlabs.nlp.{RecursivePipeline, LightPipeline}
import com.johnsnowlabs.nlp.annotators.{Stemmer, Tokenizer, Normalizer}
import com.johnsnowlabs.nlp.annotators.ner.{NerConverter, NerApproach}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach} 
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.embeddings.{BertEmbeddings, WordEmbeddingsModel}
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import com.johnsnowlabs.util.CoNLLGenerator 

import java.util.List;

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row, Dataset}
import org.apache.spark.sql.types.{StructType}
import org.apache.spark.ml.{Pipeline, PipelineModel}

import scala.collection.{JavaConversions, JavaConverters}
import scala.collection.mutable.WrappedArray
import scala.util.matching.Regex
import es.rcs.tfm.nlp.model.TfmType

/**
 * 
 */
class NerPipeline(
    sc: SparkContext, 
    spark: SparkSession, 
    posModelDirectory: String, 
    bertModelDirectory: String, 
    bertNerModelDirectory: String, 
    maxSentenceLength: Integer = 512, 
    dimension: Integer = 768,
    batchSize: Integer = 32,
    caseSensitive: Boolean = false) {
    
  println(java.time.LocalTime.now + ": NER-PIPELINE constructor")

  import spark.implicits._
  val emptyData = spark.emptyDataset[String].toDF(TfmType.TEXT)

  val pipeline = new RecursivePipeline().
  	setStages(createPipelineStagesDl)

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
    
    val data = spark.createDataFrame(
        rows, 
        structType)

    println(java.time.LocalTime.now + ": NER-PIPELINE execute df created")
    
    val result = model.transform(data)

    println(java.time.LocalTime.now + ": NER-PIPELINE execute transformed")
    
    result
    
  }
  
  
  def createPipelineStagesDl() = {
    
    val document = new DocumentAssembler().
    	setInputCol(TfmType.TEXT).
    	setOutputCol(TfmType.DOCUMENT) //.
    	//setCleanupMode("shrink") // disabled, inplace, inplace_full, shrink, shrink_full
    
    val sentence = new SentenceDetector().
    	setInputCols(Array(TfmType.DOCUMENT)).
    	setOutputCol(TfmType.SENTENCES).
    	setMaxLength(maxSentenceLength)
    
    val token = new Tokenizer().
    	setInputCols(Array(TfmType.SENTENCES)).
    	setOutputCol(TfmType.TOKEN)
    	
    /*
    val stemmer = new Stemmer().
    	setInputCols(TfmType.TOKEN).
    	setOutputCol(TfmType.STEM)

    val normalizer = new Normalizer().
    	setInputCols(TfmType.STEM).
    	setOutputCol(TfmType.NORMAL)
		 */

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
    
    val ner = NerDLModel.
    	//pretrained(TfmType.PRETRAINED_NER_BERT).
    	load(this.bertNerModelDirectory).
    	setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS)).
    	setOutputCol(TfmType.NAMED_ENTITY)

    val converter = new NerConverter().
    	setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.NAMED_ENTITY)).
    	setOutputCol(TfmType.CONVERTER_NAMED_ENTITY)

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

    Array(
  		document,
  		sentence,
  		token,
  		// stemmer,
  		// normalizer,
  		pos,
  		embeddings,
  		ner,
  		converter,
  		finisher
  	)
    
  }

}