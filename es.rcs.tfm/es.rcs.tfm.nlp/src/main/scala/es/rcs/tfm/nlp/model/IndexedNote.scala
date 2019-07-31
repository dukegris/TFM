package es.rcs.tfm.nlp.model

case class IndexedNote(text: String, iob: String, begin: Int = 0, end: Int = 0)