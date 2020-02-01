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
@SolrDocument(collection = PubArticleIdx.CORE)
public class PubArticleIdx {

	public static final String COLLECTION		= "article_collection";
	public static final String CORE				= "article_core";
	public static final String ID 				= "id";
	public static final String PMID				= "pmid";
	public static final String TITLE			= "title";
	public static final String SUMMARY			= "summary";
	
	@Id
    @Indexed(name = ID, type = "string")
    private String id;
	 
    @Indexed(name = PMID, type = "string")
    private String pmid;
    
    @Indexed(name = TITLE, type = "string")
    private String title;
    
    @Indexed(name = SUMMARY, type = "string")
    private String summary;
    
}
