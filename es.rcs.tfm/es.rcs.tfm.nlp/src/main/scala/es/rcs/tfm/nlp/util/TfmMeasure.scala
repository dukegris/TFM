package es.rcs.tfm.nlp.util

import com.johnsnowlabs.nlp.annotators.common.NerTagged
import com.johnsnowlabs.nlp.annotators.ner.dl.{NerDLModel}
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
		val prec = predictedCorrect.toFloat / predicted
		val rec = predictedCorrect.toFloat / correct
		val f1 = 2 * prec * rec / (prec + rec)

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-MEASURE: END   calcStat")

		(prec, rec, f1)

	}


	def measure(
			result: Dataset[_],
			printErrors: Int = 0): Unit = {

		if (DEBUG) println(java.time.LocalTime.now + ": TFM-MEASURE: BEGIN measure")

		val rows = result.
				select(TfmType.CONVERTER_NAMED_ENTITY, TfmType.CONVERTER_LABEL).
				collect()

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

			if (toPrintErrors > 0) {

				var errors = toPrintErrors
				for (p <- predictions) {
					if (errors > 0 && !correctPredictions.contains(p)) {
						System.out.println(s"Predicted\t${p.result}\t${p.begin}\t${p.end}\t${p.metadata("text")}")
						errors -= 1
					}
				}

				errors = toPrintErrors
				for (p <- labels) {
					if (errors > 0 && !correctPredictions.contains(p)) {
						System.out.println(s"Correct\t${p.result}\t${p.begin}\t${p.end}\t${p.metadata("text")}")
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