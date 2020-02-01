package es.rcs.tfm.nlp.model

object TfmType {
  
  val ID = "id"
  val TEXT = "text"
  val NOTES = "notes"
  
  val DOCUMENT = "document"
  val SENTENCES = "sentence"
  val TOKEN = "token"
  val NORMAL = "normal"
  val STEM = "stem"
  val WORD_EMBEDDINGS = "word_embeddings"
  val POS = "pos"
  val NAMED_ENTITY = "named_entity"
  val LABEL = "label"

  val CONVERTER_NAMED_ENTITY= "converter_named_entity"
  val CONVERTER_LABEL = "converter_label"

  val FINISHED_TOKEN = "finished_token"
  val FINISHED_TOKEN_METADATA = "finished_token_metadata"
  val FINISHED_NAMED_ENTITY = "finished_named_entity"
  val FINISHED_NAMED_ENTITY_CHUNK = "finished_named_entity_chunk"
  val FINISHED_POS = "finished_pos"

/*
  val LABEL_SPAN = "label_span"
  val NAMED_ENTITY_SPAN = "named_entity_span"
  val NAMED_ENTITY_CHUNK = "named_entity_chunk"
  val PRETRAINED_BERT = "bert_uncased"
  val PRETRAINED_NER_BERT = "ner_dl_bert"
 */  
}