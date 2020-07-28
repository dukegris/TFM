package es.rcs.tfm.srv.services.train;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.model.ArticuloBloque;
import es.rcs.tfm.srv.repository.TrainRepository;
import es.rcs.tfm.srv.setup.TmVarBiocProcessor;
import es.rcs.tfm.srv.setup.TmVarPubtatorProcessor;
import es.rcs.tfm.xml.XmlNames;

@Service(value = SrvNames.TRAINING_SRVC)
@DependsOn(value = {
		SrvNames.SPARK_SESSION_TRAIN,
		XmlNames.BIOC_CONTEXT})
@PropertySource(
		{"classpath:/META-INF/service.properties"} )
public class TrainService {

	private static final Logger LOG = LoggerFactory.getLogger(TrainService.class);

	@Value("${tfm.model.pos.directory:J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/pos}")				
	private String POS_DIRECTORY =   "J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/pos";
	
	@Value("${tfm.model.bert.directory:J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert}")
	String BERT_DIRECTORY =           "J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert";
	
	@Value("${tfm.model.ner.directory:J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner}")
	private String NER_DIRECTORY =   "J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner";

	@Value("${tfm.model.tensorflow.directory:J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/tensorflow}")
	private String TENSORFLOW_DIRECTORY =   "J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/tensorflow";

	@Value("${tfm.dataset.directory:J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets}")
	private String DATASET_DIRECTORY = "J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets";

	@Value("${tfm.pipeline.directory:J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training}")
	private String OUT_PIPELINE_DIRECTORY = "J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training";
	
	@Value("${tfm.conll2003.directory:J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/conll}")		
	String CONLL_DIRECTORY =         "J:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/conll";
	
	@Value("${tfm.conll2003.in.model.pos:pos_anc_en_2.0.2_2.4_1556659930154}")				
	private String CONLL2003_IN_POS_MODEL = "pos_anc_en_2.0.2_2.4_1556659930154";

	@Value("${tfm.conll2003.in.model.bert:bert_base_uncased_en_2.4.0_2.4_1580579889322}")			
	private String CONLL2003_IN_BERT_MODEL = "bert_base_uncased_en_2.4.0_2.4_1580579889322";
	
	@Value("${tfm.conll2003.in.model.bert.MaxSentenceLength:512}")				
	private Integer CONLL2003_IN_BERT_MAX_SENTENCE_LENGHT = 512;
	
	@Value("${tfm.conll2003.in.model.bert.Dimension:1024}")				
	private Integer CONLL2003_IN_BERT_DIMENSION = 1024;
	
	@Value("${tfm.conll2003.in.model.bert.CaseSensitive:true}")				
	private Boolean CONLL2003_IN_BERT_CASE_SENSITIVE = true;
	
	@Value("${tfm.conll2003.in.model.bert.BatchSize:32}")				
	private Integer CONLL2003_IN_BERT_BATCH_SIZE = 32;
	
	@Value("${tfm.conll2003.in.model.bert.PoolingLayer:-1}")				
	private Integer CONLL2003_IN_BERT_POOLING_LAYER = -1;

	@Value("${tfm.conll2003.in.model.ner:tfm_ner_english_M-512_B-256_bert_large_uncased_en}")				
	private String CONLL2003_IN_NER_MODEL = "tfm_ner_english_M-512_B-256_bert_large_uncased_en";
	
	@Value("${tfm.train.ner.model:tfm_ner_mutations_M-512_B-256_bert_large_uncased_en}")				
	private String TRAIN_NER_MODEL = "tfm_ner_mutations_M-512_B-256_bert_large_uncased_en";

	@Value("${tfm.train.ner.MinEpochs:1}")				
	private Integer TRAIN_NER_MIN_EPOCHS = 1;

	@Value("${tfm.train.ner.MaxEpochs:1}")				
	private Integer TRAIN_NER_MAX_EPOCHS = 1;
		
	@Value("${tfm.train.ner.Lr:0.003}")				
	private Float TRAIN_NER_LR = 0.003f;
	
