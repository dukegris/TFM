package es.rcs.tfm.srv.train.services;

import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import es.rcs.tfm.srv.SrvNames;

/**
 * Lanza una tarea de preparación de datos
 * 
 * @author raul
 *
 */
@Component(
		value = SrvNames.TRAIN_NER_MODEL_TASK)
@DependsOn(
		value = {
				SrvNames.SPARK_SESSION_TRAIN,
				SrvNames.TRAINING_SRVC})
@Scope("prototype")
public class TrainNerFromConllTask extends Thread{

	private static final Logger LOG = LoggerFactory.getLogger(TrainNerFromConllTask.class);

	public TrainNerFromConllTask() {
		super();
		this.setName(SrvNames.TRAIN_NER_MODEL_TASK);
	}

	@Override
	public void run() {
		LOG.info("TRAIN MODEL START");
		train.trainModel(spark);
		LOG.info("TRAIN MODEL END");
	}

	@Autowired
	@Qualifier( value = SrvNames.SPARK_SESSION_TRAIN )
	public SparkSession spark;
	
	@Autowired
	@Qualifier( value = SrvNames.TRAINING_SRVC )
	private TrainService train;
	
}
