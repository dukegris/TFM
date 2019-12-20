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

import java.io.{BufferedWriter, File, FileWriter}

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row, Dataset}
import org.apache.spark.ml.PipelineModel

import scala.collection.mutable

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
      modelDirectory: String, 
      pipelineModelDirectory: String, 
      predictionsCsvFileName: String): PipelineModel = {

    println(java.time.LocalTime.now + ": NER-TRAIN: measureNerTraining")
    val nerReader = CoNLL()
    val trainFile = ExternalResource(trainFileName, ReadAs.LINE_BY_LINE, Map.empty[String, String])
    val testFile = ExternalResource(testFileName, ReadAs.LINE_BY_LINE, Map.empty[String, String])
    val trainDataset = nerReader.readDataset(spark, trainFile.path)
    val testDataset = nerReader.readDataset(spark, testFile.path)

    println(java.time.LocalTime.now + s": NER-TRAIN: Lectura en ${(System.nanoTime() - System.nanoTime())/1e9}\n")
    val pipelineModel = trainNerModel(nerReader, trainDataset, testDataset)
    
    // Guardar el pipeline
    pipelineModel.write.overwrite().save(pipelineModelDirectory)

    // Guardar el modelo
    saveNerModel(pipelineModel, modelDirectory)
    
    // Medir el entrenamiento del modelo generado sobre los conjuntos de train y de test
    val trainTransformed = measureNerModel(pipelineModel, trainDataset, predictionsCsvFileName + "_train.csv", false)
    val testTransformed = measureNerModel(pipelineModel, testDataset, predictionsCsvFileName + "_test.csv", false)
    
    // Medir el entrenamiento del modelo NER guardado
    measureNerModelExact(pipelineModel, trainTransformed)
    measureNerModelExact(pipelineModel, testTransformed)

    val dir = new File("./CONLL-test.tmp.parquet")
    dir.listFiles.foreach(f => {
      f.delete
    })
    dir.delete
    
    pipelineModel
    
  }
 
  
  def trainNerModel(
      nerReader: CoNLL, 
      trainDataset: Dataset[_], 
      testDataset: Dataset[_]): PipelineModel = {

    println(java.time.LocalTime.now + ": NER-TRAIN: trainNerModel")

    // TODO En vez de testdata se debiera repartir el traindata en un 85-15
    val preparedDataset = trainDataset.randomSplit(Array(0.85, 0.15), seed = 11L)
    val tmpParquetDir = preparedDataset(1).write.mode("overwrite").format("parquet").save("./CONLL-test.tmp.parquet")
    val stages = createPipelineStagesDl("./CONLL-test.tmp.parquet")

    val pipeline = new RecursivePipeline().setStages(stages)
    val model = pipeline.fit(preparedDataset(0))

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
  
  
  def measureNerModel(
      model: PipelineModel, 
      df: Dataset[_], 
      csvFile: String,
      extended: Boolean = true, 
      errorsToPrint: Int = 0): DataFrame = {
    
    println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel")
    val nerTF = model.
      stages.
      filter(s => s.isInstanceOf[NerDLModel]).
      head.
      asInstanceOf[NerDLModel].
      getModelIfNotSet

    println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel readed")
    val transformed = model.transform(df)
      
    println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel collect")
    val annotation = Annotation.collect(transformed, TfmType.NAMED_ENTITY_SPAN)
    saveNerSpanTags(annotation, csvFile)

    println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel transformed")
    val labeled = NerTagged.
      collectTrainingInstances(
          transformed, 
          Seq(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS), TfmType.LABEL)

    println(java.time.LocalTime.now + ": NER-TRAIN: measureNerModel labeled")
    nerTF.measure(labeled, (s: String) => System.out.println(s), extended, errorsToPrint)

    transformed
    
  }


  def measureNerModelExact(
      model: PipelineModel, 
      transformed: DataFrame,  
      printErrors: Int = 0): Unit = {
    
    val rows = transformed.select(TfmType.NAMED_ENTITY_SPAN, TfmType.LABEL_SPAN).collect()

    val correctPredicted = mutable.Map[String, Int]()
    val predicted = mutable.Map[String, Int]()
    val correct = mutable.Map[String, Int]()
    var toPrintErrors = printErrors

    for (row <- rows) {

      val predictions = NerTagged.getAnnotations(row, 0).filter(a => a.result != "O")
      val labels = NerTagged.getAnnotations(row, 1).filter(a => a.result != "O")

      for (p <- predictions) {
        val tag = p.metadata("entity")
        predicted(tag) = predicted.getOrElse(tag, 0) + 1
      }

      for (l <- labels) {
        val tag = l.metadata("entity")
        correct(tag) = correct.getOrElse(tag, 0) + 1
      }

      val correctPredictions = labels.toSet.intersect(predictions.toSet)

      for (a <- correctPredictions) {
        val tag = a.metadata("entity")
        correctPredicted(tag) = correctPredicted.getOrElse(tag, 0) + 1
      }

      if (toPrintErrors > 0) {
        for (p <- predictions) {
          if (toPrintErrors > 0 && !correctPredictions.contains(p)) {
            System.out.println(s"Predicted\t${p.result}\t${p.begin}\t${p.end}\t${p.metadata("text")}")
            toPrintErrors -= 1
          }
        }

        for (p <- labels) {
          if (toPrintErrors > 0 && !correctPredictions.contains(p)) {
            System.out.println(s"Correct\t${p.result}\t${p.begin}\t${p.end}\t${p.metadata("text")}")
            toPrintErrors -= 1
          }
        }
      }
    }

    val (prec, rec, f1) = calcStat(correct.values.sum, predicted.values.sum, correctPredicted.values.sum)
    System.out.println(s"$prec\t$rec\t$f1")

    val tags = (correct.keys ++ predicted.keys ++ correctPredicted.keys).toList.distinct

    for (tag <- tags) {
      val (prec, rec, f1) = calcStat(correct.getOrElse(tag, 0), predicted.getOrElse(tag, 0), correctPredicted.getOrElse(tag, 0))
      System.out.println(s"$tag\t$prec\t$rec\t$f1")
    }
  }

  
  def calcStat(correct: Int, predicted: Int, predictedCorrect: Int): (Float, Float, Float) = {
    // prec = (predicted & correct) / predicted
    // rec = (predicted & correct) / correct
    val prec = predictedCorrect.toFloat / predicted
    val rec = predictedCorrect.toFloat / correct
    val f1 = 2 * prec * rec / (prec + rec)

    (prec, rec, f1)
  }

  
  /**
    * Saves ner results to csv file
    * @param annotations
    * @param file
    */
  def saveNerSpanTags(
      annotations: Array[Array[Annotation]], 
      file: String): Unit = {
    val bw = new BufferedWriter(new FileWriter(new File(file)))

    bw.write(s"start\tend\ttag\ttext\n")
    for (i <- 0 until annotations.length) {
      for (a <- annotations(i))
        bw.write(s"${a.begin}\t${a.end}\t${a.result}\t${a.metadata("entity").replace("\n", " ")}\n")
    }
    bw.close()
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

    val ner =  new NerDLApproach().

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
    		ner,
    		converter,
    		labelConverter
    )
 
  }
  
}