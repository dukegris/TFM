package es.rcs.tfm.srv.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import es.rcs.tfm.srv.SrvNames;

@Configuration(
		SrvNames.SRV_CONFIG )
@ComponentScan({
		SrvNames.SRV_CORPUS_SERVICES_PKG,
		SrvNames.SRV_CORPUS_REPOSITORIES_PKG,
		SrvNames.SRV_TRAIN_SERVICES_PKG })
@PropertySource(
		{"classpath:/META-INF/service.properties"} )
public class ServicesConfig {

}
