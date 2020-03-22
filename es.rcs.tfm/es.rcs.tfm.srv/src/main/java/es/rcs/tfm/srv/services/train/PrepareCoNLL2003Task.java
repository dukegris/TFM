package es.rcs.tfm.srv.services.train;

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
@Component(value = SrvNames.PREPARE_CONLL_FROM_TXT_TASK)
@DependsOn(value = {
		SrvNames.SPARK_SESSION_TRAIN,
		SrvNames.TRAINING_SRVC})
@Scope("prototype")
public class PrepareCoNLL2003Task extends Thread{

	private static final Logger LOG = LoggerFactory.getLogger(PrepareCoNLL2003Task.class);

	public PrepareCoNLL2003Task() {
		super();
		this.setName(SrvNames.PREPARE_CONLL_FROM_TXT_TASK);
	}

	@Override
	public void run() {
		LOG.info("PREPARE DATA START");
		//TODO train.prepareCoNLL2003DataForTraining(spark);
		LOG.info("PREPARE DATA END");
	}

	@Autowired
	@Qualifier( value = SrvNames.SPARK_SESSION_TRAIN )
	public SparkSession spark;
	
	@Autowired
	@Qualifier(value = SrvNames.TRAINING_SRVC)
	private TrainService train;
	
}
