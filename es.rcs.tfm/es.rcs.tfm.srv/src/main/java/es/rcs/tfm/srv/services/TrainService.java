package es.rcs.tfm.srv.services;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.repository.TrainRepository;
import es.rcs.tfm.srv.setup.BiocXmlProcessor;
import es.rcs.tfm.srv.setup.Conll2003Writer;
import es.rcs.tfm.srv.setup.PubtatorTxtProcessor;
import es.rcs.tfm.xml.XmlNames;

@Service(value = SrvNames.TRAINING_SRVC)
@DependsOn(value = {
		SrvNames.SPARK_SESSION_TRAIN,
		XmlNames.BIOC_CONTEXT})
@PropertySource(
		{"classpath:/META-INF/service.properties"} )
public class TrainService {

	private static final Logger LOG = LoggerFactory.getLogger(TrainService.class);

	private @Value("${tfm.model.pos_asc.directory}")		String POS_MODEL =					"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/pos_anc_en_2.0.2_2.4_1556659930154";
	private @Value("${tfm.model.bert_uncased.directory}")	String BERT_UNCASED_MODEL =			"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert_uncased_en_2.0.2_2.4_1556651478920";
	private @Value("${tfm.model.bert_ner.directory}")		String BERT_NER_MODEL =				"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner_dl_bert_en_2.0.2_2.4_1558809068913";
	private @Value("${tfm.model.tfm.directory}")			String TFM_NER_MODEL =				"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/tfm_1.0.0";

	private @Value("${tfm.training.ner.train.pubtator}")	String TRAIN_NER_PUBTATOR_TRAIN =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmVar/train.PubTator";
	private @Value("${tfm.training.ner.test.pubtator}")		String TRAIN_NER_PUBTATOR_TEST =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmVar/test.PubTator";

	private @Value("${tfm.training.ner.train.bioc}")		String TRAIN_NER_BIOC_TRAIN =		"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmVar/train.BioC.xml";
	private @Value("${tfm.training.ner.test.bioc}")			String TRAIN_NER_BIOC_XML_TEST =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmVar/test.Bioc.xml";
			
	private @Value("${tfm.training.ner.directory}")			String TRAIN_NER_DIRECTORY =		"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner";
	
	private static final String NER_TXT_TRAIN =		"ner_txt_train" + Conll2003Writer.CONLL_EXT;
	private static final String NER_TXT_TEST =		"ner_txt_test" + Conll2003Writer.CONLL_EXT;
	private static final String NER_BIOC_TRAIN =	"ner_bioc_train" + Conll2003Writer.CONLL_EXT;
	private static final String NER_BIOC_TEST =		"ner_bioc_test" + Conll2003Writer.CONLL_EXT;

	public void trainModel(SparkSession spark) {
		
		trainModel(
				spark,
				FilenameUtils.concat(TRAIN_NER_DIRECTORY, NER_TXT_TRAIN), 
				FilenameUtils.concat(TRAIN_NER_DIRECTORY, NER_TXT_TEST), 
				TFM_NER_MODEL);
		
		trainModel(
				spark,
				FilenameUtils.concat(TRAIN_NER_DIRECTORY, NER_BIOC_TRAIN), 
				FilenameUtils.concat(TRAIN_NER_DIRECTORY, NER_BIOC_TEST), 
				TFM_NER_MODEL);
		
	}
	
	public void prepareDataForTraining(SparkSession spark) {

		prepareDataForTrainingFromPubtator(
				spark,
				TRAIN_NER_PUBTATOR_TRAIN, 
				FilenameUtils.concat(TRAIN_NER_DIRECTORY, NER_TXT_TRAIN));
		
		prepareDataForTrainingFromPubtator(
				spark,
				TRAIN_NER_PUBTATOR_TEST, 
				FilenameUtils.concat(TRAIN_NER_DIRECTORY, NER_TXT_TEST));
		
		prepareDataForTrainingFromBioc(
				spark,
				TRAIN_NER_BIOC_TRAIN, 
				FilenameUtils.concat(TRAIN_NER_DIRECTORY, NER_BIOC_TRAIN));
		
		prepareDataForTrainingFromBioc(
				spark,
				TRAIN_NER_BIOC_XML_TEST, 
				FilenameUtils.concat(TRAIN_NER_DIRECTORY, NER_BIOC_TEST));

	}

	public void trainModel(SparkSession spark, String trainfile, String testfile, String outdir) {
		
		Path outdirname = Paths.get(outdir);
		Path filename = Paths.get(trainfile);
		try {
			boolean result = TrainRepository.trainFromConll(
					spark, 
					trainfile, 
					testfile, 
					outdirname.toFile().getName() + "_" + filename.toFile().getName() + ".csv", 
					outdirname.toFile().getName() + "_" + filename.toFile().getName() + ".pipeline9", 
					POS_MODEL,
					BERT_UNCASED_MODEL, 
					TFM_NER_MODEL);
			if (result) {
				LOG.info("TRAIN SERVICE: OK");
			} else {
				LOG.info("TRAIN SERVICE: FAIL");
			}
		} catch (Exception ex) {
			LOG.warn("TRAIN SERVICE: FAIL - ex:" + ex.toString());
		}
		
	}

	public void prepareDataForTrainingFromBioc(SparkSession spark, String infile, String outfile) {
		try {
			Path filename = Paths.get(infile);
			boolean result = TrainRepository.getConllFrom(
					spark, 
					new BiocXmlProcessor(filename), 
					FilenameUtils.concat(TRAIN_NER_DIRECTORY, filename.toFile().getName()), 
					POS_MODEL,
					BERT_UNCASED_MODEL, 
					BERT_NER_MODEL,
					outfile);
			if (result) {
				LOG.info("PREPARE DATA SERVICE: bioc OK");
			} else {
				LOG.info("PREPARE DATA SERVICE: bioc FAIL");
			}
		} catch (Exception ex) {
			LOG.warn("PREPARE DATA SERVICE: bioc FAIL - ex:" + ex.toString());
		}
	}

	public void prepareDataForTrainingFromPubtator(SparkSession spark, String infile, String outfile) {
		
		try {
			Path filename = Paths.get(infile);
			boolean result = TrainRepository.getConllFrom(
					spark, 
					new PubtatorTxtProcessor(filename), 
					FilenameUtils.concat(TRAIN_NER_DIRECTORY, filename.toFile().getName()), 
					POS_MODEL,
					BERT_UNCASED_MODEL, 
					BERT_NER_MODEL,
					outfile);
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