	@Value("${tfm.train.ner.Po:0.005}")				
	private Float TRAIN_NER_PO = 0.005f;
	
	@Value("${tfm.train.ner.DropOut:32}")				
	private Float TRAIN_NER_DROPOUT = 0.5f;

	@Value("${tfm.train.ner.ValidationSplit:0.2}")				
	private Float TRAIN_NER_VALIDATION_SPLIT = 0.2f;
	
	@Value("${tfm.train.ner.BatchSize:32}")				
	private Integer TRAIN_NER_BATCH_SIZE = 32;

	public boolean testPrepareConll(
			String inFileName, 
			String outFileName, 
			String posModelDirectoryName, 
			String bertModelDirectoryName, 
			String nerModelDirectoryName) {

		if (	StringUtils.isBlank(inFileName)) return false;
		if (	StringUtils.isBlank(outFileName)) return false;
		if (	StringUtils.isBlank(posModelDirectoryName)) return false;
		if (	StringUtils.isBlank(bertModelDirectoryName)) return false;
		if (	StringUtils.isBlank(nerModelDirectoryName)) return false;
		
		File inFile = Paths.get(inFileName).toFile();
		if (	(inFile == null) || 
				!inFile.exists() || 
				!inFile.isFile()) {
			
			inFile = Paths.get(FilenameUtils.concat(DATASET_DIRECTORY, inFileName)).toFile();
			if (	(inFile == null) || 
					!inFile.exists() || 
					!inFile.isFile()) 
				return false;
			
		}
	
		File posModelDirectory = Paths.get(FilenameUtils.concat(POS_DIRECTORY, posModelDirectoryName)).toFile();
		if (	(posModelDirectory == null) || 
				!posModelDirectory.exists() || 
				!posModelDirectory.isDirectory()) 
			return false;

		File bertModelDirectory = Paths.get(FilenameUtils.concat(BERT_DIRECTORY, bertModelDirectoryName)).toFile();
		if (	StringUtils.isBlank(bertModelDirectoryName) ||
				(bertModelDirectory == null) || 
				!bertModelDirectory.exists() || 
				!bertModelDirectory.isDirectory())
			return false;

		File nerModelDirectory = Paths.get(FilenameUtils.concat(NER_DIRECTORY, nerModelDirectoryName)).toFile();
		if (	(nerModelDirectory == null) || 
				!nerModelDirectory.exists() || 
				!nerModelDirectory.isDirectory()) 
			return false;
	
		return true;

	}
	
