package es.rcs.tfm.main;

public class AppNames {

	// -------------------------------------------------------------------------------------
	// CMD: CONFIG
	public static final String CMD_CONFIG 						= "taoCommandConfig";

	// -------------------------------------------------------------------------------------
	// APP: CONFIG
	public static final String APP_NAME 						= "RCS TFM Web App";
	public static final String APP_CONFIG 						= "taoConfig";
	public static final String APP_CONFIG_PKG 					= "es.rcs.tfm.main.config";
	public static final String APP_COMPONENTS_PKG 				= "es.rcs.tfm.main.components";
	public static final String APP_SERVICES_PKG 				= "es.rcs.tfm.main.services";
	public static final String APP_PROPERTIES_LOADER 			= "taoPropertiesLoader";
	public static final String APP_MESSAGES_LOADER 				= "taoMessagesLoader";

	public static final String APP_PRODUCTION 					= "production";
	public static final String APP_TEST 						= "test";
	public static final String APP_DEVELOPMENT 					= "deveopment";

	// -------------------------------------------------------------------------------------
	// APP: SHELL
	public static final String APP_SHELL 						= "taoShell";
	public static final String BIOC								= "BIOC";
	public static final String PUBTATOR							= "PUBTATOR";
	public static final String APP_TRAIN_COMMAND 				= "taoTrainCommand";
	public static final String APP_CONLL_COMMAND 				= "taoConllCommand";
	public static final String APP_DOWNLOAD_COMMAND 			= "taoDownloadCommand";
	public static final String APP_HELP_COMMAND 				= "taoHelpCommand";

	// -------------------------------------------------------------------------------------
	// APP: SHELL EXIT CODES
	public static final int OK									= 0;
	
	public static final int GENERATE_INVALID_FILE				= 1;
	public static final int GENERATE_INVALID_TYPE				= 2;
	public static final int GENERATE_START_FAILED				= 99;
	
	public static final int TRAIN_INVALID_TRAIN_FILE			= 3;
	public static final int TRAIN_INVALID_TEST_FILE				= 4;
	public static final int TRAIN_INVALID_DIRECTORY				= 5;
	public static final int TRAIN_START_FAILED					= 98;

	// -------------------------------------------------------------------------------------
	// J2EE: WEB SERVER
	public static final String JETTY_CONFIG 					= "taoJettyConfig";

	public static final String JETTY_SERVLET 					= "taoJettyServlet";
	public static final String JETTY_SERVLET_FACTORY 			= "taoJettyServletFactory";
	public static final String JETTY_SERVLET_REG 				= "taoJettyServletRegistration";
	public static final String JETTY_SERVLET_LSNR 				= "taoJettyServletListener";
	public static final String JETTY_SESSION_LSNR 				= "taoJettySessionListener";
	public static final String JETTY_REQUEST_LSNR 				= "taoJettyRequestListener";
	public static final String JETTY_LOCALE 					= "taoJettySessionLocale";
	public static final String JETTY_COOKIE 					= "taoJettyCookie";
	public static final String JETTY_FILTER_REG 				= "taoJettyFilterRegistration";
	public static final String JETTY_SESSION_PUBLISHER 			= "taoJettySessionEventPublisher";

	// -------------------------------------------------------------------------------------
	// J2EE: WEB SERVER APPLICATION
	public static final String WEB_CONFIG						= "taoWebConf";

	public static final String WEB_JACKSON_MAPPER				= "taoCrnkJsonMapper";
	public static final String WEB_LOCALE_RESOLVER 				= "taoWebLocaleResolver";
	
	// -------------------------------------------------------------------------------------
	// J2EE: WEB REST CRNK
	public static final String CRNK_CONFIG						= "taoCrnkConfiguration";

	public static final String CRNK_TX_MODULE					= "taoCrnkTransactionsModule";
	public static final String CRNK_JPA_MODULE					= "taoCrnkJpaModule";
	public static final String CRNK_CORS_SETUP					= "taoCrnkCorsSetup";
	public static final String CRNK_CORS_FILTER					= "taoCrnkCorsFilter";
	public static final String CRNK_SEC_CONFIG					= "taoCrnkSecurityConf";

