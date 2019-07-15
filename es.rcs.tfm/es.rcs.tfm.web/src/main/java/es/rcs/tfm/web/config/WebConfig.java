package es.rcs.tfm.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

import es.rcs.tfm.web.WebNames;

@Configuration( WebNames.WEB_CONFIG )
@ComponentScan( basePackages = {
		WebNames.WEB_SETUP_PKG} )
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

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
	}


}