	/**
	 * Genera un fichero CONLL2003 a partir de un fichero de texto en formato BIOC para procesos de extracci�n de entidades
	 * @param spark La instancia de Spark
	 * @param inFileName Fichero BioC
	 * @param outFileName Fichero Conll
	 * @param posModelDirectoryName El modelo POS 
	 * @param bertModelDirectoryName El modelo BERT 
	 * @param nerModelDirectoryName  El modelo NER
	 * @param mantainNerFromGenericModel Mantener los IOB obtenidos del modelo gen�rico de NER
	 */
	public void prepareCoNLL2003DataForTrainingFromBioc(
			SparkSession spark, 
			String inFileName, 
			String outFileName, 
			String posModelDirectoryName, 
			String bertModelDirectoryName, 
			String nerModelDirectoryName,
			Boolean mantainNerFromGenericModel) {

		try {
	
			File inFile = Paths.get(inFileName).toFile();
			if (	(inFile == null) || 
					!inFile.exists() || 
					!inFile.isFile()) {
				
				inFile = Paths.get(FilenameUtils.concat(DATASET_DIRECTORY, inFileName)).toFile();
				if (	(inFile == null) || 
						!inFile.exists() || 
						!inFile.isFile()) {
					
					LOG.warn("PREPARE DATA SERVICE: bioc FAIL - IN FILE MUST EXITS");
					return;
					
				}
			}

			File outFile = Paths.get(FilenameUtils.concat(CONLL_DIRECTORY, outFileName)).toFile();
			if (	StringUtils.isBlank(outFileName))  {
				LOG.warn("PREPARE DATA SERVICE: bioc FAIL - OUT FILENAME CAN'T BE BLANK");
				return;
			}
			
			File posModelDirectory = Paths.get(FilenameUtils.concat(POS_DIRECTORY, posModelDirectoryName)).toFile();
			if (	StringUtils.isBlank(posModelDirectoryName) ||
					(posModelDirectory == null) || 
					!posModelDirectory.exists() || 
					!posModelDirectory.isDirectory()) 
				posModelDirectory = Paths.get(FilenameUtils.concat(POS_DIRECTORY, CONLL2003_IN_POS_MODEL)).toFile();

			Integer bertMaxSentenceLength = CONLL2003_IN_BERT_MAX_SENTENCE_LENGHT;
			Integer bertDimension = CONLL2003_IN_BERT_DIMENSION;
			Integer bertBatchSize = CONLL2003_IN_BERT_BATCH_SIZE;
			Boolean bertCaseSensitive = CONLL2003_IN_BERT_CASE_SENSITIVE;
			Integer bertPoolingLayer = CONLL2003_IN_BERT_POOLING_LAYER;

			File bertModelDirectory = Paths.get(FilenameUtils.concat(BERT_DIRECTORY, bertModelDirectoryName)).toFile();
			if (	StringUtils.isBlank(bertModelDirectoryName) ||
					(bertModelDirectory == null) || 
					!bertModelDirectory.exists() || 
					!bertModelDirectory.isDirectory()) {
				bertModelDirectory = Paths.get(FilenameUtils.concat(BERT_DIRECTORY, CONLL2003_IN_BERT_MODEL)).toFile();
			} else {
				if (bertModelDirectoryName.contains("_base_")) bertDimension = 768;
				if (bertModelDirectoryName.contains("_large_")) bertDimension = 1024;
				if (bertModelDirectoryName.contains("_cased_")) bertCaseSensitive = true;
				if (bertModelDirectoryName.contains("_uncased_")) bertCaseSensitive = false;
			}

			File nerModelDirectory = Paths.get(FilenameUtils.concat(NER_DIRECTORY, nerModelDirectoryName)).toFile();
			if (	StringUtils.isBlank(nerModelDirectoryName) ||
					(nerModelDirectory == null) || 
					!nerModelDirectory.exists() || 
					!nerModelDirectory.isDirectory()) 
				nerModelDirectory = Paths.get(FilenameUtils.concat(NER_DIRECTORY, CONLL2003_IN_NER_MODEL)).toFile();

			boolean result = TrainRepository.getConllFrom(
					spark, 
					new TmVarBiocProcessor(inFile.toPath()), 
					inFile,
					posModelDirectory,
					bertModelDirectory, 
					nerModelDirectory,
					outFile,
					mantainNerFromGenericModel,
					bertMaxSentenceLength,
					bertDimension,
					bertBatchSize,
					bertCaseSensitive,
					bertPoolingLayer);
			if (result) {
				LOG.info("PREPARE DATA SERVICE: bioc OK");
			} else {
				LOG.warn("PREPARE DATA SERVICE: bioc FAIL");
			}
			
		} catch (Exception ex) {
			LOG.error("PREPARE DATA SERVICE: bioc FAIL - ex:" + ex.toString());
		}
		
	}

