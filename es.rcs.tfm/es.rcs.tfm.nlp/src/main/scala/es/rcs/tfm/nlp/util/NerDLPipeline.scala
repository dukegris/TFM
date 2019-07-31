package es.rcs.tfm.nlp.util

import com.johnsnowlabs.nlp._
import com.johnsnowlabs.nlp.annotators.common.NerTagged
import com.johnsnowlabs.nlp.annotators.ner.{NerConverter, Verbose}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLApproach, NerDLModel}
import com.johnsnowlabs.nlp.embeddings.{WordEmbeddings, WordEmbeddingsFormat}
import com.johnsnowlabs.nlp.training.CoNLL
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ReadAs}

import org.apache.spark.sql.{SparkSession}
import org.apache.spark.ml.PipelineModel

import scala.collection.Seq

class NerDLPipeline(spark: SparkSession) {
  
  def test(): Unit = {
  
    val folder = "../es.rcs.tfm.corpus/training/ner/sparknlp/"
  
    val nerReader = CoNLL()
  
    val trainFile = ExternalResource(folder + "eng.train", ReadAs.LINE_BY_LINE, Map.empty[String, String])
    val testFileA = ExternalResource(folder + "eng.testa", ReadAs.LINE_BY_LINE, Map.empty[String, String])
    val testFileB = ExternalResource(folder + "eng.testb", ReadAs.LINE_BY_LINE, Map.empty[String, String])

    val model = trainNerModel(nerReader, trainFile)
  
    measure(nerReader, model, trainFile, false)
    measure(nerReader, model, testFileA, false)
    measure(nerReader, model, testFileB, true)
  
    val annotations = getUserFriendly(nerReader, model, testFileB)
    (new NerHelper(spark)).saveNerSpanTags(annotations, "predicted.csv")
  
    model.write.overwrite().save("ner_model")
    PipelineModel.read.load("ner_model")
  
    System.out.println("Training dataset")
    (new NerHelper(spark)).measureExact(nerReader, model, trainFile)
  
    System.out.println("Validation dataset")
    (new NerHelper(spark)).measureExact(nerReader, model, testFileA)
  
    System.out.println("Test dataset")
    (new NerHelper(spark)).measureExact(nerReader, model, testFileB)
    
  }

  def trainNerModel(nerReader: CoNLL, er: ExternalResource): PipelineModel = {
    System.out.println("Dataset Reading")
    val time = System.nanoTime()
    val dataset = nerReader.readDataset(spark, er.path)
    System.out.println(s"Done, ${(System.nanoTime() - time)/1e9}\n")

    System.out.println("Start fitting")

    val stages = createPipeline()

    val pipeline = new RecursivePipeline()
      .setStages(stages)

    pipeline.fit(dataset)
  }

  def createPipeline() = {

    val glove = new WordEmbeddings()
      .setEmbeddingsSource("../es.rcs.tfm.corpus/training/ner/sparknlp/glove.6B.100d.txt", 100, WordEmbeddingsFormat.TEXT)
      .setInputCols("sentence", "token")
      .setOutputCol("glove")
/*
      wordEmbeddings = new WordEmbeddings()
        .setInputCols("document", "token")
        .setOutputCol("word_embeddings")
        .setEmbeddingsSource("./embeddings.100d.test.txt",
        100, "text")
    val embeddings = BertEmbeddings.
    	pretrained("bert_uncased", "en").
    	//load(this.modelDirectory).
    	setInputCols("").
    	setOutputCol("glove")
 */       
    	
    val nerTagger = new NerDLApproach()
      .setInputCols("sentence", "token", "glove")
      .setLabelColumn("label")
      .setMaxEpochs(1)
      .setRandomSeed(0)
      .setPo(0.005f)
      .setLr(1e-3f)
      .setDropout(0.5f)
      .setBatchSize(32)
      .setOutputCol("ner")
      .setVerbose(Verbose.Epochs)

    val converter = new NerConverter()
      .setInputCols("document", "token", "ner")
      .setOutputCol("ner_span")

    val labelConverter = new NerConverter()
      .setInputCols("document", "token", "label")
      .setOutputCol("label_span")

    Array(
      glove,
      nerTagger,
      converter,
      labelConverter
    )
  }

  def measure(nerReader: CoNLL, model: PipelineModel, file: ExternalResource, extended: Boolean = true, errorsToPrint: Int = 0): Unit = {
    val ner = model.stages.filter(s => s.isInstanceOf[NerDLModel]).head.asInstanceOf[NerDLModel].getModelIfNotSet
    val df = nerReader.readDataset(spark, file.path).toDF()
    val transformed = model.transform(df)

    val labeled = NerTagged.collectTrainingInstances(transformed, Seq("sentence", "token", "glove"), "label")

    ner.measure(labeled, (s: String) => System.out.println(s), extended, errorsToPrint)
  }

  def getUserFriendly(nerReader: CoNLL, model: PipelineModel, file: ExternalResource): Array[Array[Annotation]] = {
    val df = model.transform(nerReader.readDataset(spark, file.path))
    Annotation.collect(df, "ner_span")
  }
  
}
