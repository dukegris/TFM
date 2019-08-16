package es.rcs.tfm.srv.components;

import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.services.TrainService;

/**
 * Lanza una tarea de preparaci�n de datos
 * 
 * @author raul
 *
 */
@Component(value = SrvNames.TRAIN_MODEL_TASK)
@Scope("prototype")
public class TrainConllTask extends Thread{

	private static final Logger LOG = LoggerFactory.getLogger(TrainConllTask.class);

	public TrainConllTask() {
		super();
		this.setName(SrvNames.TRAIN_MODEL_TASK);
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
	@Qualifier(value = SrvNames.TRAINING_SRVC)
	private TrainService train;
	
}