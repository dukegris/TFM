package es.rcs.tfm.srv.train.services;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import es.rcs.tfm.srv.repository.TrainRepository;
import es.rcs.tfm.srv.setup.TmBiocXmlProcessor;
import es.rcs.tfm.srv.setup.TmVarTxtProcessor;
import es.rcs.tfm.xml.XmlNames;

@Service(value = SrvNames.TRAINING_SRVC)
@DependsOn(value = {
		SrvNames.SPARK_SESSION_TRAIN,
		XmlNames.BIOC_CONTEXT})
@PropertySource(
		{"classpath:/META-INF/service.properties"} )
public class TrainService {

	private static final Logger LOG = LoggerFactory.getLogger(TrainService.class);

	private @Value("${tfm.model.pos.directory}")				String POS_DIRECTORY =				"/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/pos";
	private @Value("${tfm.model.bert.directory}")				String BERT_DIRECTORY =				"/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert";
	private @Value("${tfm.model.ner.directory}")				String NER_DIRECTORY =				"/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner";

	private @Value("${tfm.training.ner.in.train.pubtator}")		String TRAIN_NER_IN_TXT_TRAIN =		"/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/tmVar/tmVarCorpus/train.PubTator.txt";
	private @Value("${tfm.training.ner.in.train.bioc}")			String TRAIN_NER_IN_BIOC_TRAIN =	"/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/tmVar/tmVarCorpus/train.BioC.xml";
	private @Value("${tfm.training.ner.in.test.pubtator}")		String TRAIN_NER_IN_PUBTATOR_TEST =	"/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/tmVar/tmVarCorpus/test.PubTator.txt";
	private @Value("${tfm.training.ner.in.test.bioc}")			String TRAIN_NER_IN_BIOC_TEST =		"/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/datasets/tmVar/tmVarCorpus/test.BioC.xml";
	private @Value("${tfm.training.ner.in.model.pos}")			String TRAIN_NER_IN_POS_MODEL =		"pos_anc_en_2.0.2_2.4_1556659930154";
	private @Value("${tfm.training.ner.in.model.bert}")			String TRAIN_NER_IN_BERT_MODEL =	"bert_base_cased_en_2.2.0_2.4_1566671427398";
	private @Value("${tfm.training.ner.in.model.ner}")			String TRAIN_NER_IN_NER_MODEL =		"ner_dl_bert_base_cased_en_2.2.0_2.4_1567854461249";
	
	private @Value("${tfm.training.ner.out.mantain-iob}")		Boolean TRAIN_NER_OUT_GEN_IOB =		false;
	private @Value("${tfm.training.ner.out.directory}")			String TRAIN_NER_OUT_DIRECTORY =	"/home/rcuesta/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner";
	private @Value("${tfm.training.ner.out.train_txt.conll}")	String TRAIN_NER_OUT_TXT_TRAIN =	"ner_txt_train.conll";
	private @Value("${tfm.training.ner.out.test_txt.conll}")	String TRAIN_NER_OUT_TXT_TEST =		"ner_txt_test.conll";
	private @Value("${tfm.training.ner.out.train_bioc.conll}")	String TRAIN_NER_OUT_BIOC_TRAIN =	"ner_bioc_train.conll";
	private @Value("${tfm.training.ner.out.test_bioc.conll}")	String TRAIN_NER_OUT_BIOC_TEST =	"ner_bioc_test.conll";
	private @Value("${tfm.training.ner.out.txt.model}")			String TRAIN_NER_OUT_TXT_MODEL =	"tfm_ner_txt_1.0.0";
	private @Value("${tfm.training.ner.out.bioc.model}")		String TRAIN_NER_OUT_BIOC_MODEL =	"tfm_ner_bioc_1.0.0";

	public void trainModel(SparkSession spark) {
		
		trainModel(
				spark,
				FilenameUtils.concat(TRAIN_NER_OUT_DIRECTORY, TRAIN_NER_OUT_TXT_TRAIN), 
				FilenameUtils.concat(TRAIN_NER_OUT_DIRECTORY, TRAIN_NER_OUT_TXT_TEST), 
				FilenameUtils.concat(TRAIN_NER_OUT_DIRECTORY, TRAIN_NER_OUT_TXT_MODEL),
				TRAIN_NER_IN_BERT_MODEL);
		
		trainModel(
				spark,
				FilenameUtils.concat(TRAIN_NER_OUT_DIRECTORY, TRAIN_NER_OUT_BIOC_TRAIN), 
				FilenameUtils.concat(TRAIN_NER_OUT_DIRECTORY, TRAIN_NER_OUT_BIOC_TEST), 
				FilenameUtils.concat(TRAIN_NER_OUT_DIRECTORY, TRAIN_NER_OUT_BIOC_MODEL),
				TRAIN_NER_IN_BERT_MODEL);
		
	}
	
