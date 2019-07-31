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
import com.johnsnowlabs.nlp.training.CoNLL

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.{SparkSession, DataFrame, Row, Dataset}

class NerTrain(sc: SparkContext, spark: SparkSession, modelDirectory: String) {

  val nerCorpus = """
                   |-DOCSTART- O
                   |
                   |John PER
                   |Smith PER
                   |works O
                   |at O
                   |Airbus ORG
                   |Germany LOC
                   |. O
                   |
                  """.stripMargin
                  
  def testNerFromConll2003() = { // Dataset[Row] = {
    
    // Crea un texto con la primera columna y tiene en la segunda los IOB
    val lines = nerCorpus.split("\n")
    val data = CoNLL(conllLabelIndex = 1).readDatasetFromLines(lines, spark).toDF
    
    import spark.implicits._
    val emptyData = spark.emptyDataset[String].toDF("text")

    val document = new DocumentAssembler().
      setInputCol("text").
    	setOutputCol(AnnotatorType.DOCUMENT)
    
    val sentence = new SentenceDetector().
    	setInputCols(AnnotatorType.DOCUMENT).
    	setOutputCol("sentences")
    
    val token = new Tokenizer().
    	setInputCols("sentences").
    	setOutputCol(AnnotatorType.TOKEN)
    
    val pos = PerceptronModel.pretrained().
    	setInputCols("sentences", AnnotatorType.TOKEN).
    	setOutputCol(AnnotatorType.POS)
    	
    val embeddings = BertEmbeddings.
    	pretrained("bert_uncased", "en").
    	//load(this.modelDirectory).
    	setInputCols(AnnotatorType.DOCUMENT).
    	setOutputCol(AnnotatorType.WORD_EMBEDDINGS)

    val nerTagger =  new NerDLApproach().
      setInputCols("sentence", AnnotatorType.TOKEN, AnnotatorType.WORD_EMBEDDINGS).
      setOutputCol(AnnotatorType.NAMED_ENTITY).
      setLabelColumn("label").
      setRandomSeed(0).
      setPo(0.01f).
      // setPo(5e-3f). //0.005
      setLr(0.1f).
      // setLr(1e-1f). //0.001
      setMaxEpochs(100).
      // setMaxEpochs(1).
      // setDropout(5e-1f). //0.5
      // setTrainValidationProp(0.1f).
      // setExternalDataset("tmvar.train").
      // setValidationDataset("tmvar.validation").
      // setTestDataset("tmvar.test").
      // setEmbeddingsSource("/clinical.embeddings.100d.txt", 100, 2).
      // setIncludeEmbeddings(True).
      // setVerbose(2).
      setBatchSize(9)

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
    		pos,
    		embeddings,
    		nerTagger,
    		converter,
    		finisher
    ))

    val model = pipeline.fit(emptyData)

    //model.getStages(3).write.overwrite.save("newmodel")
    
  }
  
  /*
   * 
  converter = NerConverter()\
  .setInputCols(["document", "token", "ner"])\
  .setOutputCol("ner_span")
    
finisher = Finisher() \
  .setInputCols(["ner_span"])\
  .setIncludeKeys(True)

pipeline = Pipeline(
  stages = [
    documentAssembler,
    sentenceDetector,
    tokenizer,
    nerTagger,
    converter,
    finisher
  ]
searchForSuitableGraph(10, 100, 100)
    assert(smallGraphFile.endsWith("blstm_10_100_128_100.pb") || smallGraphFile.endsWith("blstm-noncontrib_10_100_128_100.pb"))

    val bigGraphFile = NerDLApproach.searchForSuitableGraph(25, 300, 100)
    assert(bigGraphFile.endsWith("blstm_25_300_128_100.pb") || bigGraphFile.endsWith("blstm-noncontrib_25_300_128_100.pb"))
       * 
    val conll = CoNLL()
    val training_data = conll.readDataset(ResourceHelper.spark, "python/tensorflow/ner/conll2003/eng.testa")
    val embeddings = WordEmbeddingsModel.pretrained().setOutputCol("embeddings")
    val readyData = embeddings.transform(training_data)
val ner = new NerDLApproach()
      .setInputCols("sentence", "token", "embeddings")
      .setOutputCol("ner")
      .setLabelColumn("label")
      .setOutputCol("ner")
      .setPo(5e-3f) //0.005
      .setDropout(5e-1f) //0.5
      .setMaxEpochs(1)
      .setRandomSeed(0)
      .setVerbose(0)
      .setTrainValidationProp(0.1f)
      .fit(readyData)  }   */
}