package es.rcs.tfm.solr.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@SolrDocument(collection = Article.CORE)
public class Article {

	public static final String COLLECTION		= "article_collection";
	public static final String CORE				= "article_core";
	public static final String ID 				= "id";
	public static final String TITLE			= "title";
	
	@Id
    @Indexed(name = ID, type = "string")
    private String id;
 
    @Indexed(name = TITLE, type = "string")
    private String title;
    
}
