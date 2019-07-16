package es.rcs.tfm.nlp.service

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.ml.Pipeline

import com.johnsnowlabs.nlp.{SparkNLP, DocumentAssembler, Finisher}
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import com.johnsnowlabs.nlp.embeddings.{BertEmbeddings, WordEmbeddingsFormat}
import com.johnsnowlabs.nlp.annotators.{Tokenizer, Normalizer}
import com.johnsnowlabs.nlp.annotators.ner.NerConverter
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, PretrainedNerDL} 
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector

class TestSparkNLP(sc: SparkContext, spark: SparkSession, modelDirectory: String) {

  val summary = "Aminoacyl tRNA synthetases (ARSs) link specific amino acids with their cognate transfer RNAs in a critical early step of protein translation. Mutations in ARSs have emerged as a cause of recessive, often complex neurological disease traits. Here we report an allelic series consisting of seven novel and two previously reported biallelic variants in valyl-tRNA synthetase (VARS) in ten patients with a developmental encephalopathy with microcephaly, often associated with early-onset epilepsy. In silico, in vitro, and yeast complementation assays demonstrate that the underlying pathomechanism of these mutations is most likely a loss of protein function. Zebrafish modeling accurately recapitulated some of the key neurological disease traits. These results provide both genetic and biological insights into neurodevelopmental disease and pave the way for further in-depth research on ARS related recessive disorders and precision therapies."

  def nlp1: DataFrame = {
    
    SparkNLP.version()
    
    val testData = spark.createDataFrame(Seq(
      (1, summary))).toDF("id", "text")
    
    val pipeline = PretrainedPipeline("explain_document_ml", lang = "en")

    val annotation = pipeline.transform(testData)

    annotation
    
  }

  def nlp2: DataFrame = {

    SparkNLP.version()
    
    val testData = spark.createDataFrame(Seq(
      (1, summary))).toDF("id", "text")

    val pipeline = PretrainedPipeline("explain_document_dl", lang = "en")

    val annotation = pipeline.transform(testData)
    
    annotation("lemmas")

    annotation("pos")
    
    annotation
  }

  def nlp3: DataFrame = {
    
    SparkNLP.version()
    
    val testData = spark.createDataFrame(Seq(
      (1, summary))).toDF("id", "text")

    val pipeline = PretrainedPipeline("entity_recognizer_dl", lang = "en")

    val annotation = pipeline.transform(testData)

    annotation
    
  }

  def nlp4: DataFrame = {
    
    SparkNLP.version()
    
    val testData = spark.createDataFrame(Seq(
      (1, summary))).toDF("id", "text")

    val pipeline = PretrainedPipeline("match_datetime", lang = "en")

    val annotation = pipeline.transform(testData)

    annotation
    
  }

  def nlp5: DataFrame = {

    SparkNLP.version()
    
    val testData = spark.createDataFrame(Seq(
      (1, summary))).toDF("id", "text")

    val pipeline = PretrainedPipeline("match_pattern", lang = "en")

    val annotation = pipeline.transform(testData)

    annotation
    
  }

  def nlp6: DataFrame = {

    SparkNLP.version()
    
    val testData = spark.createDataFrame(Seq(
      (1, summary))).toDF("id", "text")

    val pipeline = PretrainedPipeline("match_chunk", lang = "en")

    val annotation = pipeline.transform(testData)
    
    annotation
    
  }

  def nlp7 {
    
    val testData = spark.createDataFrame(Seq(
      (1, summary))).toDF("id", "text")

      // load NER model trained by deep learning approach and GloVe word embeddings
    val ner_dl = NerDLModel.pretrained("ner_dl")
    // load NER model trained by deep learning approach and BERT word embeddings
    val ner_bert = NerDLModel.pretrained("ner_dl_bert")
    val french_pos = PerceptronModel.load(modelDirectory + "/pos_ud_gsd_fr_2.0.2_2.4_1556531457346/")
      .setInputCols("document", "token")
      .setOutputCol("pos")
  }
  
  def nlp8: DataFrame = {
    
    val testData = spark.createDataFrame(Seq(
      (1, summary))).toDF("id", "text")

    val document = new DocumentAssembler()
      .setInputCol("text")
      .setOutputCol("document")
    val sentenceDetector = new SentenceDetector()
      .setInputCols(Array("document"))
      .setOutputCol("sentences")
    val token = new Tokenizer()
      .setInputCols(Array("sentences"))
      .setOutputCol("token")
    val pos = PerceptronModel.pretrained()
      .setInputCols("sentences", "token")
      .setOutputCol("pos")

    /*
    val ner = new NerCrfApproach().
      setInputCols("document", "token", "pos").
      setOutputCol("ner").
      setLabelColumn("label").
      setOutputCol("ner").
      setMinEpochs(1).
      setMaxEpochs(5).
      setEmbeddingsSource("data/embeddings/glove.6B.100d.txt", 100, WordEmbeddingsFormat.TEXT).
      setExternalFeatures("data/ner/dict.txt", ",").
      setExternalDataset("data/ner/eng.train", "SPARK_DATASET").
      setC0(1250000).
      setRandomSeed(0).
      setVerbose(2)      
     */
      
    val normalizer = new Normalizer()
      .setInputCols("token")
      .setOutputCol("normal")
      
    /*
    val wordEmbeddings = new WordEmbeddings()
        .setInputCols("document", "token")
        .setOutputCol("word_embeddings")
        .setEmbeddingsSource("./embeddings.100d.test.txt",
        100, WordEmbeddingsFormat.TEXT) */
    // D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner_dl_bert_en_2.0.2_2.4_1558809068913
    // D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert_uncased_en_2.0.2_2.4_1556651478920
    val embeddings = BertEmbeddings
      //.pretrained("bert_uncased", "en")
      .load(modelDirectory + "/bert_uncased_en_2.0.2_2.4_1556651478920")
      .setInputCols("document")
      .setOutputCol("word_embeddings")
    val nerDl = NerDLModel
      //.pretrained("ner_dl_bert") 
      .load(modelDirectory + "/ner_dl_bert_en_2.0.2_2.4_1558809068913")
      .setInputCols("normal", "document", "word_embeddings")
      .setOutputCol("ner")
    val nerConverter = new NerConverter()
      .setInputCols("document", "normal", "ner")
      .setOutputCol("ner_converter")
    val finisher = new Finisher().
      setInputCols("ner")
    val pipeline = new Pipeline().setStages(Array(
      document,
      sentenceDetector,
      token,
      normalizer,
      embeddings,
      nerDl,
      nerConverter
    ))
      
    import spark.implicits._
    val emptyData = spark.emptyDataset[String].toDF("text")
      
    //val pipeline = PretrainedPipeline("ner_dl_bert", lang = "en")
    val model = pipeline.fit(emptyData)
    val annotation = model.transform(testData)
    
    annotation.show()
    
    annotation
      
  }

}