package es.rcs.tfm.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import es.rcs.tfm.web.WebNames;

@Configuration( WebNames.WEB_CONFIG )
@ComponentScan( basePackages = {
		WebNames.WEB_SETUP_PKG,
		WebNames.WEB_CONTROLLER_PKG} )
public class WebCofig {

}
