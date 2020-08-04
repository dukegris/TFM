package es.rcs.tfm.main.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import es.rcs.tfm.main.AppNames;

@Configuration(
		AppNames.UTIL_CONFIG)
public class UtilConfig {
	
	@Bean(	name = AppNames.APP_PROPERTIES_LOADER )
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {

		return new PropertySourcesPlaceholderConfigurer();

	}

}