	public void prepareDataForTraining(SparkSession spark) {

		prepareDataForTrainingFromPubtator(
				spark,
				TRAIN_NER_IN_TXT_TRAIN, 
				FilenameUtils.concat(TRAIN_NER_OUT_DIRECTORY, TRAIN_NER_OUT_TXT_TRAIN),
				TRAIN_NER_IN_BERT_MODEL,
				TRAIN_NER_IN_NER_MODEL,
				TRAIN_NER_OUT_GEN_IOB);
		
		prepareDataForTrainingFromPubtator(
				spark,
				TRAIN_NER_IN_PUBTATOR_TEST, 
				FilenameUtils.concat(TRAIN_NER_OUT_DIRECTORY, TRAIN_NER_OUT_TXT_TEST),
				TRAIN_NER_IN_BERT_MODEL,
				TRAIN_NER_IN_NER_MODEL,
				TRAIN_NER_OUT_GEN_IOB);
		
		prepareDataForTrainingFromBioc(
				spark,
				TRAIN_NER_IN_BIOC_TRAIN, 
				FilenameUtils.concat(TRAIN_NER_OUT_DIRECTORY, TRAIN_NER_OUT_BIOC_TRAIN),
				TRAIN_NER_IN_BERT_MODEL,
				TRAIN_NER_IN_NER_MODEL,
				TRAIN_NER_OUT_GEN_IOB);
		
		prepareDataForTrainingFromBioc(
				spark,
				TRAIN_NER_IN_BIOC_TEST, 
				FilenameUtils.concat(TRAIN_NER_OUT_DIRECTORY, TRAIN_NER_OUT_BIOC_TEST),
				TRAIN_NER_IN_BERT_MODEL,
				TRAIN_NER_IN_NER_MODEL,
				TRAIN_NER_OUT_GEN_IOB);

	}

	// L: Layers
	// H: Dimension
	// M: MaxSentence
	// B: BatchSize
	// cased_L-12_H-768_A-12_M-512_B-32
	// cased_L-24_H-1024_A-16_M-512_B-32
	// multi_cased_L-12_H-768_A-12_M-512_B-32
	private static final Pattern MODEL_NAME=Pattern.compile("((?:(?:un)|(?:multi-))cased)_L-(\\d+)_H-(\\d+)_A-(\\d+)_M-(\\d+)_B-(\\d+)");
	private static Integer getData(String name, Integer pos) {
		Integer result = -1;
		try {
			Matcher m = MODEL_NAME.matcher(name);
			if (m.find()) {
				result = Integer.parseInt(m.group(pos));
			}
		} catch (NumberFormatException | IndexOutOfBoundsException ex) {
		}
		return result;
	}

	private static Integer getMaxSentence(String name) {
		Integer result = getData(name, 5);
		//if (result == -1) result = 512;
		if (result == -1) result = 512;
		return result;
	}
	private static Integer getDimension(String name) {
		Integer result = getData(name, 3);
		//if (result == -1) result = 1024;
		if (result == -1) result = 768;
		return result;
	}
	private static Integer getBatchSize(String name) {
		Integer result = getData(name, 6);
		//if (result == -1) result = 32;
		if (result == -1) result = 32;
		return result;
	}
	private static Boolean getCaseSensitive(String name) {
		Boolean result = true;
		try {
			Matcher m = MODEL_NAME.matcher(name);
			if (m.find()) {
				result = "uncased".equals(m.group(1)) ? false : true;
			}
		} catch (NumberFormatException | IndexOutOfBoundsException ex) {
		}
		return result;
	}
	public void trainModel(SparkSession spark, String trainfile, String testfile, String outdir, String bertmodel) {
		
		Path outdirname = Paths.get(outdir);
		Path filename = Paths.get(trainfile);
		
		File bertmodelDirectory = Paths.get(FilenameUtils.concat(BERT_DIRECTORY, bertmodel)).toFile();
		if (	StringUtils.isBlank(bertmodel) ||
				(bertmodelDirectory == null) || 
				!bertmodelDirectory.exists() || 
				!bertmodelDirectory.isDirectory()) 
			bertmodelDirectory = Paths.get(FilenameUtils.concat(BERT_DIRECTORY, TRAIN_NER_IN_BERT_MODEL)).toFile();
		
		try {
			boolean result = TrainRepository.trainFromConll(
					spark, 
					trainfile, 
					testfile, 
					outdirname.toFile().getName() + "_" + filename.toFile().getName() + ".csv", 
					outdirname.toFile().getName() + "_" + filename.toFile().getName() + ".pipeline9", 
					FilenameUtils.concat(POS_DIRECTORY, TRAIN_NER_IN_POS_MODEL),
					bertmodelDirectory.getAbsolutePath(), 
					outdir,
					getMaxSentence(bertmodel),
					getDimension(bertmodel),
					getBatchSize(bertmodel),
					getCaseSensitive(bertmodel));
			if (result) {
				LOG.info("TRAIN SERVICE: OK");
			} else {
				LOG.info("TRAIN SERVICE: FAIL");
			}
		} catch (Exception ex) {
			LOG.warn("TRAIN SERVICE: FAIL - ex:" + ex.toString());
		}
		
	}

