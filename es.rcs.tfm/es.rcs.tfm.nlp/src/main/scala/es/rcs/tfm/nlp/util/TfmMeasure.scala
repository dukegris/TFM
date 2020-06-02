package es.rcs.tfm.nlp.util

import com.johnsnowlabs.nlp.annotators.common.NerTagged
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel, NerDLApproach}
import com.johnsnowlabs.nlp.annotators.ner.crf.{NerCrfModel}

import org.apache.spark.sql.{Dataset}
import org.apache.spark.mllib.evaluation.MulticlassMetrics

import scala.collection.mutable

import es.rcs.tfm.nlp.model.TfmType

object TfmMeasure {

	val DEBUG = false


	def calcStat(
			correct: Int,
			predicted: Int,
			predictedCorrect: Int): (Float, Float, Float) = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-MEASURE: BEGIN calcStat")

		// prec = (predicted & correct) / predicted
		// rec = (predicted & correct) / correct
		val prec = if (predicted != 0) predictedCorrect.toFloat / predicted else 0
		val rec = if (correct != 0) predictedCorrect.toFloat / correct else 0
		val f1 = if ((prec + rec) != 0) 2 * prec * rec / (prec + rec) else 0

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-MEASURE: END   calcStat")

		(prec, rec, f1)

	}


	def measureDL(
			nerApproach: NerDLApproach,
			nerTagger: NerDLModel,
			data: Dataset[_],
			extended: Boolean = true,
			errorsToPrint: Int = 0): Unit = {

		println(java.time.LocalTime.now + ": TFM-MEASURE: BEGIN measureDL")

		// Nos hace falta un grafo para con mas tags al incluir B-, I-, E- para cada mutacion 4
		// Tenemos 14 4x3 + O + ""
		val instances = NerTagged.collectTrainingInstances(
				data.toDF(), 
				Array(TfmType.SENTENCES, TfmType.TOKEN, TfmType.WORD_EMBEDDINGS), TfmType.LABEL)
		val labels = instances.
			flatMap(r => r._1.labels).distinct

		val sentences = instances.
			map(r => r._2)

		// Tenemos 768 dimensiones (procede del modelo BERT
		val dims = nerApproach.calculateEmbeddingsDim(sentences)

		// Tenemos 88 caracteres diferentes
		val chars = instances.
			flatMap(r => r._2.
				tokens.
				flatMap(token => token.token.toCharArray)).
			distinct

		println("TAGS: " + labels.length)
		println("SENTENCES: " + sentences.length)
		println("DIMS: " + dims)
		println("CHARS: " + chars.length)

		val nerTF = nerTagger.getModelIfNotSet
		//nerTF.measure(instances, (s: String) => System.out.println(s), extended, errorsToPrint)
		nerTF.measure(instances, extended, includeConfidence = true, enableOutputLogs = false, outputLogsPath = "d:/measure.log")//, uuid = uuid)

		println(java.time.LocalTime.now + ": TFM-MEASURE: END   measureDL")

	}


	def measureTagsDL(
			nerTagger: NerDLModel,
			data: Dataset[_],
			errorsToPrint: Int = 0): Unit = {

		println(java.time.LocalTime.now + ": TFM-MEASURE: BEGIN measureTagsDL")

		val nerTransform = nerTagger.
		    transform(data)
		val nerTagsConverted = TfmHelper.
				prepareNerConverter().
				transform(nerTransform)
		val result = TfmHelper.
				prepareLabelConverter().
				transform(nerTagsConverted)

		TfmMeasure.measure(result, errorsToPrint)

		println(java.time.LocalTime.now + ": TFM-MEASURE: END   measureTagsDL")

	}


	def measure(
			result: Dataset[_],
			printErrors: Int = 0): Unit = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-MEASURE: BEGIN measure")

		val rows = result.
				select(TfmType.CONVERTER_NAMED_ENTITY, TfmType.CONVERTER_LABEL).
				collect()

		val falsePositivePredicted = mutable.Map[String, Int]()
		val falseNegativePredicted = mutable.Map[String, Int]()
		val correctPredicted = mutable.Map[String, Int]()
		val predicted = mutable.Map[String, Int]()
		val correct = mutable.Map[String, Int]()
		var toPrintErrors = printErrors

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
			
			val falsePositivePredictions = labels.toSet.diff(predictions.toSet)
			for (fp <- falsePositivePredictions) {
				val tag = fp.metadata("entity")
				falsePositivePredicted(tag) = falsePositivePredicted.getOrElse(tag, 0) + 1
			}
			
			val falseNegativePredictions = predictions.toSet.diff(labels.toSet)
			for (fn <- falseNegativePredictions) {
				val tag = fn.metadata("entity")
				falseNegativePredicted(tag) = falseNegativePredicted.getOrElse(tag, 0) + 1
			}

			if (toPrintErrors > 0) {

			  System.out.println("PREDICTIONS")
				var errors = toPrintErrors * 3
				for (p <- predictions) {
					if (errors > 0 && !correctPredictions.contains(p)) {
						System.out.println(s"Predicted\t${p.result}\t${p.begin}\t${p.end}\t${p.metadata.getOrElse("entity", "_text_")}")
						errors -= 1
					}
					if (errors > 0 && !falsePositivePredictions.contains(p)) {
						System.out.println(s"False Positive Predicted\t${p.result}\t${p.begin}\t${p.end}\t${p.metadata.getOrElse("entity", "_text_")}")
						errors -= 1
					}
					if (errors > 0 && !falseNegativePredictions.contains(p)) {
						System.out.println(s"False Negative Predicted\t${p.result}\t${p.begin}\t${p.end}\t${p.metadata.getOrElse("entity", "_text_")}")
						errors -= 1
					}
				}

			  System.out.println("LABELS")
				errors = toPrintErrors * 3
				for (p <- labels) {
					if (errors > 0 && !correctPredictions.contains(p)) {
						System.out.println(s"Correct\t${p.result}\t${p.begin}\t${p.end}\t${p.metadata.getOrElse("entity", "_text_")}")
						errors -= 1
					}
					if (errors > 0 && !falsePositivePredictions.contains(p)) {
						System.out.println(s"False Positive Predicted\t${p.result}\t${p.begin}\t${p.end}\t${p.metadata.getOrElse("entity", "_text_")}")
						errors -= 1
					}
					if (errors > 0 && !falseNegativePredictions.contains(p)) {
						System.out.println(s"False Negative Predicted\t${p.result}\t${p.begin}\t${p.end}\t${p.metadata.getOrElse("entity", "_text_")}")
						errors -= 1
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

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-MEASURE: END   measure")

	}

	/*
	def measurePredictionsAndLabels(
			result: Dataset[_]): Unit = {

		// Compute raw scores on the test set
		import result.sparkSession.implicits._ // for row casting
		val predictionAndLabels = result.
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