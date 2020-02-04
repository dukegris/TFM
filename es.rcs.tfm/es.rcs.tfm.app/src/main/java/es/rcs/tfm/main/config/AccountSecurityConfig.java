package es.rcs.tfm.main.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import es.rcs.tfm.api.controller.AccessController;
import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.main.components.RestExceptionAuthenticationEntryPoint;
import es.rcs.tfm.main.setup.JwtAuthenticationFilter;
import es.rcs.tfm.main.setup.JwtAuthorizationFilter;
import es.rcs.tfm.main.setup.TaoAuthenticationProvider;

@Order(value = 110)
@EnableWebSecurity
@Configuration( AppNames.SEC_CONFIG )
@PropertySource( {"classpath:/META-INF/security.properties"} ) 
public class AccountSecurityConfig extends WebSecurityConfigurerAdapter {

	private @Value("${tao.security.rememberme.cookie}") String REMEMBERME_COOKIE_ID = "TAO-CLIENT-REMEMBER-COOKIE";
	private @Value("${tao.security.session.cookie}") String SESSION_COOKIE_ID = "TAO-CLIENT_SESSION_COOKIE";
	private @Value("${tao.security.xsrf.cookie}") String XSRF_COOKIE_ID = "TAO-CLIENT-X-XSRF-COOKIE";
	private @Value("${tao.security.xsrf.header}") String XSRF_HEADER_ID = "TAO-CLIENT-X-XSRF-HEADER";

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
			// Esta Configuracion de seguridad solo se aplica al API de seguridad
			.requestMatcher(new AntPathRequestMatcher(AccessController.API_ACCOUNT_BASE + "/**"))
			//.requiresChannel()
			//	.anyRequest().requiresSecure()
			//	.and()
			.authorizeRequests()

				.antMatchers(AccessController.API_LOGIN_ACTION + "/**").permitAll()
				
				.antMatchers(HttpMethod.GET, AccessController.API_ACCOUNTME_ACTION).fullyAuthenticated()
				
				/*
				.antMatchers(HttpMethod.GET, SecNames.ACCOUNT_API_ACTION + "/**").permitAll()
				.antMatchers(HttpMethod.POST, SecNames.ACCOUNT_API_ACTION + "/**").fullyAuthenticated()
				.antMatchers(HttpMethod.PUT, SecNames.ACCOUNT_API_ACTION + "/**").fullyAuthenticated()
				.antMatchers(HttpMethod.DELETE, SecNames.ACCOUNT_API_ACTION + "/**").fullyAuthenticated()
				 */
				//.anyRequest().authenticated()

				.and()
			.exceptionHandling()
				.authenticationEntryPoint(restExceptionAuthenticationEntryPoint)
				.and()
			.cors() // Se podra acceder desde todas las IP
				.configurationSource(corsConfigurationSource())
				.and()
			.csrf()
				.disable()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
			.addFilter(new JwtAuthenticationFilter(authenticationManager()))
			.addFilter(new JwtAuthorizationFilter(authenticationManager()))
			;

	}
	
    @Bean( name = AppNames.SEC_CORS_SETUP )
    public CorsConfigurationSource corsConfigurationSource() {
    	
    	final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addAllowedOrigin("*");
        
        // Permitir el acceso a esta cabecera
        config.addExposedHeader(AppNames.JWT_HEADER_AUTHORIZATION);
        //config.setAllowedMethods(Arrays.asList(new String[]{"*"}));
        //config.setAllowedOrigins(Arrays.asList(new String[]{"*"}));
        //config.setAllowedHeaders(Arrays.asList(new String[]{"*"}));
        //config.setAllowedMethods(Arrays.asList(new String[]{"GET", "POST", "PUT", "PATCH", "DELETE"}));
        //config.setAllowedOrigins(Arrays.asList(new String[]{"http://localhost:4200", "http://localhost:8080", "https://localhost:8443"}));
        //config.setAllowedHeaders(Arrays.asList(new String[]{"Authorization", "Cache-Control", "Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"}));
        config.applyPermitDefaultValues();
    	
        final UrlBasedCorsConfigurationSource bean = new UrlBasedCorsConfigurationSource();
        bean.registerCorsConfiguration(AccessController.API_ACCOUNT_BASE + "/**", config);

        return bean;
        
    }

	@Bean( name = AppNames.SEC_CRYPT_PASSWORD )
	public PasswordEncoder getPasswordEncoder() {
		BCryptPasswordEncoder bean = new BCryptPasswordEncoder();
		return bean;
	};
    
	@Bean( AppNames.SEC_AUTH_PROVIDER )
	public AuthenticationProvider getAuthenticationProvider() {
		
		TaoAuthenticationProvider bean = 
			new TaoAuthenticationProvider(
					userDetailsService,
					getPasswordEncoder());
			
		return bean;
		
	}

	@Autowired
	@Qualifier(	AppNames.SEC_AUTH_EX_EP )
	private RestExceptionAuthenticationEntryPoint restExceptionAuthenticationEntryPoint;

	@Autowired
	@Qualifier(	AppNames.SEC_DETAILS_SERVICE )
	private UserDetailsService userDetailsService;

	/*	
	@Bean( name = SecNames.SEC_JWT_AUTHENTICATION_FILTER )
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager) {
    	JwtAuthenticationFilter bean = null;
		try {
			bean = new JwtAuthenticationFilter(authenticationManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return bean;
    }

    @Bean( name = SecNames.SEC_JWT_AUTHORIZATION_FILTER )
    public JwtAuthorizationFilter jwtAuthorizationFilter(AuthenticationManager authenticationManager) {
    	JwtAuthorizationFilter bean = null;
		try {
			bean = new JwtAuthorizationFilter(authenticationManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return bean;
    			
    }
 */
	
}
