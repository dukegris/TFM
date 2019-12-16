package es.rcs.tfm.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.rcs.tfm.web.WebNames;
import io.crnk.security.ResourcePermission;
import io.crnk.security.SecurityConfig;
import io.crnk.spring.setup.boot.core.CrnkCoreAutoConfiguration;
import io.crnk.spring.setup.boot.core.CrnkTomcatAutoConfiguration;
import io.crnk.spring.setup.boot.home.CrnkHomeAutoConfiguration;
import io.crnk.spring.setup.boot.meta.CrnkMetaAutoConfiguration;
import io.crnk.spring.setup.boot.mvc.CrnkSpringMvcAutoConfiguration;
import io.crnk.spring.setup.boot.operations.CrnkOperationsAutoConfiguration;
import io.crnk.spring.setup.boot.security.CrnkSecurityAutoConfiguration;
import io.crnk.spring.setup.boot.security.SecurityModuleConfigurer;
import io.crnk.spring.setup.boot.ui.CrnkUIAutoConfiguration;
import io.crnk.spring.setup.boot.validation.CrnkValidationAutoConfiguration;

@Configuration( WebNames.CRNK_CONFIG )
@Import( {
		CrnkHomeAutoConfiguration.class,
		CrnkCoreAutoConfiguration.class,
		CrnkValidationAutoConfiguration.class,
		//CrnkJpaAutoConfiguration.class,
		CrnkMetaAutoConfiguration.class,
		CrnkOperationsAutoConfiguration.class,
		CrnkUIAutoConfiguration.class,
		CrnkSecurityAutoConfiguration.class,
		CrnkSpringMvcAutoConfiguration.class,
		//CrnkErrorControllerAutoConfiguration.class,
		CrnkTomcatAutoConfiguration.class } )
@EnableGlobalMethodSecurity(
		prePostEnabled = true, 
		securedEnabled = true, 
		jsr250Enabled = true)
public class CrnkConfig extends WebSecurityConfigurerAdapter {

	public static final String CRNK_API_ACTION = "/api/crnk";
	public static final RequestMatcher CRNK_API_REQUEST = new AntPathRequestMatcher(CRNK_API_ACTION + "/**");

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
			// Esta Configuracion de seguridad solo se aplica al API
			.requestMatcher(CRNK_API_REQUEST)
			.authorizeRequests()
				.antMatchers(CRNK_API_ACTION + "/**").permitAll()
				.antMatchers(CRNK_API_ACTION + "/**/**").permitAll()
				.antMatchers(HttpMethod.GET, CRNK_API_ACTION + "/**").permitAll()
				.and()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
			.cors() // Se podra acceder desde todas las IP
				.configurationSource(corsConfigurationSource())
				.and()
			.csrf()
				.disable()
//			.addFilter(new JwtAuthenticationFilter(authenticationManager()))
//			.addFilter(new JwtAuthorizationFilter(authenticationManager()))
				;

	}

	@Bean( name = WebNames.CRNK_SEC_CONFIG )
	public SecurityModuleConfigurer securityModuleConfiguration() {
		SecurityModuleConfigurer bean = new SecurityModuleConfigurer() {
			@Override
			public void configure(SecurityConfig.Builder config) {
				config.permitAll(ResourcePermission.ALL);
			} 
		};
		return bean;
	}
	
	@Bean( name = WebNames.CRNK_CORS_FILTER )
	public FilterRegistrationBean<CorsFilter> corsFilter() {
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<CorsFilter>(new CorsFilter(corsConfigurationSource()));
		bean.setOrder(0);
		return bean;
	}

    @Bean( name = WebNames.CRNK_CORS_SETUP )
    public CorsConfigurationSource corsConfigurationSource() {
    	
    	final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addAllowedOrigin("*");
        //config.setAllowedMethods(Arrays.asList(new String[]{"*"}));
        //config.setAllowedOrigins(Arrays.asList(new String[]{"*"}));
        //config.setAllowedHeaders(Arrays.asList(new String[]{"*"}));
        //config.setAllowedMethods(Arrays.asList(new String[]{"GET", "POST", "PUT", "PATCH", "DELETE"}));
        //config.setAllowedOrigins(Arrays.asList(new String[]{"http://localhost:4200", "http://localhost:8080", "https://localhost:8443"}));
        //config.setAllowedHeaders(Arrays.asList(new String[]{"Authorization", "Cache-Control", "Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"}));
        config.applyPermitDefaultValues();
    	
        final UrlBasedCorsConfigurationSource bean = new UrlBasedCorsConfigurationSource();
        bean.registerCorsConfiguration(CRNK_API_ACTION + "/**", config);

        return bean;
        
    }
    
	@Autowired
	@Qualifier(value = WebNames.WEB_JACKSON_MAPPER )
	private ObjectMapper objectMapper;

}
