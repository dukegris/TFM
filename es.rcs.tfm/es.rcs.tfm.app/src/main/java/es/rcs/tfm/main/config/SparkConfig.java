package es.rcs.tfm.main.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import es.rcs.tfm.main.AppNames;

@Configuration(
		AppNames.SPARK_CONFIG )
@PropertySource( 
		{"classpath:/META-INF/spark.properties"} )
public class SparkConfig {


	@Value("${hadoop.home}") private String hadoopHome;
	@Value("${spark.home}") private String sparkHome;
	@Value("${spark.master}") private String sparkMaster;
	@Value("${spark.driver.cores}") private String sparkCores;
	@Value("${spark.driver.memory}") private String sparkDriverMemory;
	@Value("${spark.executor.memory}") private String sparkExcutorMemory;
	@Value("${spark.ui.enabled}") private String sparkUiEnabled;
	@Value("${spark.ui.port}") private String sparkUiPort;
	
    @Bean( name = AppNames.SPARK_SESSION )
    public SparkSession getSparkSession() {
    	
		Path hadoop = Paths.get(hadoopHome);
		if (	hadoop.toFile().exists() &&
				hadoop.toFile().isDirectory()) {
			System.setProperty("hadoop.home.dir", hadoop.toFile().getAbsolutePath());
		}
		
		//https://spark.apache.org/docs/latest/configuration.html#application-properties
        SparkConf sparkConf = new SparkConf().
        		setAppName(AppNames.SPARK_APPNAME).
        		setMaster(sparkMaster). // Locahost with 2 threads
        		set("spark.driver.cores", sparkCores).
        		set("spark.driver.memory", sparkDriverMemory).
        		set("spark.executor.memory", sparkExcutorMemory).
        		set("spark.local.dir", sparkHome + "/tmp").
        		set("spark.ui.enabled", sparkUiEnabled).
        		set("spark.ui.port", sparkUiPort).
        		set("spark.jars.packages", "JohnSnowLabs:spark-nlp:2.0.9");
        
        SparkSession spark = SparkSession.
				builder().
				config(sparkConf).
				getOrCreate();
    	
        return spark;
        
    }

	
}
