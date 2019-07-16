package es.rcs.tfm.nlp;

import java.util.List;

import org.apache.spark.sql.SparkSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.main.config.SparkConfig;
import es.rcs.tfm.nlp.repository.TestSpark;
import scala.Tuple2;

@RunWith(
		SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		SparkConfig.class,
		NlpTest.class })
public class NlpTest {
	
	@Test
	public void testQuijote() {
	
		TestSpark test = new TestSpark(spark);
		List<Tuple2<String, Integer>> output = test.countWords("./el_quijote.txt");
		for (Tuple2<?, ?> tuple : output) {
			System.out.println("OUTPUT: " + tuple._1() + ": " + tuple._2());
		}

	}
		
	@Autowired
	@Qualifier( value = AppNames.SPARK_SESSION)
	private SparkSession spark;
	
}
