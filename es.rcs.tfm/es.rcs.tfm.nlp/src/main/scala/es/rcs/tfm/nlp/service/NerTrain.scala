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
import es.rcs.tfm.nlp.util.TfmType

class NerTrain(sc: SparkContext, spark: SparkSession, posModelDirectory: String, bertModelDirectory: String) {

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

println(java.time.LocalTime.now + ": measureNerTraining begin")
    val nerReader = CoNLL()
    val nerHelper = new NerHelper(spark)
  
    val trainFile = ExternalResource(trainFileName, ReadAs.LINE_BY_LINE, Map.empty[String, String])
    val testFile = ExternalResource(testFileName, ReadAs.LINE_BY_LINE, Map.empty[String, String])
     
println(java.time.LocalTime.now + ": trainNerModel " + trainFile)
    val model = trainNerModel(nerReader, trainFile)
  
println(java.time.LocalTime.now + ": measureNerModel " + trainFile.path)
    measureNerModel(nerReader, model, trainFile, false)
    
println(java.time.LocalTime.now + ": measureNerModel " + testFile.path)
    measureNerModel(nerReader, model, testFile, false)
    
println(java.time.LocalTime.now + ": model.transform " + testFile.path)
    val df = model.transform(nerReader.readDataset(spark, testFile.path))
    
println(java.time.LocalTime.now + ": collect anotations")
    val annotation = Annotation.collect(df, TfmType.NAMED_ENTITY_SPAN)
    nerHelper.saveNerSpanTags(annotation, predictionsCsvFileName)
    
println(java.time.LocalTime.now + ": reload model")
    model.write.overwrite().save(pipelineModelDirectory)
    val loadedModel = PipelineModel.read.load(pipelineModelDirectory)
    
println(java.time.LocalTime.now + ": Measure Training dataset " + trainFile.path)
    nerHelper.measureExact(nerReader, loadedModel, trainFile)
    
println(java.time.LocalTime.now + ": Measure Test dataset " + testFile.path)
    nerHelper.measureExact(nerReader, loadedModel, testFile)
    
println(java.time.LocalTime.now + ": measureNerTraining end")
    model
    
  }
  
  def measureNerModel(nerReader: CoNLL, model: PipelineModel, file: ExternalResource, extended: Boolean = true, errorsToPrint: Int = 0): Unit = {
    
println(java.time.LocalTime.now + ": measureNerModel begin")
    val ner = model.
      stages.
      filter(s => s.isInstanceOf[NerDLModel]).
      head.
      asInstanceOf[NerDLModel].
      getModelIfNotSet

    val df = nerReader.
      readDataset(spark, file.path).
      toDF()

println(java.time.LocalTime.now + ": measureNerModel transform")
    val transformed = model.
      transform(df)

println(java.time.LocalTime.now + ": measureNerModel annotation")
    val labeled = NerTagged.
      collectTrainingInstances(
          transformed, 
          Seq(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS), TfmType.LABEL)

println(java.time.LocalTime.now + ": results")
    ner.measure(labeled, (s: String) => System.out.println(s), extended, errorsToPrint)
println(java.time.LocalTime.now + ": measureNerModel end")

  }
  
  def trainNerModel(nerReader: CoNLL, file: ExternalResource): PipelineModel = {

println(java.time.LocalTime.now + ": NER-TRAIN: Lectura del dataset")

    val time = System.nanoTime()
    
    val dataset = nerReader.
      readDataset(spark, file.path)
    
    System.out.println(s"NER-TRAIN: Lectura en ${(System.nanoTime() - time)/1e9}\n")

println(java.time.LocalTime.now + ": NER-TRAIN: Comienzo del entrenamiento")

    val stages = createPipelineStagesDl()

    val pipeline = new RecursivePipeline().
      setStages(stages)

    val model = pipeline.fit(dataset)
    
println(java.time.LocalTime.now + ": NER-TRAIN: Salida")
    model
    
  }
  
  def createPipelineStagesDl() = {
    
    val document = new DocumentAssembler().
      setInputCol(TfmType.TEXT).
    	setOutputCol(TfmType.DOCUMENT)
    
    val sentence = new SentenceDetector().
    	setInputCols(Array(TfmType.DOCUMENT)).
    	setOutputCol(TfmType.SENTENCES)
    
    val token = new Tokenizer().
    	setInputCols(Array(TfmType.SENTENCES)).
    	setOutputCol(TfmType.TOKEN)
    
    val pos = PerceptronModel.
      //pretrained().
      load(this.posModelDirectory).
    	setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN)).
    	setOutputCol(TfmType.POS)
    	
    val embeddings = BertEmbeddings.
      //pretrained(TfmType.PRETRAINED_BERT, "en").
    	load(this.bertModelDirectory).
    	setInputCols(Array(TfmType.SENTENCES)).
    	setOutputCol(TfmType.WORD_EMBEDDINGS)

    val nerTagger =  new NerDLApproach().
      setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS)).
      setOutputCol(TfmType.NAMED_ENTITY).
      setLabelColumn(TfmType.LABEL).
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
    	setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.NAMED_ENTITY)).
    	//setInputCols(TfmType.DOCUMENT, TfmType.NORMAL, TfmType.NAMED_ENTITY).
    	setOutputCol(TfmType.NAMED_ENTITY_SPAN)

    val labelConverter = new NerConverter()
      .setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.LABEL))
      .setOutputCol(TfmType.LABEL_SPAN)

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