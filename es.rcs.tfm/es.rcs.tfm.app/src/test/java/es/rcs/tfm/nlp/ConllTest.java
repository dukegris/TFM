package es.rcs.tfm.nlp;

import org.apache.spark.sql.SparkSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import es.rcs.tfm.db.config.DbConfig;
import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.main.config.DatabaseConfig;
import es.rcs.tfm.main.config.SolrConfig;
import es.rcs.tfm.main.config.SparkConfig;
import es.rcs.tfm.solr.config.IndexConfig;
import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.config.ServicesConfig;
import es.rcs.tfm.srv.services.train.TrainService;
import es.rcs.tfm.xml.config.XmlConfig;

@RunWith(
		SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		SparkConfig.class,
		XmlConfig.class,
		DbConfig.class,
		ServicesConfig.class,
		DatabaseConfig.class,
		SolrConfig.class,
		IndexConfig.class,
		ConllTest.class})
public class ConllTest {
	
	@Test
	public void generateConll() {
		try {

			String tmvarType = "BIOC";
			/*
			String inFileName = "tmVar/tmVarCorpus/train.PubTator.txt";
			String outFileName = "train.PubTator.conll";
			String inFileName = "tmVar/tmVarCorpus/test.PubTator.txt";
			String outFileName = "test.PubTator.conll";
			String inFileName = "tmVar/tmVarCorpus/test.BioC.xml";
			String outFileName = "test.BioC.conll";
			*/
			String inFileName = "tmVar/tmVarCorpus/train.BioC.xml";
			String outFileName = "train.BioC.conll";
			String posModelDirectoryName = "J:/Worksapce-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/pos";
			String bertModelDirectoryName = "J:/Worksapce-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert";
			String nerModelDirectoryName = "J:/Worksapce-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner";
			if (AppNames.BIOC.equals(tmvarType.toUpperCase())) {
				train.prepareCoNLL2003DataForTrainingFromBioc(
						spark, 
						inFileName,
						outFileName,
						posModelDirectoryName,
						bertModelDirectoryName,
						nerModelDirectoryName,
						false);
			} else if (AppNames.PUBTATOR.equals(tmvarType.toUpperCase())) {
				train.prepareCoNLL2003DataForTrainingFromPubtator(
						spark,
						inFileName,
						outFileName,
						posModelDirectoryName,
						bertModelDirectoryName,
						nerModelDirectoryName,
						false);
			}
			
		} catch (Exception ex) {
			System.out.println("FAILED " + ex);		
		}
	}

	@Autowired
	@Qualifier( value = SrvNames.SPARK_SESSION_TRAIN )
	public SparkSession spark;
	
	@Autowired
	@Qualifier(value = SrvNames.TRAINING_SRVC )
	private TrainService train;

}
