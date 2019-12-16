package es.rcs.tfm.api.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import es.rcs.tfm.api.ApiNames;

@Configuration( ApiNames.API_CONFIG )
@ComponentScan( basePackages = {
		ApiNames.API_SETUP_PKG,
		ApiNames.API_CONTROLLER_PKG,
		ApiNames.API_REPOSITORY_PKG} )
public class ApiConfig {

}
