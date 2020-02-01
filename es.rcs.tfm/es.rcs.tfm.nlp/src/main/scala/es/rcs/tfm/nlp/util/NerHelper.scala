package es.rcs.tfm.nlp.util

import com.johnsnowlabs.nlp.{Annotation, AnnotatorType, AnnotatorModel, DocumentAssembler, Finisher, RecursivePipeline, LightPipeline}
import com.johnsnowlabs.nlp.annotators.{Stemmer, Tokenizer, Normalizer}
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.annotators.pos.perceptron.PerceptronModel
import com.johnsnowlabs.nlp.annotators.common.NerTagged
import com.johnsnowlabs.nlp.annotators.ner.{NerConverter, NerApproach, Verbose}
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach}
import com.johnsnowlabs.nlp.annotators.ner.crf.{NerCrfModel, NerCrfApproach}
import com.johnsnowlabs.nlp.embeddings.{BertEmbeddings, WordEmbeddingsFormat, WordEmbeddingsModel}
import com.johnsnowlabs.nlp.training.CoNLL
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ResourceHelper, ReadAs}

import es.rcs.tfm.nlp.model.TfmType

import java.io.{BufferedWriter, File, FileWriter}
import java.util.List;

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row, Dataset}
import org.apache.spark.sql.types.{StructType}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.mllib.evaluation.MulticlassMetrics

import scala.collection.{JavaConversions, JavaConverters}
import scala.collection.mutable
import scala.collection.mutable.WrappedArray
import scala.util.matching.Regex

object NerHelper {

  def prepareData (
      nerReader: CoNLL,
      fileName: String
  ): Dataset[_] = {

    println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareData " + fileName)

    val file = ExternalResource(
        fileName,
        ReadAs.LINE_BY_LINE,
        Map.empty[String, String])

    val data = nerReader.
        readDataset(
            ResourceHelper.spark,
            file.path)

    println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareData " + fileName)

    data

  }

  def prepareDocument (
  ): DocumentAssembler = {

    println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareDocument ")

    val document = new DocumentAssembler().
    	//setCleanupMode("shrink") // disabled, inplace, inplace_full, shrink, shrink_full
    	setInputCol(TfmType.TEXT).
    	setOutputCol(TfmType.DOCUMENT)

    println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareDocument ")

    document

  }

  def prepareSentence (
      maxSentenceLength: Integer = 512
  ): SentenceDetector = {

    println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareSentence ")

    val sentence = new SentenceDetector().
      setMaxLength(maxSentenceLength).
    	setInputCols(Array(TfmType.DOCUMENT)).
    	setOutputCol(TfmType.SENTENCES)

    println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareSentence ")

    sentence

  }

  def prepareToken (
  ): Tokenizer = {

    println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareToken ")

    val token = new Tokenizer().
    	setInputCols(Array(TfmType.SENTENCES)).
    	setOutputCol(TfmType.TOKEN)

    println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareToken ")

    token

  }

