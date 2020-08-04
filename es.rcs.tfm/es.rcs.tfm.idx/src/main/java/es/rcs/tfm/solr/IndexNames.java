package es.rcs.tfm.solr;

public class IndexNames {

	// -------------------------------------------------------------------------------------
	// TFM-INDEX: CONFIGURACION
	public static final String IDX_CONFIG =						"taoIndexConf";
	public static final String IDX_CONFIG_PKG = 				"es.rcs.tfm.solr.config";
	public static final String IDX_SETUP_PKG =					"es.rcs.tfm.solr.setup";

	// -------------------------------------------------------------------------------------
	// TFM-INDEX: server
	public static final String IDX_CORE_SERVER =				"ARTICLES";
	public static final String IDX_SERVER =						"taoIndexServer";
	// Este nombre no se puede cambiar. En EnableSolrRepositories  	solrClientRef permite poner el 
	// nombre que se quiera pero la implementación de SolrRepositoryConfigExtension solo se hace
	// referencia al texto de un bean: BeanDefinitionName.SOLR_CLIENT.getBeanName(), por lo que no 
	// recoge el valo
	public static final String IDX_CLIENT =						"solrClient";
	//public static final String IDX_CLIENT =						"taoIndexClient";
	public static final String IDX_FACTORY =					"taoIndexFactory";
	public static final String IDX_TEMPLATE =					"taoIndexTemplate";

	// -------------------------------------------------------------------------------------
	// TFM-INDEX: REPOSITORIOS
	public static final String IDX_REPOSITORY_PKG =				"es.rcs.tfm.solr.repository";
	public static final String IDX_ALL_ARTICLES_REP =			"taoAllArticlesIdxRepo";

}
