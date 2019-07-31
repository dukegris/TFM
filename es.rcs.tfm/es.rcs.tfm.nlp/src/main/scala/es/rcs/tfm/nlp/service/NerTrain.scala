package es.rcs.tfm.nlp.service

import com.johnsnowlabs.nlp.{Annotation, SparkNLP, DocumentAssembler, Finisher, AnnotatorType}
import com.johnsnowlabs.nlp.{RecursivePipeline, LightPipeline}
import com.johnsnowlabs.nlp.annotators.{Stemmer, Tokenizer, Normalizer}
import com.johnsnowlabs.nlp.annotators.common.NerTagged
import com.johnsnowlabs.nlp.annotators.ner.{NerConverter, NerApproach}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach, PretrainedNerDL} 
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.embeddings.{BertEmbeddings, WordEmbeddingsFormat, WordEmbeddingsModel}
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import com.johnsnowlabs.nlp.training.CoNLL
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ReadAs}

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row, Dataset}
import org.apache.spark.ml.PipelineModel

import es.rcs.tfm.nlp.util.NerHelper

class NerTrain(sc: SparkContext, spark: SparkSession, bertModelDirectory: String) {

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

  def getNerDlModelFor(trainFileName: String, modelDirectory: String): NerDLModel = {
    
    val nerReader = CoNLL()
    val nerHelper = new NerHelper(spark)
    
    val trainFile = ExternalResource(
         trainFileName, 
         ReadAs.LINE_BY_LINE, 
         Map.empty[String, String])
    
    val model = trainNerModel(nerReader, trainFile)
    
    val ner = model.
      stages.
      filter(s => s.isInstanceOf[NerDLModel]).
      head.
      asInstanceOf[NerDLModel]
      
    ner.
      write.
      overwrite.
      save(modelDirectory)
    
     ner
     
  }

  def saveNerModel(model: PipelineModel, modelDirectory: String): NerDLModel = {

    val ner = model.
      stages.
      filter(s => s.isInstanceOf[NerDLModel]).
      head.
      asInstanceOf[NerDLModel]
      
    ner.
      write.
      overwrite.
      save(modelDirectory)
    
     ner
         
  }
                  
  def measureNerTraining(trainFileName: String, testFileName: String, predictionsCsvFileName: String, pipelineModelDirectory: String): PipelineModel = {

    val nerReader = CoNLL()
    val nerHelper = new NerHelper(spark)
  
    val trainFile = ExternalResource(trainFileName, ReadAs.LINE_BY_LINE, Map.empty[String, String])
    val testFile = ExternalResource(testFileName, ReadAs.LINE_BY_LINE, Map.empty[String, String])
     
    val model = trainNerModel(nerReader, trainFile)
  
    measureNerModel(nerReader, model, trainFile, false)
    measureNerModel(nerReader, model, testFile, false)
  
    val df = model.transform(nerReader.readDataset(spark, testFile.path))
    val annotation = Annotation.collect(df, "ner_span")
    nerHelper.saveNerSpanTags(annotation, predictionsCsvFileName)
    
    model.write.overwrite().save(pipelineModelDirectory)
    val loadedModel = PipelineModel.read.load(pipelineModelDirectory)

    System.out.println("Training dataset")
    nerHelper.measureExact(nerReader, loadedModel, trainFile)
  
    System.out.println("Test dataset")
    nerHelper.measureExact(nerReader, loadedModel, testFile)
    
    model
    
  }
  
  def measureNerModel(nerReader: CoNLL, model: PipelineModel, file: ExternalResource, extended: Boolean = true, errorsToPrint: Int = 0): Unit = {
    
    val ner = model.
      stages.
      filter(s => s.isInstanceOf[NerDLModel]).
      head.
      asInstanceOf[NerDLModel].
      getModelIfNotSet

    val df = nerReader.
      readDataset(spark, file.path).
      toDF()

    val transformed = model.
      transform(df)

    val labeled = NerTagged.
      collectTrainingInstances(
          transformed, 
          Seq("sentence", AnnotatorType.TOKEN, AnnotatorType.WORD_EMBEDDINGS), "label")

    ner.measure(labeled, (s: String) => System.out.println(s), extended, errorsToPrint)

  }
  
  def trainNerModel(nerReader: CoNLL, file: ExternalResource): PipelineModel = {

    System.out.println("NER-TRAIN: Lectura del dataset")

    val time = System.nanoTime()
    
    val dataset = nerReader.
      readDataset(spark, file.path)
    
    System.out.println(s"NER-TRAIN: Lectura en ${(System.nanoTime() - time)/1e9}\n")

    System.out.println("NER-TRAIN: Comienzo del entrenamiento")

    val stages = createPipelineStagesDl()

    val pipeline = new RecursivePipeline().
      setStages(stages)

    pipeline.fit(dataset)
    
  }
  
  def createPipelineStagesDl() = {
    
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
      // setVerbose(Verbose.Epochs)
      setBatchSize(9)

    val converter = new NerConverter().
    	setInputCols(AnnotatorType.DOCUMENT, AnnotatorType.TOKEN, AnnotatorType.NAMED_ENTITY).
    	//setInputCols(AnnotatorType.DOCUMENT, "normal_token", AnnotatorType.NAMED_ENTITY).
    	setOutputCol("ner_span")

    val labelConverter = new NerConverter()
      .setInputCols(AnnotatorType.DOCUMENT, AnnotatorType.TOKEN, "label")
      .setOutputCol("label_span")

    Array(
    		document,
    		sentence,
    		token,
    		pos,
    		embeddings,
    		nerTagger,
    		converter,
    		labelConverter
    )
    
  }

}