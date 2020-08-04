package es.rcs.tfm.main.boot;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.main.config.SparkConfig;
import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.config.ServicesConfig;
import es.rcs.tfm.xml.config.XmlConfig;

@Configuration(
		AppNames.CMD_COMMANDS_CONFIG)
@Import({ 
		SparkConfig.class,
		XmlConfig.class,
		ServicesConfig.class })
@DependsOn({
		SrvNames.SPARK_SESSION_TRAIN,
		SrvNames.TRAINING_SRVC })
public class CommandConfig {

}