	/**
	 * Genera un fichero CONLL2003 a partir de un fichero de texto en formato BIOC para procesos de extracciï¿½n de entidades
	 * @param spark La instancia de Spark
	 * @param infile Fichero BioC
	 * @param outfile Fichero Conll
	 * @param bertmodel El modelo BERT 
	 * @param nermodel  El modelo NER
	 * @param mantainNerFromGenericModel Mantener los IOB obtenidos del modelo genérico de NER
	 */
	public void prepareDataForTrainingFromBioc(
			SparkSession spark, 
			String infile, 
			String outfile, 
			String bertmodel, 
			String nermodel,
			Boolean mantainNerFromGenericModel) {

		try {

			Path filename = Paths.get(infile);
			
			File bertmodelDirectory = Paths.get(FilenameUtils.concat(BERT_DIRECTORY, bertmodel)).toFile();
			if (	StringUtils.isBlank(bertmodel) ||
					(bertmodelDirectory == null) || 
					!bertmodelDirectory.exists() || 
					!bertmodelDirectory.isDirectory()) 
				bertmodelDirectory = Paths.get(FilenameUtils.concat(BERT_DIRECTORY, TRAIN_NER_IN_BERT_MODEL)).toFile();

			File nermodelDirectory = Paths.get(FilenameUtils.concat(NER_DIRECTORY, nermodel)).toFile();
			if (	StringUtils.isBlank(nermodel) ||
					(nermodelDirectory == null) || 
					!nermodelDirectory.exists() || 
					!nermodelDirectory.isDirectory()) 
				nermodelDirectory = Paths.get(FilenameUtils.concat(NER_DIRECTORY, TRAIN_NER_IN_NER_MODEL)).toFile();
			
			boolean result = TrainRepository.getConllFrom(
					spark, 
					new TmBiocXmlProcessor(filename), 
					infile, 
					FilenameUtils.concat(POS_DIRECTORY, TRAIN_NER_IN_POS_MODEL),
					bertmodelDirectory.getAbsolutePath(), 
					nermodelDirectory.getAbsolutePath(),
					outfile,
					mantainNerFromGenericModel,
					getMaxSentence(bertmodel),
					getDimension(bertmodel),
					getBatchSize(bertmodel),
					getCaseSensitive(bertmodel));
			if (result) {
				LOG.info("PREPARE DATA SERVICE: bioc OK");
			} else {
				LOG.info("PREPARE DATA SERVICE: bioc FAIL");
			}
			
		} catch (Exception ex) {
			LOG.warn("PREPARE DATA SERVICE: bioc FAIL - ex:" + ex.toString());
		}
		
	}

	/**
	 * Genera un fichero CONLL2003 a partir de un fichero de texto en formato PUBTATOR para procesos de extracciï¿½n de entidades
	 * @param spark La instancia de Spark
	 * @param infile Fichero Pubtator
	 * @param outfile Fichero Conll
	 * @param bertmodel El modelo BERT 
	 * @param nermodel  El modelo NER
	 * @param mantainNerFromGenericModel Mantener los IOB obtenidos del modelo genérico de NER
	 */
	public void prepareDataForTrainingFromPubtator(
			SparkSession spark, 
			String infile, 
			String outfile, 
			String bertmodel, 
			String nermodel,
			Boolean mantainNerFromGenericModel) {
		
		try {
			
			Path filename = Paths.get(infile);
			
			File bertmodelDirectory = Paths.get(FilenameUtils.concat(BERT_DIRECTORY, bertmodel)).toFile();
			if (	StringUtils.isBlank(bertmodel) ||
					(bertmodelDirectory == null) || 
					!bertmodelDirectory.exists() || 
					!bertmodelDirectory.isDirectory()) 
				bertmodelDirectory = Paths.get(FilenameUtils.concat(BERT_DIRECTORY, TRAIN_NER_IN_BERT_MODEL)).toFile();

			File nermodelDirectory = Paths.get(FilenameUtils.concat(NER_DIRECTORY, nermodel)).toFile();
			if (	StringUtils.isBlank(nermodel) ||
					(nermodelDirectory == null) || 
					!nermodelDirectory.exists() || 
					!nermodelDirectory.isDirectory()) 
				nermodelDirectory = Paths.get(FilenameUtils.concat(NER_DIRECTORY, TRAIN_NER_IN_NER_MODEL)).toFile();
			
			boolean result = TrainRepository.getConllFrom(
					spark, 
					new TmVarTxtProcessor(filename), 
					infile, 
					FilenameUtils.concat(POS_DIRECTORY, TRAIN_NER_IN_POS_MODEL),
					bertmodelDirectory.getAbsolutePath(), 
					nermodelDirectory.getAbsolutePath(),
					outfile,
					mantainNerFromGenericModel,
					getMaxSentence(bertmodel),
					getDimension(bertmodel),
					getBatchSize(bertmodel),
					getCaseSensitive(bertmodel));
			
			if (result) {
				LOG.info("PREPARE DATA SERVICE: txt OK");
			} else {
				LOG.info("PREPARE DATA SERVICE: txt FAIL");
			}
		} catch (Exception ex) {
			LOG.warn("PREPARE DATA SERVICE: txt FAIL - ex:" + ex.toString());
		}
		
	}
	
}
