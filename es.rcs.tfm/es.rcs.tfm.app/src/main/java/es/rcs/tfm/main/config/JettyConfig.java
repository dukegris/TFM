package es.rcs.tfm.main.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.embedded.jetty.JettyWebServer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import es.rcs.tfm.main.AppNames;

@Configuration(  AppNames.JETTY_CONFIG )
@PropertySource( {"classpath:/META-INF/jetty.properties"} )
public class JettyConfig {

	private @Value("${tao.web.keystore.location}") String KEYSTORE_LOCATION = "META-INF/keystore.jks";
	private @Value("${tao.web.keystore.password}") String KEYSTORE_PASSWORD = "changeit";
	private @Value("${tao.web.truststore.location}") String TRUSTSTORE_LOCATION = "META-INF/truststore.jks";
	private @Value("${tao.web.truststore.password}") String TRUSTSTORE_PASSWORD = "changeit";
	private @Value("${tao.web.alias.id}") String KEY_ID = "tomcat";
	private @Value("${tao.web.alias.password}") String KEY_PASSWORD = "tomcat";
	
	private @Value("${server.port}") String HTTP_PORT_DEF = "80";
	private @Value("${server.port}") String HTTPS_PORT_DEF = "443";

	private @Value("${tao.web.http}") String HTTP_PORT = "80";
	private @Value("${tao.web.https}") String HTTPS_PORT = "443";
	private @Value("${tao.web.webapp}") String WEBAPP_DIR = "webapp";
	private @Value("${tao.web.session.cookie}") String SESSION_COOKIE_ID = "TAO-CLIENT-SESSION-COOKIE";

	private static final Logger LOG = LoggerFactory.getLogger(JettyConfig.class);

	@Bean( name = AppNames.JETTY_SERVLET_FACTORY )
	public ServletWebServerFactory servletFactory() {
		
		JettyServletWebServerFactory bean =  new JettyServletWebServerFactory() {

			@Override
			protected JettyWebServer getJettyWebServer(Server server) {

				return super.getJettyWebServer(server);

			}
						
		};
		
		bean.addServerCustomizers(new JettyServerCustomizer() {

			@Override
			public void customize(Server server) {

				try {
					File keystore = new ClassPathResource(KEYSTORE_LOCATION).getFile();
					File truststore = new ClassPathResource(TRUSTSTORE_LOCATION).getFile();
					
					SslContextFactory sslContextFactory = new SslContextFactory();
					sslContextFactory.setKeyStorePassword(KEYSTORE_PASSWORD);
					sslContextFactory.setKeyStorePath(keystore.getAbsolutePath());
					sslContextFactory.setTrustStorePassword(TRUSTSTORE_PASSWORD);
					sslContextFactory.setTrustStorePath(truststore.getAbsolutePath());

					ServerConnector connector = new ServerConnector(server);
					
					int portInt = 443;
					try {
						portInt = Integer.parseInt(HTTPS_PORT);
					} catch (Exception ex) {
						try {
							portInt = Integer.parseInt(HTTPS_PORT_DEF);
						} catch (Exception e2) {
						}
					}
					connector.setPort(portInt);

					server.addConnector(connector);
					
				} catch (FileNotFoundException ex) {
					LOG.warn("Could not load keystore" + ex.toString());
					throw new IllegalStateException("Could not load keystore", ex);
				} catch (IOException ex) {
					LOG.warn("Could not load keystore" + ex.toString());
					throw new IllegalStateException("Could not load keystore", ex);
				}
				
			}
			
		});

		int portInt = 80;
		try {
			portInt = Integer.parseInt(HTTP_PORT);
		} catch (Exception ex) {
			try {
				portInt = Integer.parseInt(HTTP_PORT_DEF);
			} catch (Exception e2) {
			}
		}
		bean.setPort(portInt);

		File folder = new File(WEBAPP_DIR);
		try {
	        LOG.info("TAO Tomcat WEBAPP DIR: " + folder.getCanonicalPath());
			System.out.println(folder.getCanonicalPath());
		} catch (IOException ex) {
	        LOG.warn(
	        		"servletContainer " + 
	        		"can't access webapp: [" + 
	        		WEBAPP_DIR + 
	        		"] " + 
	        		ex.toString());
		}

		bean.setDocumentRoot(folder);
		
		bean.setDisplayName("TAO Tomcat");

		return bean;
		
	}

	@Bean(	name = AppNames.JETTY_LOCALE )
	public SessionLocaleResolver getSessionLocaleResolver() {
		
		SessionLocaleResolver bean =
			new SessionLocaleResolver();
		
		bean.setDefaultLocale(new Locale("es_ES"));
		
		return bean;
		
	}

	@Bean(	name = AppNames.JETTY_SERVLET )
    public DispatcherServlet dispatcherServlet() {
    	
        System.out.println("TAO SERVLET CREATION");
        LOG.info("TAO SERVLET CREATION");
        
        DispatcherServlet bean;
        bean = new DispatcherServlet();
		
        System.out.println("TAO SERVLET CREATED");
        LOG.info("TAO SERVLET CREATED");
		
        System.out.println("TAO SERVLET REGISTRATION");
        LOG.info("TAO SERVLET REGISTRATION");

        return bean;

    }
	
	@Bean(	name = AppNames.JETTY_SERVLET_REG )
	public ServletRegistrationBean<DispatcherServlet> dispatcherServletRegistration() {

		//ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet());
		ServletRegistrationBean<DispatcherServlet> bean;
		
		bean = new ServletRegistrationBean<DispatcherServlet>();
		bean.setLoadOnStartup(1);
		bean.setServlet(dispatcherServlet());
		bean.setName(AppNames.JETTY_SERVLET);// Este nombre engancha con el de la configuracion web
		bean.addUrlMappings("/");

		System.out.println("TAO SERVLET REGISTERED NAME is: " + bean.getServletName().toString()); 
		LOG.info("TAO SERVLET REGISTERED NAME is: " + bean.getServletName().toString());
	
		return bean;

	}

	@Bean(	name = AppNames.JETTY_SESSION_LSNR )
	public HttpSessionListener sessionListener() {
		
		return new HttpSessionListener() {
			 
		    @Override
		    public void sessionCreated(HttpSessionEvent event) {
		        System.out.println("TAO SESSION CREATED");
		        event.getSession().setMaxInactiveInterval(5*60);
		    }
		 
		    @Override
		    public void sessionDestroyed(HttpSessionEvent event) {
		        System.out.println("TAO SESSION DESTROYED");
		    }
			
		};
		
	}
	
	@Bean(	name = AppNames.JETTY_REQUEST_LSNR )
	public RequestContextListener requestContextListener() {
		
		return new RequestContextListener();
		
	}
	
	@Bean(	name = AppNames.JETTY_SERVLET_LSNR )
	protected ServletContextListener servletContextListener() {
		
		ServletContextListener bean = new ServletContextListener() {
			
			@Override
			public void contextInitialized(ServletContextEvent sce) {

				System.out.println("TAO SERVLET CONTEXT initialized");
				LOG.info("TAO SERVLET CONTEXT initialized");

			}

			@Override
			public void contextDestroyed(ServletContextEvent sce) {

				System.out.println("TAO SERVLET CONTEXT destroyed");
				LOG.info("TAO SERVLET CONTEXT destroyed");

			}
			
		};
		
		return bean;
		
	}

}
