package es.rcs.tfm.nlp.util

import org.apache.spark.mllib.evaluation.MulticlassMetrics

object TagsMetrics {

	def computeAccuracy(metrics: MulticlassMetrics): Unit = {
		val accuracy = (metrics.accuracy * 1000).round / 1000.toDouble
		val weightedPrecision = (metrics.weightedPrecision * 1000).round / 1000.toDouble
		val weightedRecall = (metrics.weightedRecall * 1000).round / 1000.toDouble
		val weightedFMeasure = (metrics.weightedFMeasure * 1000).round / 1000.toDouble
		val weightedFalsePositiveRate = (metrics.weightedFalsePositiveRate * 1000).round / 1000.toDouble
		println("Accuracy: ", accuracy)
		println("Weighted Precision: ", weightedPrecision)
		println("Weighted Recall: ", weightedRecall)
		println("Weighted F1 Score: ", weightedFMeasure)
		println("Weighted False Positive Rate: ", weightedFalsePositiveRate)
	}

	def computeAccuracyByEntity(metrics: MulticlassMetrics, labels: List[String]): Unit = {
		val predictedLabels = metrics.labels
		predictedLabels.foreach { predictedLabel =>
			val entity = labels(predictedLabel.toInt)
			val precision = (metrics.precision(predictedLabel) * 1000).round / 1000.toDouble
			val recall = (metrics.recall(predictedLabel) * 1000).round / 1000.toDouble
			val f1Score = (metrics.fMeasure(predictedLabel) * 1000).round / 1000.toDouble
			val falsePositiveRate = (metrics.falsePositiveRate(predictedLabel) * 1000).round / 1000.toDouble
			println(entity + " Precision: ", precision)
			println(entity + " Recall: ", recall)
			println(entity + " F1-Score: ", f1Score)
			println(entity + " FPR: ", falsePositiveRate)
		}
	}

	def computeMicroAverage(metrics: MulticlassMetrics): Unit = {
		var totalP = 0.0
		var totalR = 0.0
		var totalClassNum = 0

		val labels = metrics.labels

		labels.foreach { label =>
			totalClassNum = totalClassNum + 1
			totalP = totalP + metrics.precision(label)
			totalR = totalR + metrics.recall(label)
		}
		totalP = totalP/totalClassNum
		totalR = totalR/totalClassNum
		val microAverage = 2 * ((totalP*totalR) / (totalP+totalR))
		println("Micro-average F1-Score: ", (microAverage * 1000).round / 1000.toDouble)
	}

}
