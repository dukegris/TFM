package es.rcs.tfm.main.boot;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.main.config.AuditConfig;
import es.rcs.tfm.main.config.CacheConfig;
import es.rcs.tfm.main.config.DatabaseConfig;
import es.rcs.tfm.main.config.NlpConfig;
import es.rcs.tfm.main.config.QuartzConfig;
import es.rcs.tfm.main.config.SolrConfig;
import es.rcs.tfm.main.config.SparkConfig;
import es.rcs.tfm.main.config.UtilConfig;

@Configuration(
		AppNames.CMD_DOWNLOADER_CONFIG)
@Import({ 
		UtilConfig.class,
		AuditConfig.class,
		CacheConfig.class,
		DatabaseConfig.class,
		SolrConfig.class,
		SparkConfig.class,
		QuartzConfig.class,
		NlpConfig.class })
public class DownloaderConfig {

}
