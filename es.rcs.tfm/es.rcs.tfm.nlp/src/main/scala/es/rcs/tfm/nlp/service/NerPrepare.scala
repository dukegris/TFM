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

/**
 * 
 */
class NerPrepare(sc: SparkContext, spark: SparkSession) {

  /**
   * Ejecuta un pipeline sobre un dataset con una columna denominada text
   * @param rows El dataset
   * @param structType La estructura de datos
   * @return Un dataset etiquetado con document, sentences, token, pos, word_embedding, named_entity, ner_chunk, sus finished y sus finished metadata
   */
  def execute(rows: List[Row], structType: StructType, pipelineModelDirectory: String): DataFrame = {
    
    import spark.implicits._
    val emptyData = spark.emptyDataset[String].toDF("text")
    
    val data = spark.createDataFrame(
        rows, 
        structType)
    
    val document = new DocumentAssembler().
    	setInputCol("text").
    	setOutputCol(AnnotatorType.DOCUMENT) //.
    	//setCleanupMode("shrink") // disabled, inplace, inplace_full, shrink, shrink_full
    
    val sentence = new SentenceDetector().
    	setInputCols(AnnotatorType.DOCUMENT).
    	setOutputCol("sentences")
    
    val token = new Tokenizer().
    	setInputCols("sentences").
    	setOutputCol(AnnotatorType.TOKEN)
    /*
    val stemmer = new Stemmer().
    	setInputCols(AnnotatorType.TOKEN).
    	setOutputCol("stem")

    val normalizer = new Normalizer().
    	setInputCols("stem").
    	setOutputCol("normal_token")

    val pos = PerceptronModel.pretrained().
    	setInputCols("sentences", "normal_token").
    	setOutputCol(AnnotatorType.POS)
		 */

    val pos = PerceptronModel.pretrained().
    	setInputCols("sentences", AnnotatorType.TOKEN).
    	setOutputCol(AnnotatorType.POS)
    	
    val embeddings = BertEmbeddings.
    	pretrained("bert_uncased", "en").
    	//load(this.modelDirectory).
    	setInputCols(AnnotatorType.DOCUMENT).
    	setOutputCol(AnnotatorType.WORD_EMBEDDINGS)
    
    val ner = NerDLModel.
    	pretrained("ner_dl_bert").
    	//load(modelDirectory).
    	setInputCols(AnnotatorType.DOCUMENT, AnnotatorType.TOKEN, AnnotatorType.WORD_EMBEDDINGS).
    	//setInputCols(AnnotatorType.DOCUMENT, "normal_token", AnnotatorType.WORD_EMBEDDINGS).
    	setOutputCol(AnnotatorType.NAMED_ENTITY)

    val converter = new NerConverter().
    	setInputCols(AnnotatorType.DOCUMENT, AnnotatorType.TOKEN, AnnotatorType.NAMED_ENTITY).
    	//setInputCols(AnnotatorType.DOCUMENT, "normal_token", AnnotatorType.NAMED_ENTITY).
    	setOutputCol("ner_chunk")

    val finisher = new Finisher().
      //setInputCols(AnnotatorType.TOKEN, "stem", "normal_token", AnnotatorType.POS, AnnotatorType.WORD_EMBEDDINGS, AnnotatorType.NAMED_ENTITY, "ner_chunk").
      setInputCols(AnnotatorType.TOKEN, AnnotatorType.POS, AnnotatorType.WORD_EMBEDDINGS, AnnotatorType.NAMED_ENTITY, "ner_chunk").
      setIncludeMetadata(true).
      setCleanAnnotations(false)

    val pipeline = new RecursivePipeline().
    	setStages(Array(
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
    ))

    val model = pipeline.fit(emptyData)
    
    model.write.overwrite().save(pipelineModelDirectory)
    val loadedModel = PipelineModel.read.load(pipelineModelDirectory)

    val result = model.transform(data)
    
    // result.show

    result
    
  }

}