	/**
	 * Genera un fichero CONLL2003 a partir de un fichero de texto en formato PUBTATOR para procesos de extracci�n de entidades
	 * @param spark La instancia de Spark
	 * @param inFileName Fichero Pubtator
	 * @param outFileName Fichero Conll
	 * @param posModelDirectoryName El modelo POS 
	 * @param bertModelDirectoryName El modelo BERT 
	 * @param nerModelDirectoryName  El modelo NER
	 * @param mantainNerFromGenericModel Mantener los IOB obtenidos del modelo gen�rico de NER
	 */
	public void prepareCoNLL2003DataForTrainingFromPubtator(
			SparkSession spark, 
			String inFileName, 
			String outFileName, 
			String posModelDirectoryName, 
			String bertModelDirectoryName, 
			String nerModelDirectoryName,
			Boolean mantainNerFromGenericModel) {
		
		try {
			
			File inFile = Paths.get(inFileName).toFile();
			if (	(inFile == null) || 
					!inFile.exists() || 
					!inFile.isFile()) {
				
				inFile = Paths.get(FilenameUtils.concat(DATASET_DIRECTORY, inFileName)).toFile();
				if (	(inFile == null) || 
						!inFile.exists() || 
						!inFile.isFile()) {

					LOG.warn("PREPARE DATA SERVICE: txt FAIL - IN FILE MUST EXITS");
					return;

				}
			}

			File outFile = Paths.get(FilenameUtils.concat(CONLL_DIRECTORY, outFileName)).toFile();
			if (	StringUtils.isBlank(outFileName))  {
				LOG.warn("PREPARE DATA SERVICE: txt FAIL - OUT FILE  CAN'T BE BLANK");
				return;
			}

			File posModelDirectory = Paths.get(FilenameUtils.concat(POS_DIRECTORY, posModelDirectoryName)).toFile();
			if (	StringUtils.isBlank(posModelDirectoryName) ||
					(posModelDirectory == null) || 
					!posModelDirectory.exists() || 
					!posModelDirectory.isDirectory()) 
				posModelDirectory = Paths.get(FilenameUtils.concat(POS_DIRECTORY, CONLL2003_IN_POS_MODEL)).toFile();

			// https://github.com/JohnSnowLabs/spark-nlp/issues/570#issuecomment-524286943
			Integer bertMaxSentenceLength = CONLL2003_IN_BERT_MAX_SENTENCE_LENGHT;
			Integer bertDimension = CONLL2003_IN_BERT_DIMENSION;
			Integer bertBatchSize = CONLL2003_IN_BERT_BATCH_SIZE;
			Boolean bertCaseSensitive = CONLL2003_IN_BERT_CASE_SENSITIVE;
			Integer bertPoolingLayer = CONLL2003_IN_BERT_POOLING_LAYER;

			File bertModelDirectory = Paths.get(FilenameUtils.concat(BERT_DIRECTORY, bertModelDirectoryName)).toFile();
			if (	StringUtils.isBlank(bertModelDirectoryName) ||
					(bertModelDirectory == null) || 
					!bertModelDirectory.exists() || 
					!bertModelDirectory.isDirectory()) {
				bertModelDirectory = Paths.get(FilenameUtils.concat(BERT_DIRECTORY, CONLL2003_IN_BERT_MODEL)).toFile();
			} else {
				if (bertModelDirectoryName.contains("_base_")) bertDimension = 768;
				if (bertModelDirectoryName.contains("_large_")) bertDimension = 1024;
				if (bertModelDirectoryName.contains("_cased_")) bertCaseSensitive = true;
				if (bertModelDirectoryName.contains("_uncased_")) bertCaseSensitive = false;
			}

			File nerModelDirectory = Paths.get(FilenameUtils.concat(NER_DIRECTORY, nerModelDirectoryName)).toFile();
			if (	StringUtils.isBlank(nerModelDirectoryName) ||
					(nerModelDirectory == null) || 
					!nerModelDirectory.exists() || 
					!nerModelDirectory.isDirectory()) 
				nerModelDirectory = Paths.get(FilenameUtils.concat(NER_DIRECTORY, CONLL2003_IN_NER_MODEL)).toFile();
	
			boolean result = TrainRepository.getConllFrom(
					spark, 
					new TmVarPubtatorProcessor(inFile.toPath()), 
					inFile,
					posModelDirectory,
					bertModelDirectory, 
					nerModelDirectory,
					outFile,
					mantainNerFromGenericModel,
					bertMaxSentenceLength,
					bertDimension,
					bertBatchSize,
					bertCaseSensitive,
					bertPoolingLayer);
			
			if (result) {
				LOG.info("PREPARE DATA SERVICE: txt OK");
			} else {
				LOG.info("PREPARE DATA SERVICE: txt FAIL");
			}
		} catch (Exception ex) {
			LOG.warn("PREPARE DATA SERVICE: txt FAIL - ex:" + ex.toString());
		}
		
	}

