package es.rcs.tfm.srv.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import es.rcs.tfm.srv.SrvNames;

@Configuration(
		SrvNames.SRV_CONFIG )
@ComponentScan(
		basePackages = SrvNames.SRV_SERVICES_PKG)
@PropertySource(
		{"classpath:/META-INF/service.properties"} )
public class ServicesConfig {
	
}
