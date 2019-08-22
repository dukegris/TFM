package es.rcs.tfm.nlp.service

import com.johnsnowlabs.nlp.{SparkNLP, DocumentAssembler, Finisher, AnnotatorType}
import com.johnsnowlabs.nlp.{RecursivePipeline, LightPipeline}
import com.johnsnowlabs.nlp.annotators.{Stemmer, Tokenizer, Normalizer}
import com.johnsnowlabs.nlp.annotators.ner.{NerConverter, NerApproach}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach, PretrainedNerDL} 
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.embeddings.{BertEmbeddings, WordEmbeddingsFormat, WordEmbeddingsModel}
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
import es.rcs.tfm.nlp.util.TfmType

/**
 * 
 */
class NerPrepare(sc: SparkContext, spark: SparkSession, posModelDirectory: String, bertModelDirectory: String, bertNerModelDirectory: String) {

  /**
   * Ejecuta un pipeline sobre un dataset con una columna denominada text
   * @param rows El dataset
   * @param structType La estructura de datos
   * @return Un dataset etiquetado con document, sentences, token, pos, word_embedding, named_entity, ner_chunk, sus finished y sus finished metadata
   */
  def execute(rows: List[Row], structType: StructType, pipelineModelDirectory: String): DataFrame = {
    
println(java.time.LocalTime.now + ": execute init")
    import spark.implicits._
    val emptyData = spark.emptyDataset[String].toDF(TfmType.TEXT)
    
    val data = spark.createDataFrame(
        rows, 
        structType)
    
    val pipeline = new RecursivePipeline().
    	setStages(createPipelineStagesDl)

println(java.time.LocalTime.now + ": execute fit")
    val model = pipeline.fit(emptyData)
println(java.time.LocalTime.now + ": execute save")
    //model.write.overwrite().save(pipelineModelDirectory)
    //val loadedModel = PipelineModel.read.load(pipelineModelDirectory)
println(java.time.LocalTime.now + ": execute transform")
    // java.lang.IllegalArgumentException: Input to reshape is a tensor with 15728640 values, but the requested shape has 983040
	  //   [[{{node bert/embeddings/Reshape}} = Reshape[T=DT_FLOAT, Tshape=DT_INT32, _device="/job:localhost/replica:0/task:0/device:CPU:0"](bert/embeddings/embedding_lookup, bert/embeddings/Reshape/shape)]]
    //val result = loadedModel.transform(data)
    val result = model.transform(data)
println(java.time.LocalTime.now + ": execute end")

    result
    
  }
  
  def createPipelineStagesDl() = {
    
    val document = new DocumentAssembler().
    	setInputCol(TfmType.TEXT).
    	setOutputCol(TfmType.DOCUMENT) //.
    	//setCleanupMode("shrink") // disabled, inplace, inplace_full, shrink, shrink_full
    
    val sentence = new SentenceDetector().
    	setInputCols(Array(TfmType.DOCUMENT)).
    	setOutputCol(TfmType.SENTENCES)
    
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

    val pos = PerceptronModel.pretrained().
    	setInputCols(TfmType.SENTENCES, TfmType.NORMAL).
    	setOutputCol(TfmType.POS)
		 */

    val pos = PerceptronModel.
      //pretrained().
      load(this.posModelDirectory).
    	setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN)).
    	setOutputCol(TfmType.POS)
    	
    val embeddings = BertEmbeddings.
      //pretrained(TfmType.PRETRAINED_BERT, "en").
      //pretrained("bert_uncased", "en").
    	load(this.bertModelDirectory).
    	setMaxSentenceLength(4096).
    	setDimension(1024).
    	setInputCols(Array(TfmType.SENTENCES)).
    	setOutputCol(TfmType.WORD_EMBEDDINGS)
    
    val ner = NerDLModel.
    	//pretrained(TfmType.PRETRAINED_NER_BERT).
    	load(this.bertNerModelDirectory).
    	setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS)).
    	setOutputCol(TfmType.NAMED_ENTITY)

    val converter = new NerConverter().
    	setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.NAMED_ENTITY)).
    	setOutputCol(TfmType.NAMED_ENTITY_CHUNK)

    val finisher = new Finisher().
      //setInputCols(Array(TfmType.DOCUMENT, TfmType.SENTENCES, TfmType.TOKEN, TfmType.POS, TfmType.WORD_EMBEDDINGS, TfmType.NAMED_ENTITY, TfmType.NAMED_ENTITY_CHUNK)).
      setInputCols(Array(
          TfmType.DOCUMENT, TfmType.SENTENCES, 
          TfmType.TOKEN, TfmType.POS, 
          TfmType.WORD_EMBEDDINGS, TfmType.NAMED_ENTITY)).
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
  		//converter,
  		finisher
  	)
    
  }

}