  def preparePos (
      posModelDirectory: String
  ): PerceptronModel = {

    println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN preparePos ")

    val pos = PerceptronModel.
      load(posModelDirectory).
    	setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN)).
    	setOutputCol(TfmType.POS)

    println(java.time.LocalTime.now + ": TFM-HELPER: END   preparePos ")

    pos

  }

  def prepareBert (
      bertModelDirectory: String,
      maxSentenceLength: Integer = 512,
      dimension: Integer = 768,
      batchSize: Integer = 32
  ): BertEmbeddings = {

    // PREPARAR BERT
    println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareBert " + bertModelDirectory)

    val embeddings = BertEmbeddings.
      load(bertModelDirectory).
      setDimension(dimension).
      setMaxSentenceLength(maxSentenceLength).
      setBatchSize(batchSize).
      setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN)).
      setOutputCol(TfmType.WORD_EMBEDDINGS)

    println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareBert " + bertModelDirectory)

    embeddings

  }

  def prepareNerCrfApproach (
      epochs: Int = 1,
      l2: Float = 1f,
      lossEps: Double = 1e-3,
      c0: Int = 2250000,
      minW: Double = 0.0
  ): NerCrfApproach = {

    // PREPARAR NerDLApproach
    println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareNerDL")

    val nerTagger =  new NerCrfApproach().
      // setConfigProtoBytes(bytes). // ConfigProto from tensorflow, serialized into byte array
      setRandomSeed(0). // Random seed
      setMinEpochs(1). // Minimum number of epochs to train
      setMaxEpochs(epochs). // Maximum number of epochs to train
      //setBatchSize(32). // Batch size

      // ENTRENAMIENTO CRF
      setL2(l2). // L2 regularization coefficient for CRF
      setC0(c0). // c0 defines decay speed for gradient
      setLossEps(lossEps). // If epoch relative improvement lass than this value, training is stopped
      setMinW(minW). // Features with less weights than this value will be filtered out

      // VALIDACIONES
      //setDicts("").
      //setExternalFeatures(path, delimiter, readAs, options): Path to file or folder of line separated file that has something like this: Volvo:ORG with such delimiter, readAs LINE_BY_LINE or SPARK_DATASET with options passed to the latter.
      // MEDIDAS
      // setEnableOutputLogs(true). // Whether to output to annotators log folder
      // setEvaluationLogExtended(true). // Whether logs for validation to be extended: it displays time and evaluation of each label. Default is false.
      setIncludeConfidence(true). // whether to include confidence scores in annotation metadata

      // CONFIGURACION NERDLMODEL
      // setUseContrib(false) // whether to use contrib LSTM Cells. Not compatible with Windows
      // setVerbose(2). // Level of verbosity during training
      //setEntities(Array("MUT-DNA", "MUT-PRO", "MUT_SNP")). // Entities to recognize
      setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.POS, TfmType.WORD_EMBEDDINGS)).
      setOutputCol(TfmType.NAMED_ENTITY).
      setLabelColumn(TfmType.LABEL) // Column with label per each token

    println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareNerDL")

    nerTagger

  }

  def prepareNerDL (
      tfGraphDirectory: String,
      epochs: Int = 1,
      lr: Float = 1e-3f,
      po: Float = 5e-3f
  ): NerDLApproach = {

    // PREPARAR NerDLApproach
    println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareNerDL")

    val nerTagger =  new NerDLApproach().
      // CONFIGURACION TENSORFLOW
      // setConfigProtoBytes(bytes). // ConfigProto from tensorflow, serialized into byte array
      // setGraphFolder(path). // Folder path that contain external graph files
      setRandomSeed(0). // Random seed
      setMinEpochs(1). // Minimum number of epochs to train
      setMaxEpochs(epochs). // Maximum number of epochs to train
      setBatchSize(32). // Batch size

      // ENTRENAMIENTO TENSORFLOW
      setLr(lr). // Learning Rate
      setPo(po). // Learning rate decay coefficient. Real Learning Rage = lr / (1 + po * epoch)
      // setDropout(5e-1f). // Dropout coefficient
      setGraphFolder(tfGraphDirectory).

      // VALIDACIONES
      // setValidationSplit(validationSplit). //Choose the proportion of training dataset to be validated against the model on each Epoch. The value should be between 0.0 and 1.0 and by default it is 0.0 and off.
      setIncludeConfidence(true). // whether to include confidence scores in annotation metadata
      // setTestDataset("tmvar.test"). // Path to test dataset. If set used to calculate statistic on it during training.
      //setTestDataset("/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmp/" + testDataSet).
      setValidationSplit(0.20f). //Si no hay conjunto de test, divide el de entrenamiento
      // setTestDataset(preparedTestData).

      // MEDIDAS
      setEnableOutputLogs(true). // Whether to output to annotators log folder
      setEvaluationLogExtended(true). // Whether logs for validation to be extended: it displays time and evaluation of each label. Default is false.
      setIncludeConfidence(true). // whether to include confidence scores in annotation metadata"

      // CONFIGURACI�N NERDLMODEL
      // setUseContrib(false) // whether to use contrib LSTM Cells. Not compatible with Windows
      // setVerbose(2). // Level of verbosity during training
      //setEntities(Array("MUT-DNA", "MUT-PRO", "MUT_SNP")). // Entities to recognize
      setInputCols(Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS)).
      setOutputCol(TfmType.NAMED_ENTITY).
      setLabelColumn(TfmType.LABEL) // Column with label per each token

    println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareNerDL")

    nerTagger

  }

  def prepareMeasureNerPipeline (
    nerTagger: AnnotatorModel[_],
    data: Dataset[_]
  ) : PipelineModel = {

    println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN prepareMeasureNerPipeline")
    
    val converterLabel = new NerConverter().
        setInputCols(TfmType.SENTENCES, TfmType.TOKEN, TfmType.LABEL).
        setOutputCol(TfmType.CONVERTER_LABEL)

    val converterNer = new NerConverter().
        setInputCols(TfmType.SENTENCES, TfmType.TOKEN, TfmType.NAMED_ENTITY). // BUG
        setOutputCol(TfmType.CONVERTER_NAMED_ENTITY)

    val finisher = new Finisher().
        setInputCols(
            TfmType.DOCUMENT, 
            TfmType.SENTENCES, 
            TfmType.TOKEN, 
            TfmType.POS, 
            TfmType.WORD_EMBEDDINGS, 
            TfmType.NAMED_ENTITY,
            TfmType.CONVERTER_NAMED_ENTITY,
            TfmType.LABEL,
            TfmType.CONVERTER_LABEL).
        setIncludeMetadata(true).
        setCleanAnnotations(false)

    //import spark.implicits._
    import data.sparkSession.implicits._ // for row casting
    val stages = Array(
        nerTagger,
        converterLabel,
        converterNer,
        finisher
    )

    val trainPipeline = new RecursivePipeline().setStages(stages)
    val model = trainPipeline.fit(data)
    println(java.time.LocalTime.now + ": TFM-HELPER: END   prepareMeasureNerPipeline")

    model

}  
  def saveModel[_](
      model: AnnotatorModel[_],
      nerDirectory: String
  ): AnnotatorModel[_] = {

    println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN saveModel")

    model.
      write.
      overwrite.
      save(nerDirectory)

    println(java.time.LocalTime.now + ": TFM-HELPER: END   saveModel")

    model

  }

  def saveDLModel(
      model: PipelineModel,
      nerDirectory: String
  ): NerDLModel = {

    println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN saveDLModel")

    // GUARDAR EL MODELO
    val ner = model.
        stages.
        filter(s => s.isInstanceOf[NerDLModel]).
        head.
        asInstanceOf[NerDLModel]

    saveModel(ner, nerDirectory)

    println(java.time.LocalTime.now + ": TFM-HELPER: END   saveDLModel")

    ner

  }

  def saveCrfModel(
      model: PipelineModel,
      nerDirectory: String
  ): NerCrfModel = {

    println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN saveCRFModel")

    // GUARDAR EL MODELO
    val ner = model.
        stages.
        filter(s => s.isInstanceOf[NerCrfModel]).
        head.
        asInstanceOf[NerCrfModel]

    saveModel(ner, nerDirectory)

    println(java.time.LocalTime.now + ": TFM-HELPER: END   saveCRFModel")

    ner

  }

  /**
    * Print top n Named Entity annotations
    */
  def print(
      annotations: Seq[Annotation],
      n: Int
  ): Unit = {

    for (a <- annotations.take(n)) {
      System.out.println(s"${a.begin}, ${a.end}, ${a.result}, ${a.metadata("text")}")
    }

  }

  /**
    * Saves ner results to csv file
    * @param annotations
    * @param file
    */
  def saveNerSpanTags(
      annotations: Array[Array[Annotation]],
      targetFile: String,
      sep: String = "\t",
      header: Boolean = false
  ): Unit = {

    val bw = new BufferedWriter(new FileWriter(new File(targetFile)))

    if (header) bw.write(s"start${sep}end${sep}tag${sep}text\n")
    for (i <- 0 until annotations.length) {
      for (a <- annotations(i))
        bw.write(s"${a.begin}${sep}${a.end}${sep}${a.result}${sep}${a.metadata("entity").replace("\n", " ")}\n")
    }

    bw.close()

  }

  // https://fullstackml.com/2015/12/21/how-to-export-data-frame-from-apache-spark/
  def saveDsToCsvInDatabricks(
      ds: Dataset[_],
      targetFile: String,
      sep: String = ",",
      header: Boolean = false): Unit = {

    println(java.time.LocalTime.now + ": NER-CONLL2003: save to CSV in Databricks")

    val tmpParquetDir = "saveDsToCsv.tmp.parquet"

    ds.
      // repartition(1).
      write.
      mode("overwrite").
      format("com.databricks.spark.csv").
      option("header", header.toString).
      option("delimiter", sep).
      save(tmpParquetDir)

    val dir = new File(tmpParquetDir)
    dir.listFiles.foreach(f => {
      if (f.getName().startsWith("part-00000")) {
        f.renameTo(new File(targetFile))
      } else {
        f.delete
      }
    })

    dir.delete

  }

  // https://fullstackml.com/2015/12/21/how-to-export-data-frame-from-apache-spark/
  def saveDsToCsv(
      ds: Dataset[_],
      targetFile: String,
      sep: String = ",",
      header: Boolean = false): Unit = {

    println(java.time.LocalTime.now + ": NER-CONLL2003: save to CSV")

    ds.
      // repartition(1).
      write.
      mode("overwrite").
      format("csv").
      option("header", header.toString).
      option("delimiter", sep).
      option("nullValue", "").
      option("emptyValue", "").
      save(targetFile)

  }

  def calcStat(
    correct: Int,
    predicted: Int,
    predictedCorrect: Int
  ): (Float, Float, Float) = {

    // prec = (predicted & correct) / predicted
    // rec = (predicted & correct) / correct
    val prec = predictedCorrect.toFloat / predicted
    val rec = predictedCorrect.toFloat / correct
    val f1 = 2 * prec * rec / (prec + rec)

    (prec, rec, f1)

  }

  def measure(
    result: Dataset[_]
  ): Unit = {

    println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN measure")
    val rows = result.select(TfmType.CONVERTER_NAMED_ENTITY, TfmType.CONVERTER_LABEL).collect()

    val correctPredicted = mutable.Map[String, Int]()
    val predicted = mutable.Map[String, Int]()
    val correct = mutable.Map[String, Int]()
    var toPrintErrors = 0

    for (row <- rows) {

        val predictions = NerTagged.getAnnotations(row, 0).filter(a => a.result != "O")
        for (p <- predictions) {
            val tag = p.metadata("entity")
            predicted(tag) = predicted.getOrElse(tag, 0) + 1
        }

        val labels = NerTagged.getAnnotations(row, 1).filter(a => a.result != "O")
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
    System.out.println(s"\tprec\t\trec\t\tf1")
    System.out.println(s"\t$prec\t$rec\t$f1")

    val keys = (correct.keys ++ predicted.keys ++ correctPredicted.keys).toList.distinct
    for (key <- keys) {
        val (prec, rec, f1) = calcStat(correct.getOrElse(key, 0), predicted.getOrElse(key, 0), correctPredicted.getOrElse(key, 0))
        System.out.println(s"$key\t$prec\t$rec\t$f1")
    }
    println(java.time.LocalTime.now + ": TFM-HELPER: END   measure")

  }
  
  def measureDL(
    ner: NerDLModel,
    data: Dataset[_]
  ): Unit = {
    println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN measureDL")
    val model = prepareMeasureNerPipeline(ner, data)
    val result = model.transform(data)
    println(java.time.LocalTime.now + ": TFM-HELPER: END   measureDL")
    measure(result)

    // ESTADISTICAS DEL CONJUNTO DE ENTRENAMIENTO
    val labeledData = NerTagged.
        collectTrainingInstances(
            result,
            Seq(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS),
            TfmType.LABEL)

    println(java.time.LocalTime.now + ": TFM-HELPER: measureNerModel")
    ner.getModelIfNotSet.measure(labeledData, (s: String) => System.out.println(s), true, 0)
    println(java.time.LocalTime.now + ": TFM-HELPER: measureNerModel")

  }

  def measureCRF(
      ner: NerCrfModel,
      data: Dataset[_]
  ): Unit = {
    println(java.time.LocalTime.now + ": TFM-HELPER: BEGIN measureCRF")
    val model = prepareMeasureNerPipeline(ner, data)
    val result = model.transform(data)
    println(java.time.LocalTime.now + ": TFM-HELPER: END   measureCRF")
    measure(result)
  }

  /*
  def measurePredictionsAndLabels(
      data: Dataset[_]
  ): Unit = {
    
    // Compute raw scores on the test set
    import data.sparkSession.implicits._ // for row casting
    val predictionAndLabels = data.
      select (TfmType.NAMED_ENTITY, TfmType.LABEL).
      map { row =>
        val prediction = row.getAs[String](TfmType.NAMED_ENTITY)
        val label = row.getAs[String](TfmType.LABEL)
        (prediction, label)
      }
    
    // Instantiate metrics object
    val metrics = new MulticlassMetrics(predictionAndLabels)
    
    // Confusion matrix
    println("Confusion matrix:")
    println(metrics.confusionMatrix)
    
    // Overall Statistics
    val accuracy = metrics.accuracy
    println("Summary Statistics")
    println(s"Accuracy = $accuracy")
    
    // Precision by label
    val labels = metrics.labels
    labels.foreach { l =>
      println(s"Precision($l) = " + metrics.precision(l))
    }
    
    // Recall by label
    labels.foreach { l =>
      println(s"Recall($l) = " + metrics.recall(l))
    }
    
    // False positive rate by label
    labels.foreach { l =>
      println(s"FPR($l) = " + metrics.falsePositiveRate(l))
    }
    
    // F-measure by label
    labels.foreach { l =>
      println(s"F1-Score($l) = " + metrics.fMeasure(l))
    }
    
    // Weighted stats
    println(s"Weighted precision: ${metrics.weightedPrecision}")
    println(s"Weighted recall: ${metrics.weightedRecall}")
    println(s"Weighted F1 score: ${metrics.weightedFMeasure}")
    println(s"Weighted false positive rate: ${metrics.weightedFalsePositiveRate}")
    
  }
   */
  
}