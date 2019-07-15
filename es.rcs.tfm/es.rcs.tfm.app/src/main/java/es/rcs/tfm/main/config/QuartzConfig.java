package es.rcs.tfm.main.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.services.LoaderService;

@EnableScheduling
@Configuration(AppNames.QUARTZ_CONFIG)
public class QuartzConfig {

	@Scheduled(
			cron = "0 */1 * * * SUN-SAT") // Cada minuto
			//cron = "0 0 0 * * MON-SUN")
	public void reportCurrentTime() {
		loader.doLoadPubmed();
	}
	
	@Autowired
	@Qualifier(value = SrvNames.SRV_LOADER)
	private LoaderService loader;
	
}
