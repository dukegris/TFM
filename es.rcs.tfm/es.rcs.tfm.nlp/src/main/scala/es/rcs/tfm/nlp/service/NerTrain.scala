package es.rcs.tfm.nlp.service

import com.johnsnowlabs.nlp.{Annotation, SparkNLP, DocumentAssembler, Finisher, AnnotatorType}
import com.johnsnowlabs.nlp.{RecursivePipeline, LightPipeline}
import com.johnsnowlabs.nlp.annotators.{Stemmer, Tokenizer, Normalizer}
import com.johnsnowlabs.nlp.annotators.common.NerTagged
import com.johnsnowlabs.nlp.annotators.ner.{NerConverter, NerApproach}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach} 
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.embeddings.{BertEmbeddings, WordEmbeddingsFormat, WordEmbeddingsModel}
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import com.johnsnowlabs.nlp.training.CoNLL
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ReadAs}

import java.io.File

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row, Dataset}
import org.apache.spark.ml.PipelineModel

import es.rcs.tfm.nlp.model.TfmType

class NerTrain(
    sc: SparkContext, 
    spark: SparkSession, 
    posModelDirectory: String, 
    bertModelDirectory: String, 
    tensorflowModelDirectory: String, 
    maxSentenceLength: Integer = 512, 
    dimension: Integer = 1024,
    batchSize: Integer = 32,
    caseSensitive: Boolean = false,
    entities: Array[String] = Array("MUT-DNA", "MUT-PRO", "MUT_SNP")) {

  
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
                  
                  
  def train(
      trainFileName: String, 
      testFileName: String, 
      predictionsCsvFileName: String, 
      pipelineModelDirectory: String): PipelineModel = {

    println(java.time.LocalTime.now + ": NER-TRAIN: measureNerTraining")
    val nerReader = CoNLL()
    val trainFile = ExternalResource(trainFileName, ReadAs.LINE_BY_LINE, Map.empty[String, String])
    val testFile = ExternalResource(testFileName, ReadAs.LINE_BY_LINE, Map.empty[String, String])
    val trainDataset = nerReader.readDataset(spark, trainFile.path)
    val testDataset = nerReader.readDataset(spark, testFile.path)

    val tmpParquetDir = testDataset.write.mode("overwrite").parquet("./CONLL-test.tmp.parquet")

    println(java.time.LocalTime.now + s": NER-TRAIN: Lectura en ${(System.nanoTime() - System.nanoTime())/1e9}\n")
    val model = trainNerModel(nerReader, trainDataset, trainDataset)
  
//    measureNerModel(nerReader, model, trainFile, false)
    
//    measureNerModel(nerReader, model, testFile, false)
    
//    val df = model.transform(nerReader.readDataset(spark, testFile.path))
    
//    val nerHelper = new NerHelper(spark)
  
//    val annotation = Annotation.collect(df, TfmType.NAMED_ENTITY_SPAN)
//    nerHelper.saveNerSpanTags(annotation, predictionsCsvFileName)
    
//    model.write.overwrite().save(pipelineModelDirectory)
//    val loadedModel = PipelineModel.read.load(pipelineModelDirectory)
    
//    nerHelper.measureExact(nerReader, loadedModel, trainFile)
    
//    nerHelper.measureExact(nerReader, loadedModel, testFile)

    val dir = new File("./CONLL-test.tmp.parquet")
    dir.listFiles.foreach(f => {
      f.delete
    })
    dir.delete
    
    model
    
  }
 
  
  def trainNerModel(
      nerReader: CoNLL, 
      trainDataset: Dataset[_], 
      testDataset: Dataset[_]): PipelineModel = {

    println(java.time.LocalTime.now + ": NER-TRAIN: trainNerModel")

    val stages = createPipelineStagesDl("./CONLL-test.tmp.parquet")
    val pipeline = new RecursivePipeline().setStages(stages)
    val model = pipeline.fit(trainDataset)

    model
    
  }
 
  
  def saveNerModel(
      model: PipelineModel, 
      modelDirectory: String): NerDLModel = {

    println(java.time.LocalTime.now + ": NER-TRAIN: save ner model")
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
  

  def createPipelineStagesDl(
      testDataset: String) = {
    
    println(java.time.LocalTime.now + ": NER-TRAIN: createPipelineStagesDl")
    val document = new DocumentAssembler().
      setInputCol(TfmType.TEXT).
    	setOutputCol(TfmType.DOCUMENT).
	    setCleanupMode("shrink")
    
    val sentence = new SentenceDetector().
    	setInputCols(Array(TfmType.DOCUMENT)).
    	setOutputCol(TfmType.SENTENCES).
    	setMaxLength(maxSentenceLength)
    
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
    	setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN)).
    	setOutputCol(TfmType.WORD_EMBEDDINGS).
    	setMaxSentenceLength(maxSentenceLength).
    	setDimension(dimension).
    	setCaseSensitive(caseSensitive).
    	setBatchSize(batchSize)

    val nerTagger =  new NerDLApproach().

      // CONFIGURACION TENSORFLOW
      // setConfigProtoBytes(bytes). // ConfigProto from tensorflow, serialized into byte array
      // setGraphFolder(path). // Folder path that contain external graph files
      setRandomSeed(0). // Random seed
      setMinEpochs(10). // inimum number of epochs to train
      setMaxEpochs(100). // Maximum number of epochs to train
      setBatchSize(32). // Batch size

      // ENTRENAMIENTO TENSORFLOW
      setLr(0.1f). // Learning Rate
      setPo(0.01f). // Learning rate decay coefficient. Real Learning Rage = lr / (1 + po * epoch)
      // setDropout(5e-1f). // Dropout coefficient
      setGraphFolder(tensorflowModelDirectory).

      // VALIDACIONES
      // setValidationSplit(validationSplit). //Choose the proportion of training dataset to be validated against the model on each Epoch. The value should be between 0.0 and 1.0 and by default it is 0.0 and off.
      setIncludeConfidence(true). // whether to include confidence scores in annotation metadata
      // setTestDataset("tmvar.test"). // Path to test dataset. If set used to calculate statistic on it during training.
      setTestDataset(testDataset).

      // MEDIDAS
      setEnableOutputLogs(true). // Whether to output to annotators log folder
      setEvaluationLogExtended(true). // Whether logs for validation to be extended: it displays time and evaluation of each label. Default is false.
      setIncludeConfidence(true). // whether to include confidence scores in annotation metadata"

      // CONFIGURACION NERDLMODEL
      // setUseContrib(false) // whether to use contrib LSTM Cells. Not compatible with Windows
      // setVerbose(2). // Level of verbosity during training
      setEntities(entities). // Entities to recognize
      setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS)).
      setOutputCol(TfmType.NAMED_ENTITY).
      setLabelColumn(TfmType.LABEL) // Column with label per each token

    val converter = new NerConverter().
    	setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.NAMED_ENTITY)).
    	setOutputCol(TfmType.NAMED_ENTITY_SPAN)

    val labelConverter = new NerConverter()
      .setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.LABEL))
      .setOutputCol(TfmType.LABEL_SPAN)

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
          TfmType.NAMED_ENTITY_CHUNK,
          TfmType.LABEL_SPAN)).
    	setIncludeMetadata(true).
      setCleanAnnotations(false)

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

  /*
  
  def measureNerModel(nerReader: CoNLL, model: PipelineModel, file: ExternalResource, extended: Boolean = true, errorsToPrint: Int = 0): Unit = {
    
    println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel")
    val ner = model.
      stages.
      filter(s => s.isInstanceOf[NerDLModel]).
      head.
      asInstanceOf[NerDLModel].
      getModelIfNotSet

    val df = nerReader.
      readDataset(spark, file.path).
      toDF()

    println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel readed")
    val transformed = model.
      transform(df)

    println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel transformed")
    val labeled = NerTagged.
      collectTrainingInstances(
          transformed, 
          Seq(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS), TfmType.LABEL)

    println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel labeled")
    ner.measure(labeled, (s: String) => System.out.println(s), extended, errorsToPrint)

  }
  */
  
}