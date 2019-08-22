package es.rcs.tfm.srv;

public class SrvNames {

	// -------------------------------------------------------------------------------------
	// SRV: CONFIGURACION
	public static final String SRV_CONFIG =			"taoServicesConfiguration";
	public static final String SRV_CONFIG_PKG = 	"es.rcs.tfm.srv.config";
	public static final String SRV_SETUP_PKG =		"es.rcs.tfm.srv.setup";
	public static final String SRV_COMPONENTS_PKG =	"es.rcs.tfm.srv.components";
	public static final String SRV_SERVICES_PKG =	"es.rcs.tfm.srv.services";

	// -------------------------------------------------------------------------------------
	// SRV: SERVICES
	public static final String PUBMED_LOADER_SRVC =	"taoPubmedLoaderService";
	public static final String PMC_LOADER_SRVC =	"taoPmcLoaderService";
	public static final String CORPUS_SRVC =		"taoCorpusService";
	public static final String TRAINING_SRVC =		"taoTrainingService";

	// -------------------------------------------------------------------------------------
	// SRV: SERVICES
	public static final String SPARK_SESSION_TRAIN = "taoSparkTrainSession";

	// -------------------------------------------------------------------------------------
	// SRV: THREADS
	public static final String PREPARE_CONLL_FROM_TXT_TASK =	"taoPrepareDataTask";
	public static final String TRAIN_NER_MODEL_TASK =	"taoTrainModelTask";
	
		
	
}
