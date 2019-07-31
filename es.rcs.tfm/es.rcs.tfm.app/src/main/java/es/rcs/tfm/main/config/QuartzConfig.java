package es.rcs.tfm.main.config;

import java.lang.Thread.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.components.PrepareConllTask;
import es.rcs.tfm.srv.components.TrainConllTask;
import es.rcs.tfm.srv.services.OspmcLoaderService;
import es.rcs.tfm.srv.services.PubmedLoaderService;

@EnableScheduling
@DependsOn(value = {
		SrvNames.PUBMED_LOADER_SRVC,
		SrvNames.PMC_LOADER_SRVC,
		SrvNames.TRAINING_SRVC})
@Configuration(AppNames.QUARTZ_CONFIG)
public class QuartzConfig {

	private static final Logger LOG = LoggerFactory.getLogger(QuartzConfig.class);

	//@Scheduled(
	//		cron = "0 */1 * * * SUN-SAT") // Cada minuto
			//cron = "0 0 0 * * MON-SUN")
	public void loadPubmedNewData() {
		pubmedLoader.doLoadNewData();
	}

	//@Scheduled(
	//		cron = "0 */1 * * * SUN-SAT") // Cada minuto
			//cron = "0 0 0 * * MON-SUN")
	public void loadPubmedBaseline() {
		pubmedLoader.doLoadBaselineData();
	}

	//@Scheduled(
	//		cron = "0 */1 * * * SUN-SAT") // Cada minuto
			//cron = "0 0 0 * * MON-SUN")
	public void loadPMC() {
		pmcLoader.doLoadNewData();
	}
	
	@Scheduled(
			cron = "0 */1 * * * SUN-SAT") // Cada minuto
			//cron = "0 0 0 * * MON-SUN")
	public void prepareBronco() {
		synchronized(prepareDataTask) {
			if (prepareDataTask != null) {
				if (State.NEW.equals(prepareDataTask.getState())) {
					prepareDataTask.start();
				} else if (State.TERMINATED.equals(prepareDataTask.getState())) {
					prepareDataTask.run();
				} else if (State.RUNNABLE.equals(prepareDataTask.getState())) {
					LOG.info("PREPARE DATA TASK IS RUNNING");
				} else {
					LOG.warn("PREPARE DATA TASK IS " + prepareDataTask.getState());
				}
			}
		}
	}
	
	@Scheduled(
			cron = "0 */1 * * * SUN-SAT") // Cada minuto
			//cron = "0 0 0 * * MON-SUN")
	public void trainBronco() {
		synchronized(trainModelTask) {
			if (trainModelTask != null) {
				if (State.NEW.equals(trainModelTask.getState())) {
					trainModelTask.start();
				} else if (State.TERMINATED.equals(trainModelTask.getState())) {
					trainModelTask.run();
				} else if (State.RUNNABLE.equals(trainModelTask.getState())) {
					LOG.info("TRAIN DATA TASK IS RUNNING");
				} else {
					LOG.warn("TRAIN DATA TASK IS " + trainModelTask.getState());
				}
			}
		}
	}
	 
	@Autowired
	@Qualifier(value = SrvNames.PUBMED_LOADER_SRVC)
	private PubmedLoaderService pubmedLoader;
	
	@Autowired
	@Qualifier(value = SrvNames.PMC_LOADER_SRVC)
	private OspmcLoaderService pmcLoader;
	
	@Autowired
	@Qualifier(value = SrvNames.PREPARE_DATA_TASK)
	private PrepareConllTask prepareDataTask;
	
	@Autowired
	@Qualifier(value = SrvNames.TRAIN_MODEL_TASK)
	private TrainConllTask trainModelTask;
	
}
