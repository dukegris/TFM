package es.rcs.tfm.main.config;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.rcs.tfm.main.AppNames;

@EnableWebMvc
@Configuration(
		AppNames.WEB_CONFIG )
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {

		// TODO Para recibir todas las conexiones en el servlet por defecto
		//configurer.enable(SpringNames.TOMCAT_SERVLET);
		configurer.enable();

	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		// http://www.baeldung.com/spring-mvc-static-resources

		// TODO Poner en property
		//int cachePeriod = 3600 * 24 * 15;
		int cachePeriod = 5;
		if (!registry.hasMappingForPattern("/**")) {
			registry
				.addResourceHandler("/**")
				.addResourceLocations("classpath:/public/")
				.setCachePeriod(cachePeriod)
				.resourceChain(true)
				.addResolver(new EncodedResourceResolver())
				.addResolver(new PathResourceResolver());
		}

		if (!registry.hasMappingForPattern("/favicon.ico")) {
			registry
				.addResourceHandler("/favicon.ico")
				.addResourceLocations("classpath:/public/favicon.ico")
				.setCachePeriod(cachePeriod)
				.resourceChain(true)
				.addResolver(new EncodedResourceResolver())
				.addResolver(new PathResourceResolver());
		}

	}

    @Bean(name = AppNames.WEB_LOCALE_RESOLVER)
    public LocaleResolver localeResolver() {

    	CookieLocaleResolver bean = new CookieLocaleResolver();

        bean.setCookieName("locale");
        bean.setCookieMaxAge(30);
        bean.setDefaultLocale(new Locale("es", "ES"));

        return bean;

    }

	@Bean ( name = AppNames.WEB_JACKSON_MAPPER )
	public ObjectMapper getObjectMapper() {
		
		ObjectMapper bean = new ObjectMapper();
		
		bean.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		bean.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
		
		return bean;
		
	}

}
