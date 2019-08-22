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
import es.rcs.tfm.srv.components.PrepareNerConllFromTxtTask;
import es.rcs.tfm.srv.components.TrainNerFromConllTask;
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
	public void prepareNerConllFromTxt() {
		synchronized(prepareNerConllFromTxtTask) {
			if (prepareNerConllFromTxtTask != null) {
				if (State.NEW.equals(prepareNerConllFromTxtTask.getState())) {
					prepareNerConllFromTxtTask.start();
				} else if (State.TERMINATED.equals(prepareNerConllFromTxtTask.getState())) {
					prepareNerConllFromTxtTask.run();
				} else if (State.RUNNABLE.equals(prepareNerConllFromTxtTask.getState())) {
					LOG.info("PREPARE DATA TASK IS RUNNING");
				} else {
					LOG.warn("PREPARE DATA TASK IS " + prepareNerConllFromTxtTask.getState());
				}
			}
		}
	}
	
	//@Scheduled(
			//cron = "0 */1 * * * SUN-SAT") // Cada minuto
			//cron = "0 0 0 * * MON-SUN")
	public void trainNerFromConll() {
		synchronized(trainNerFromConllTask) {
			if (trainNerFromConllTask != null) {
				if (State.NEW.equals(trainNerFromConllTask.getState())) {
					trainNerFromConllTask.start();
				} else if (State.TERMINATED.equals(trainNerFromConllTask.getState())) {
					trainNerFromConllTask.run();
				} else if (State.RUNNABLE.equals(trainNerFromConllTask.getState())) {
					LOG.info("TRAIN DATA TASK IS RUNNING");
				} else {
					LOG.warn("TRAIN DATA TASK IS " + trainNerFromConllTask.getState());
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
	@Qualifier(value = SrvNames.PREPARE_CONLL_FROM_TXT_TASK)
	private PrepareNerConllFromTxtTask prepareNerConllFromTxtTask;
	
	@Autowired
	@Qualifier(value = SrvNames.TRAIN_NER_MODEL_TASK)
	private TrainNerFromConllTask trainNerFromConllTask;
	
}
