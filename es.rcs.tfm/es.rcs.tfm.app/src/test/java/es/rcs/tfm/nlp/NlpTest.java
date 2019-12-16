package es.rcs.tfm.nlp;

import java.util.List;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.main.config.SparkConfig;
import es.rcs.tfm.nlp.repository.TestSpark;
import es.rcs.tfm.nlp.util.TestSparkNLP;
import scala.Tuple2;

@RunWith(
		SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		SparkConfig.class,
		NlpTest.class })
public class NlpTest {
	
	@Test
	public void testQuijote() {
	
		try {
			TestSpark test = new TestSpark(spark);
			List<Tuple2<String, Integer>> output = test.countWords("./el_quijote.txt");
			for (Tuple2<?, ?> tuple : output) {
				System.out.println("OUTPUT: " + tuple._1() + ": " + tuple._2());
			}
		} catch (Exception ex) {
			System.out.println("EX: " + ex.getLocalizedMessage());
		}

	}

	
	@Test
	public void testScala() {
	
		try {
			TestSparkNLP test = new TestSparkNLP(
					spark.sparkContext(),
					spark,
					"../es.rcs.tfm.corpus/models");
			Dataset<Row> result = test.nlp1();
			if (result.isStreaming()) {
				
				SparkContext sc = spark.sparkContext();
				JavaSparkContext jsc = new JavaSparkContext(sc);
				JavaStreamingContext ssc = new JavaStreamingContext(sc.getConf(), new Duration(1000));
				
				result.foreach(r -> {
					
				});

			}
		} catch (Exception ex) {
			System.out.println("EX: " + ex.getLocalizedMessage());
		}

	}

	@Test
	public void testTrain() {
	
		try {
			/*
			NerPipeline p = new NerPipeline(spark);
			p.test();
			*/
			
		} catch (Exception ex) {
			System.out.println("EX: " + ex.getLocalizedMessage());
		}

	}

	@Autowired
	@Qualifier( value = AppNames.SPARK_SESSION_TEST)
	private SparkSession spark;
	
}