	// -------------------------------------------------------------------------------------
	// J2EE: SECURITY
	public static final String SEC_CONFIG						= "taoSecurityConfiguration";

	public static final String SEC_CRYPT_PASSWORD				= "taoSecurityPasswordEncoder";
	public static final String SEC_CORS_SETUP					= "taoSecurityCorsSetup";
	public static final String SEC_AUTH_EX_EP					= "taoSecurityExceptionEntryPoint";
	public static final String SEC_AUTH_REALM					= "TaoSecurityRealm";
	public static final String SEC_AUTH_PROVIDER				= "TaoSecurityProvider";

	public static final String SEC_DETAILS_SERVICE				= "taoSecurityUserDetailsService";
	public static final String SEC_REMEMBERME_SERVICE			= "taoSecurityRememberMeService";
	
	
	// -------------------------------------------------------------------------------------
	// J2EE: JWT TOKENS
	public static final String SEC_JWT_AUTHENTICATION_FILTER	= "taoSecJwtAuthenticationFilter";
	public static final String SEC_JWT_AUTHORIZATION_FILTER		= "taoSecJwtAuthorizationFilter";
	public static final String JWT_ID							= "TFM-JWT";
	public static final String JWT_ISSUER						= "secure-api";
	public static final String JWT_AUDIENCE						= "secure-app";
	public static final String JWT_AUTHORITIES					= "authorities";
	
	// Signing key for HS512 algorithm
	// You can use the page http://www.allkeysgenerator.com/ to generate all kinds of keys
	public static final String JWT_SECRET						= "n2r5u8x/A%D*G-KaPdSgVkYp3s6v9y$B&E(H+MbQeThWmZq4t7w!z%C*F-J@NcRf";
	
	// JWT token defaults
	public static final String JWT_TYPE							= "typ";
	public static final String JWT_TOKENTYPE					= "JWT";
	public static final String JWT_HEADER_AUTHORIZATION			= "Authorization";
	public static final String JWT_BEARER_TOKEN_PREFIX			= "Bearer ";
	public static final String JWT_BASIC_TOKEN_PREFIX			= "Basic ";	
	  
	// -------------------------------------------------------------------------------------
	// APP: IDX CONFIG
	public static final String SOLR_CONFIG 						= "taoSolrConfig";

	// -------------------------------------------------------------------------------------
	// APP: SCHEDULER CONFIG
	public static final String QUARTZ_CONFIG 					= "taoQuartzConfig";

	// -------------------------------------------------------------------------------------
	// APP: BASE DE DATOS
	public static final String BBDD_CONFIG						= "taoBBDDConfig";

	public static final String BBDD_PU							= "TFM_PU";
	public static final String BBDD_DB							= "taoBBDDDB";
	public static final String BBDD_TR_SERVICE					= "taoTransactionService";
	public static final String BBDD_TR_MANAGER					= "taoTransactionManager";
	public static final String BBDD_TR_USER_MANAGER				= "taoUserTransactionManager";
	
	public static final String BBDD_JPA_VENDOR					= "taoBBDD_JPA_VA";
	public static final String BBDD_JPA_DIALECT					= "taoBBDD_JPA_DIALECT";
	
	public static final String BBDD_DATASOURCE					= "taoBBDDDS";
	public static final String BBDD_CONSOLE						= "taoBBDDConsole";
		
	public static final String BBDD_NAME						= "jdbc:h2:file:J:/data/TFM;DB_CLOSE_ON_EXIT=true";
	public static final String BBDD_URL							= "/h2/*";

	// -------------------------------------------------------------------------------------
	// APP: SPARK CONFIG
	public static final String SPARK_CONFIG						= "taoSparkConfig";
	public static final String SPARK_SESSION_TEST				= "taoSparkSession";
	public static final String SPARK_APPNAME					= "SPARK TFM APP";
	
	
}