	public void trainModel(SparkSession spark) {
		
		trainModel(
				spark,
				CONLL_DIRECTORY,
				FilenameUtils.concat(BERT_DIRECTORY, CONLL2003_IN_BERT_MODEL),
				CONLL2003_IN_BERT_MAX_SENTENCE_LENGHT,
				CONLL2003_IN_BERT_DIMENSION,
				CONLL2003_IN_BERT_BATCH_SIZE,
				CONLL2003_IN_BERT_CASE_SENSITIVE,
				CONLL2003_IN_BERT_POOLING_LAYER,
				FilenameUtils.concat(NER_DIRECTORY, TRAIN_NER_MODEL),
				TENSORFLOW_DIRECTORY,
				TRAIN_NER_MIN_EPOCHS,
				TRAIN_NER_MAX_EPOCHS,
				TRAIN_NER_LR,
				TRAIN_NER_PO,
				TRAIN_NER_DROPOUT,
				TRAIN_NER_VALIDATION_SPLIT,
				TRAIN_NER_BATCH_SIZE);

	}

	public void trainModel(
			SparkSession spark, 
			String conllTrainDirectoryName,
			String bertModelDirectoryName,
			Integer bertMaxSentenceLength,
			Integer bertDimension,
			Integer bertBatchSize,
			Boolean bertCaseSensitive,
			Integer bertPoolingLayer,
			String nerModelDirectoryName,
			String nerTensorFlowGraphDirectoryName,
			Integer nerTfMinEpochs,
			Integer nerTfMaxEpochs,
			Float nerTfLr,
			Float nerTfPo,
			Float nerTfDropOut,
			Float nerTfValidationSplit,
			Integer nerTfBatchSize) {
		
		File conllTrainDirectory = Paths.get(conllTrainDirectoryName).toFile();
		if (	StringUtils.isBlank(conllTrainDirectoryName) ||
				(conllTrainDirectory == null) || 
				!conllTrainDirectory.exists() || 
				!conllTrainDirectory.isDirectory()) 
			conllTrainDirectory = Paths.get(CONLL_DIRECTORY).toFile();

		File bertModelDirectory = Paths.get(bertModelDirectoryName).toFile();
		if (	StringUtils.isBlank(bertModelDirectoryName) ||
				(bertModelDirectory == null) || 
				!bertModelDirectory.exists() || 
				!bertModelDirectory.isDirectory()) 
			bertModelDirectory = Paths.get(FilenameUtils.concat(BERT_DIRECTORY, CONLL2003_IN_BERT_MODEL)).toFile();
		
		File nerTensorFlowGraphDirectory = Paths.get(nerTensorFlowGraphDirectoryName).toFile();
		if (	StringUtils.isBlank(bertModelDirectoryName) ||
				(nerTensorFlowGraphDirectory == null) || 
				!nerTensorFlowGraphDirectory.exists() || 
				!nerTensorFlowGraphDirectory.isDirectory()) 
			nerTensorFlowGraphDirectory = Paths.get(TENSORFLOW_DIRECTORY).toFile();
		
		File nerModelDirectory = Paths.get(nerModelDirectoryName).toFile();
		if (	StringUtils.isBlank(bertModelDirectoryName) ||
				(nerModelDirectory == null) || 
				!nerModelDirectory.exists() || 
				!nerModelDirectory.isDirectory()) 
			nerModelDirectory = Paths.get(FilenameUtils.concat(NER_DIRECTORY, TRAIN_NER_MODEL)).toFile();

		if (bertMaxSentenceLength == null) bertMaxSentenceLength = CONLL2003_IN_BERT_MAX_SENTENCE_LENGHT;
		if (bertDimension == null) bertDimension = CONLL2003_IN_BERT_DIMENSION;
		if (bertBatchSize == null) bertBatchSize = CONLL2003_IN_BERT_BATCH_SIZE;
		if (bertCaseSensitive == null) bertCaseSensitive = CONLL2003_IN_BERT_CASE_SENSITIVE;
		if (bertPoolingLayer == null) bertPoolingLayer = CONLL2003_IN_BERT_POOLING_LAYER;
		if (nerTfMinEpochs == null) nerTfMinEpochs = TRAIN_NER_MIN_EPOCHS;
		if (nerTfMaxEpochs == null) nerTfMaxEpochs = TRAIN_NER_MAX_EPOCHS;
		if (nerTfLr == null) nerTfLr = TRAIN_NER_LR;
		if (nerTfPo == null) nerTfPo = TRAIN_NER_PO;
		if (nerTfDropOut == null) nerTfDropOut = TRAIN_NER_DROPOUT;
		if (nerTfValidationSplit == null) nerTfValidationSplit = TRAIN_NER_VALIDATION_SPLIT;
		if (nerTfBatchSize == null) nerTfBatchSize = TRAIN_NER_BATCH_SIZE;
		
		try {
			
			boolean result = TrainRepository.trainModelFromConll(
					spark, 
					conllTrainDirectory, 
					bertModelDirectory,
					bertMaxSentenceLength,
					bertDimension,
					bertBatchSize,
					bertCaseSensitive,
					bertPoolingLayer,
					nerModelDirectory,
					nerTensorFlowGraphDirectory,
					nerTfMinEpochs,
					nerTfMaxEpochs,
					nerTfLr,
					nerTfPo,
					nerTfDropOut,
					nerTfValidationSplit,
					nerTfBatchSize);

			if (result) {
				LOG.info("TRAIN SERVICE: OK");
			} else {
				LOG.warn("TRAIN SERVICE: FAIL");
			}
		} catch (Exception ex) {
			LOG.error("TRAIN SERVICE: FAIL - ex:" + ex.toString());
		}
		
	}	

