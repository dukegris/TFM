package es.rcs.tfm.main.config;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.web.config.SpringDataWebConfiguration;

import es.rcs.tfm.main.AppNames;

@Configuration( AppNames.APP_CONFIG )
public class AppConfig extends SpringDataWebConfiguration {

	public AppConfig(ApplicationContext context, ObjectFactory<ConversionService> conversionService) {
		
		super(context, conversionService);
	
	}
	
	@Bean(	name = AppNames.APP_PROPERTIES_LOADER )
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
       return new PropertySourcesPlaceholderConfigurer();
    }

}
