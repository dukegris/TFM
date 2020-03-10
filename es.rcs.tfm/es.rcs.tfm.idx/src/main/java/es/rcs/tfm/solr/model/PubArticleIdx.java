package es.rcs.tfm.solr.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@SolrDocument(
		collection = PubArticleIdx.IDX_CORE)
public class PubArticleIdx {

	public static String BOOK_TYPE							= "LIBRO";
	public static String JOURNAL_TYPE						= "REVISTA";

	public static final String IDX_COLLECTION				= "article_collection";
	public static final String IDX_CORE						= "article_core";
	public static final String IDX_ID 						= "id";
	public static final String IDX_PMID						= "pmid";
	public static final String IDX_TITLE					= "title";
	public static final String IDX_SUMMARY					= "summary";
	public static final String IDX_GRANTS					= "grants";
	public static final String IDX_AUTHORS					= "authors";
	public static final String IDX_PUBLICATION_TYPE			= "publicationType";
	public static final String IDX_PUBLICATION_TITLE		= "publicationTitle";
	public static final String IDX_PUBLICATION_NUMBER		= "publicationNumber";
	public static final String IDX_PUBLICATION_DATE			= "publicationDate";
	public static final String IDX_PUBLICATION_PAGES		= "publicationPage";
	public static final String IDX_PUBLICATION_MEDIA		= "publicationMedia";
	public static final String IDX_PUBLICATION_ABBREVIATION	= "publicationAbbreviation";
	public static final String IDX_PUBLICATION_COUNTRY		= "publicationCountry";
	public static final String IDX_PUBLICATION_LANGUAGE		= "publicationLanguage";
	public static final String IDX_PUBLICATION_IDS			= "publicationIdentifiers";
	public static final String IDX_VOLUME_TITLE				= "volumeTitle";
	public static final String IDX_VOLUME_NUMBER			= "volumeNumber";
	public static final String IDX_SERIE_TITLE				= "serieTitle";
	public static final String IDX_SERIE_NUMBER				= "serieNumber";
	public static final String IDX_EDITION_EDITOR			= "editionEditor";
	public static final String IDX_EDITION_TITLE			= "editionTitle";
	public static final String IDX_EDITION_CITY				= "editionCity";
	public static final String IDX_IDS						= "identifiers";

	@Id
    @Indexed(
    		name = IDX_ID, 
    		type = "string")
    private String id;
	 
    @Indexed(
    		name = IDX_PMID, 
    		type = "string")
    private String pmid;
    
    @Indexed(
    		name = IDX_TITLE, 
    		type = "string")
    private String title;
    
    @Indexed(
    		name = IDX_SUMMARY, 
    		type = "string")
    private String summary;
    
    @Indexed(
    		name = IDX_AUTHORS, 
    		type = "string")
    private List<String> auhors;
    
    @Indexed(
    		name = IDX_GRANTS, 
    		type = "string")
    private List<String> grants;
	
	@Indexed(
		name = IDX_IDS, 
		type = "string")
	private List<String> identifiers;

	@Indexed(
		name = IDX_PUBLICATION_TYPE, 
		type = "string")
	private String publicationType;
	
	@Indexed(
		name = IDX_PUBLICATION_TITLE, 
		type = "string")
	private String publicationTitle;
	
	@Indexed(
		name = IDX_PUBLICATION_NUMBER, 
		type = "string")
	private String publicationNumber;
	
	@Indexed(
		name = IDX_PUBLICATION_DATE, 
		type = "string")
	private String publicationDate;
	
	@Indexed(
		name = IDX_PUBLICATION_PAGES, 
		type = "string")
	private String publicationPages;
	
	@Indexed(
		name = IDX_PUBLICATION_IDS, 
		type = "string")
	private List<String> publicationIdentifiers;

	@Indexed(
		name = IDX_PUBLICATION_MEDIA, 
		type = "string")
	private String publicationMedia;
	
	@Indexed(
		name = IDX_PUBLICATION_ABBREVIATION, 
		type = "string")
	private String publicationAbbreviation;
	
	@Indexed(
		name = IDX_PUBLICATION_COUNTRY, 
		type = "string")
	private String publicationCountry;
	
	@Indexed(
		name = IDX_PUBLICATION_LANGUAGE, 
		type = "string")
	private String publicationLanguage;
	
	@Indexed(
		name = IDX_VOLUME_TITLE, 
		type = "string")
	private String volumeTitle;
	
	@Indexed(
		name = IDX_VOLUME_NUMBER, 
		type = "string")
	private String volumeNumber;
	
	@Indexed(
		name = IDX_SERIE_TITLE, 
		type = "string")
	private String serieTitle;
	
	@Indexed(
		name = IDX_SERIE_NUMBER, 
		type = "string")
	private String serieNumber;
	
	@Indexed(
		name = IDX_EDITION_EDITOR, 
		type = "string")
	private String editionEditor;
	
	@Indexed(
		name = IDX_EDITION_TITLE, 
		type = "string")
	private String editionTitle;
	
	@Indexed(
		name = IDX_EDITION_CITY, 
		type = "string")
	private String editionCity;

}
