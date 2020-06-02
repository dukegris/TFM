package es.rcs.tfm.nlp.service

import org.apache.spark.sql.{SparkSession, DataFrame, Dataset, Row}

import java.io.{File}

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

import es.rcs.tfm.nlp.model.TfmType
import es.rcs.tfm.nlp.util.TfmSave

/**
 * Clase encargada de tratar el resultado de un pipeline NER
 * 
 * @author raul
 *
 */
class TextProcesor(spark: SparkSession) {

}