package es.rcs.tfm.nlp.util;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import scala.Tuple2;

public class TestSpark {
	
	//private static final String SPACE = "[\\s+\\,\\.\\t]";
	private static final String SPACE = " ";

	private SparkSession spark = null;
	
	public TestSpark(SparkSession spark) {
		super();
		this.spark = spark;
	}

	public List<Tuple2<String, Integer>> countWords(String filename) {

		SparkContext sc = spark.sparkContext();
		    
		JavaSparkContext jsc = new JavaSparkContext(sc);
		
		JavaRDD<String> lines = jsc.textFile(filename, 1);
		 
		JavaRDD<String> words = lines.flatMap(s -> Arrays.asList(SPACE.split(s)).iterator());
		    
		JavaPairRDD<String, Integer> ones = words.mapToPair(word -> new Tuple2<>(word, 1));
		    
		JavaPairRDD<String, Integer> counts = ones.reduceByKey((Integer i1, Integer i2) -> i1 + i2);
		 
		List<Tuple2<String, Integer>> output = counts.collect();

        jsc.close();
        jsc.stop();

		return output;
		
	}

}
