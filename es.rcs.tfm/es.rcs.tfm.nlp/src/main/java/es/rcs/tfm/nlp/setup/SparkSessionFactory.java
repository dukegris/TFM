package es.rcs.tfm.nlp.setup;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.FactoryBean;

public class SparkSessionFactory implements FactoryBean<SparkSession> {
 
	private SparkConf sparkConf = null;
	
	public SparkSessionFactory(SparkConf sparkConf) {
		super();
		this.sparkConf = sparkConf;
	}

	@Override
    public SparkSession getObject() throws Exception {
        return SparkSession.
				builder().
				config(sparkConf).
				getOrCreate();
    }
 
    @Override
    public Class<?> getObjectType() {
        return SparkSession.class;
    }
 
    @Override
    public boolean isSingleton() {
        return false;
    }

	public SparkConf getSparkConf() {
		return sparkConf;
	}

	public void setSparkConf(SparkConf sparkConf) {
		this.sparkConf = sparkConf;
	}
 
    
}