	public void trainModel(
			SparkSession spark, 
			String trainFileName,
			String testFileName,
			String bertModelDirectoryName,
			Integer bertMaxSentenceLength,
			Integer bertDimension,
			Integer bertBatchSize,
			Boolean bertCaseSensitive,
			Integer bertPoolingLayer,
			String nerModel,
			String nerTensorFlowGraph,
			Integer nerTfMinEpochs,
			Integer nerTfMaxEpochs,
			Float nerTfLr,
			Float nerTfPo,
			Float nerTfDropOut,
			Float nerTfValidationSplit,
			Integer nerTfBatchSize) {
		
		File trainFile = Paths.get(trainFileName).toFile();
		if (	StringUtils.isBlank(trainFileName) ||
				(trainFile == null) || 
				!trainFile.exists() || 
				!trainFile.isFile()) 
			return;
		
		File testFile = Paths.get(testFileName).toFile();
		if (	StringUtils.isBlank(testFileName) ||
				(testFile == null) || 
				!testFile.exists() || 
				!testFile.isFile()) 
			return;

		File bertModelDirectory = Paths.get(bertModelDirectoryName).toFile();
		if (	StringUtils.isBlank(bertModelDirectoryName) ||
				(bertModelDirectory == null) || 
				!bertModelDirectory.exists() || 
				!bertModelDirectory.isDirectory()) 
			bertModelDirectory = Paths.get(FilenameUtils.concat(BERT_DIRECTORY, CONLL2003_IN_BERT_MODEL)).toFile();
		
		File nerModelDirectory = Paths.get(nerModel).toFile();
		if (	StringUtils.isBlank(bertModelDirectoryName) ||
				(nerModelDirectory == null) || 
				!nerModelDirectory.exists() || 
				!nerModelDirectory.isDirectory()) 
			nerModelDirectory = Paths.get(FilenameUtils.concat(NER_DIRECTORY, TRAIN_NER_MODEL)).toFile();
		
		File nerTensorFlowGraphDirectory = Paths.get(nerTensorFlowGraph).toFile();
		if (	StringUtils.isBlank(bertModelDirectoryName) ||
				(nerTensorFlowGraphDirectory == null) || 
				!nerTensorFlowGraphDirectory.exists() || 
				!nerTensorFlowGraphDirectory.isDirectory()) 
			nerTensorFlowGraphDirectory = Paths.get(TENSORFLOW_DIRECTORY).toFile();

		if (bertMaxSentenceLength == null) bertMaxSentenceLength = CONLL2003_IN_BERT_MAX_SENTENCE_LENGHT;
		if (bertDimension == null) bertDimension = CONLL2003_IN_BERT_DIMENSION;
		if (bertBatchSize == null) bertBatchSize = CONLL2003_IN_BERT_BATCH_SIZE;
		if (bertCaseSensitive == null) bertCaseSensitive = CONLL2003_IN_BERT_CASE_SENSITIVE;
		if (nerTfMinEpochs == null) nerTfMinEpochs = TRAIN_NER_MIN_EPOCHS;
		if (nerTfMaxEpochs == null) nerTfMaxEpochs = TRAIN_NER_MAX_EPOCHS;
		if (nerTfLr == null) nerTfLr = TRAIN_NER_LR;
		if (nerTfPo == null) nerTfPo = TRAIN_NER_PO;
		if (nerTfDropOut == null) nerTfDropOut = TRAIN_NER_DROPOUT;
		if (nerTfValidationSplit == null) nerTfValidationSplit = TRAIN_NER_VALIDATION_SPLIT;
		if (nerTfBatchSize == null) nerTfBatchSize = TRAIN_NER_BATCH_SIZE;

		try {
			
			boolean result = TrainRepository.trainModelFromConll(
					spark, 
					trainFile,
					testFile,
					bertModelDirectory,
					bertMaxSentenceLength,
					bertDimension,
					bertBatchSize,
					bertCaseSensitive,
					bertPoolingLayer,
					nerModelDirectory,
					nerTensorFlowGraphDirectory,
					nerTfMinEpochs,
					nerTfMaxEpochs,
					nerTfLr,
					nerTfPo,
					nerTfDropOut,
					nerTfValidationSplit,
					nerTfBatchSize);

			if (result) {
				LOG.info("TRAIN SERVICE: OK");
			} else {
				LOG.info("TRAIN SERVICE: FAIL");
			}
		} catch (Exception ex) {
			LOG.warn("TRAIN SERVICE: FAIL - ex:" + ex.toString());
		}
		
	}

