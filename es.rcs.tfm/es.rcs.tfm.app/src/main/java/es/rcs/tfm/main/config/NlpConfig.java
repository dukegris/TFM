package es.rcs.tfm.main.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.srv.config.ServicesConfig;
import es.rcs.tfm.xml.config.XmlConfig;

@Configuration(
		AppNames.NLP_CONFIG )
@Import({ 
		XmlConfig.class,
		ServicesConfig.class })
public class NlpConfig {

}
