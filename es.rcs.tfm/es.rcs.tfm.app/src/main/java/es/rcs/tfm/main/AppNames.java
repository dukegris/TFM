package es.rcs.tfm.main;

public class AppNames {

	// -------------------------------------------------------------------------------------
	// APP: CONFIG
	public static final String APP_NAME = 						"TCS TFM Web App";
	public static final String APP_CONFIG = 					"taoConfig";
	public static final String APP_CONFIG_PKG = 				"es.rcs.tfm.main.config";
	public static final String APP_PROPERTIES_LOADER = 			"taoPropertiesLoader";
	public static final String APP_MESSAGES_LOADER = 			"taoMessagesLoader";

	// -------------------------------------------------------------------------------------
	// J2EE: WEB SERVER
	public static final String JETTY_CONFIG = 					"taoJettyConfig";

	public static final String JETTY_SERVLET = 					"taoJettyServlet";
	public static final String JETTY_SERVLET_FACTORY = 			"taoJettyServletFactory";
	public static final String JETTY_SERVLET_REG = 				"taoJettyServletRegistration";
	public static final String JETTY_SERVLET_LSNR = 			"taoJettyServletListener";
	
	public static final String JETTY_SESSION_LSNR = 			"taoJettySessionListener";
	
	public static final String JETTY_REQUEST_LSNR = 			"taoJettyRequestListener";

	public static final String JETTY_LOCALE = 					"taoJettySessionLocale";

	public static final String JETTY_COOKIE = 					"taoJettyCookie";
	
	public static final String JETTY_FILTER_REG = 				"taoJettyFilterRegistration";
	
	public static final String JETTY_SESSION_PUBLISHER = 		"taoJettySessionEventPublisher";

	// -------------------------------------------------------------------------------------
	// APP: IDX CONFIG
	public static final String SOLR_CONFIG = 					"taoSolrConfig";

	// -------------------------------------------------------------------------------------
	// APP: SCHEDULER CONFIG
	public static final String QUARTZ_CONFIG = 					"taoQuartzConfig";

	// -------------------------------------------------------------------------------------
	// APP: BASE DE DATOS
	public static final String BBDD_CONFIG =					"taoBBDDConfig";

	public static final String BBDD_PU =						"TFM_PU";
	public static final String BBDD_DB =						"taoBBDDDB";
	
	public static final String BBDD_JPA_VENDOR =				"taoBBDD_JPA_VA";
	public static final String BBDD_JPA_DIALECT =				"taoBBDD_JPA_DIALECT";
	
	public static final String BBDD_DATASOURCE =				"taoBBDDDS";
	public static final String BBDD_CONSOLE =					"taoBBDDConsole";
		
	public static final String BBDD_NAME =						"jdbc:h2:file:J:/data/TFM;DB_CLOSE_ON_EXIT=true";
	public static final String BBDD_URL =						"/h2/*";

	// -------------------------------------------------------------------------------------
	// APP: SPARK CONFIG
	public static final String SPARK_CONFIG =					"taoSparkConfig";
	public static final String SPARK_SESSION_TEST =					"taoSparkSession";
	public static final String SPARK_APPNAME =					"SPARK TFM APP";
	
	
}
