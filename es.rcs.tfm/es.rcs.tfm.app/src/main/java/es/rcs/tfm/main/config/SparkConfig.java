package es.rcs.tfm.main.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;

import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.nlp.NlpNames;
import es.rcs.tfm.nlp.setup.SparkSessionFactory;
import es.rcs.tfm.srv.SrvNames;

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

    @Bean( name = NlpNames.SPARK_SESSION_CONFIG )
    public SparkConf getSparkConf() {
    	
		Path hadoop = Paths.get(hadoopHome);
		if (	hadoop.toFile().exists() &&
				hadoop.toFile().isDirectory()) {
			System.setProperty("hadoop.home.dir", hadoop.toFile().getAbsolutePath());
		}
		
		//https://spark.apache.org/docs/latest/configuration.html#application-properties
        SparkConf bean = new SparkConf().
        		setAppName(AppNames.SPARK_APPNAME).
        		setMaster(sparkMaster). // Locahost with 2 threads
        		set("spark.rpc.askTimeout", "240").
        		set("spark.driver.cores", sparkCores).
        		set("spark.driver.memory", sparkDriverMemory).
        		set("spark.executor.memory", sparkExcutorMemory).
        		set("spark.local.dir", sparkHome + "/tmp").
        		set("spark.ui.enabled", sparkUiEnabled).
        		set("spark.ui.port", sparkUiPort).
        		set("spark.jars.packages", "JohnSnowLabs:spark-nlp:2.0.9");

        return bean;
    }

	@Bean( name = NlpNames.SPARK_SESSION_FACTORY )
	@DependsOn( value = NlpNames.SPARK_SESSION_CONFIG )
    public SparkSessionFactory getSparkSessionFactory() {
        
		SparkSessionFactory factory = new SparkSessionFactory(getSparkConf());
        return factory;
        
    }
	
	@Bean( name = SrvNames.SPARK_SESSION_TRAIN )
	@DependsOn( value = NlpNames.SPARK_SESSION_FACTORY )
	public SparkSession getSparkTrainSession() {
	    
	    SparkSession spark = null;
		try {
			spark = getSparkSessionFactory().getObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    return spark;
	    
	}
	
    @Bean( name = AppNames.SPARK_SESSION_TEST )
    @DependsOn( value = NlpNames.SPARK_SESSION_FACTORY )
    public SparkSession getSparkSession() {
        
        SparkSession spark = null;
		try {
			spark = getSparkSessionFactory().getObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return spark;
        
    }

}
