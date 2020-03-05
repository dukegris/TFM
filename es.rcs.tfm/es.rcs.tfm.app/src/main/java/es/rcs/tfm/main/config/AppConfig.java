package es.rcs.tfm.main.config;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.web.config.SpringDataWebConfiguration;

import es.rcs.tfm.main.AppNames;

@Configuration(
		value = AppNames.APP_CONFIG )
@ComponentScan(basePackages = { 
		AppNames.APP_SERVICES_PKG,
		AppNames.APP_COMPONENTS_PKG 
		})
public class AppConfig extends SpringDataWebConfiguration {

	public AppConfig(ApplicationContext context, ObjectFactory<ConversionService> conversionService) {
		
		super(context, conversionService);
	
	}
	
	@Bean(	name = AppNames.APP_PROPERTIES_LOADER )
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {

		return new PropertySourcesPlaceholderConfigurer();

	}
/*	
    @Bean( name = AppNames.APP_SHELL)
    public ShellHelper getConllcommad(@Lazy Terminal terminal) {
            return new ShellHelper(terminal);
    }
*/
}
