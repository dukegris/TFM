package es.rcs.tfm.srv;

import org.apache.spark.sql.SparkSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import es.rcs.tfm.main.config.SparkConfig;
import es.rcs.tfm.srv.services.train.TrainService;
import es.rcs.tfm.xml.config.XmlConfig;

@RunWith(
		SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		TrainNerTest.class,
		XmlConfig.class,
		SparkConfig.class})
@ComponentScan(
		basePackages = {
				SrvNames.SRV_TRAIN_SERVICES_PKG
		})
public class TrainNerTest {
	
	@Test
	public void prepareMutationModels() throws InterruptedException {
		// train.prepareDataForTraining(spark);
		train.trainModel(spark);
	}

	@BeforeClass
	public static void startUp() throws InterruptedException {
	}
	
	@Before
	public void setUp() throws Exception {
	}
		
    @After
    public void tearDown() {
    }
	
	@AfterClass
	public static void shutDown() {
	}

	@Autowired
	@Qualifier( value = SrvNames.SPARK_SESSION_TRAIN )
	public SparkSession spark;
	
	@Autowired
	@Qualifier(value = SrvNames.TRAINING_SRVC )
	private TrainService train;

}