	public List<ArticuloBloque> process(
			SparkSession spark, 
			List<Articulo> articles) {

		if (	(articles == null) ||
				(articles.isEmpty())) return null;

		List<ArticuloBloque>resultado = null;
		try {
			
			File posModelDirectory = Paths.get(FilenameUtils.concat(POS_DIRECTORY, CONLL2003_IN_POS_MODEL)).toFile();
			File bertModelDirectory = Paths.get(FilenameUtils.concat(BERT_DIRECTORY, CONLL2003_IN_BERT_MODEL)).toFile();
			File nerModelDirectory = Paths.get(FilenameUtils.concat(NER_DIRECTORY, TRAIN_NER_MODEL)).toFile();
			File outPipelineDirectory = Paths.get(OUT_PIPELINE_DIRECTORY).toFile();

			resultado = TrainRepository.getProcessedBlocks(
					spark, 
					articles,
					posModelDirectory,
					bertModelDirectory,
					nerModelDirectory,
					outPipelineDirectory,
					CONLL2003_IN_BERT_MAX_SENTENCE_LENGHT,
					CONLL2003_IN_BERT_DIMENSION,
					CONLL2003_IN_BERT_BATCH_SIZE,
					CONLL2003_IN_BERT_CASE_SENSITIVE,
					CONLL2003_IN_BERT_POOLING_LAYER);

			if ((resultado == null) || (resultado.isEmpty())) {
				LOG.info("PROCESS SERVICE: OK");
			} else {
				LOG.info("PROCESS SERVICE: FAIL");
			}
		} catch (Exception ex) {
			LOG.warn("PROCESS SERVICE: FAIL - ex:" + ex.toString());
		}

		return resultado;

	}	

}
