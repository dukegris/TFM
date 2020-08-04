package es.rcs.tfm.main.boot;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.main.config.AccountSecurityConfig;
import es.rcs.tfm.main.config.AuditConfig;
import es.rcs.tfm.main.config.CacheConfig;
import es.rcs.tfm.main.config.CrnkConfig;
import es.rcs.tfm.main.config.DatabaseConfig;
import es.rcs.tfm.main.config.JettyConfig;
import es.rcs.tfm.main.config.SolrConfig;
import es.rcs.tfm.main.config.UtilConfig;
import es.rcs.tfm.main.config.WebConfig;

@Configuration(
		AppNames.CMD_WEBSERVER_CONFIG)
@Import({
		UtilConfig.class, 
		AuditConfig.class,
		CacheConfig.class,
		DatabaseConfig.class,
		SolrConfig.class,
		AccountSecurityConfig.class,
		JettyConfig.class,
		WebConfig.class,
		CrnkConfig.class })
public class WebserverConfig {

}
