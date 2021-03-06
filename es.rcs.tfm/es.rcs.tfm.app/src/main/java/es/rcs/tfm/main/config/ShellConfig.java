package es.rcs.tfm.main.config;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.web.config.SpringDataWebConfiguration;

import es.rcs.tfm.main.AppNames;

@Configuration(
		AppNames.APP_SHELL_CONFIG )
@ComponentScan({ 
		AppNames.APP_SHELL_PKG })
public class ShellConfig extends SpringDataWebConfiguration {

	public ShellConfig(ApplicationContext context, ObjectFactory<ConversionService> conversionService) {
		
		super(context, conversionService);
	
	}
/*	
    @Bean( name = AppNames.APP_SHELL)
    public ShellHelper getConllcommad(@Lazy Terminal terminal) {
            return new ShellHelper(terminal);
    }
*/
}
