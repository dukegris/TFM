package es.rcs.tfm.main.config;

import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;

import org.h2.server.web.WebServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.main.AppNames;

@Configuration(
		value = AppNames.BBDD_CONFIG )
@PropertySource( 
		{"classpath:/META-INF/db.properties"} )
public class DatabaseConfig {

	@Value("${tfm.datasource.url}") private String dbUrl;
	@Value("${tfm.datasource.driverClassName}") private String dbDriver;
	@Value("${tfm.datasource.username}") private String dbUsernamer;
	@Value("${tfm.datasource.password}") private String dbPassword;
	@Value("${tfm.jpa.properties.hibernate.dialect}") private String dbDialect;
	
	@Bean( name = AppNames.BBDD_JPA_DIALECT )
	public JpaDialect getJpaDialect() {
		HibernateJpaDialect bean = 
				new HibernateJpaDialect();
		
		return bean;
	}

	@Bean( name = AppNames.BBDD_JPA_VENDOR )
	public HibernateJpaVendorAdapter getJpaVendorAdapter() {
		
		HibernateJpaVendorAdapter bean = 
				new HibernateJpaVendorAdapter();
		
		bean.setShowSql(true);
		bean.setGenerateDdl(true);
		bean.setDatabasePlatform(dbDialect);
		
		return bean;
		
	}

	@Bean( name = AppNames.BBDD_CONSOLE )
	public ServletRegistrationBean<HttpServlet> h2servletRegistration() {

		WebServlet servlet = new WebServlet();
		//jdbc:h2:mem:SECURITY.DB
		
		ServletRegistrationBean<HttpServlet> bean = 
				new ServletRegistrationBean<HttpServlet>(servlet);
		
		bean.addUrlMappings(AppNames.BBDD_URL);
		
		return bean;
		
	}

	@Bean( name = AppNames.BBDD_DATASOURCE )
	public DataSource getDataSource() {
		
        
		/*
		EmbeddedDatabaseBuilder builder = 
				new EmbeddedDatabaseBuilder();
		EmbeddedDatabase bean = builder
			.setType(EmbeddedDatabaseType.H2) //.H2 or .DERBY
			.setName(AppNames.BBDD_NAME)
			.build();
		 */
		
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl( dbUrl );
		config.setDriverClassName( dbDriver );
        config.setUsername( dbUsernamer );
        config.setPassword( dbPassword );
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        HikariDataSource bean = new HikariDataSource( config );
        
        return bean; 

        /*
		DataSourceBuilder<?> bean = DataSourceBuilder.create();
        bean.driverClassName(dbDriver);
        bean.url(dbUrl);
        bean.username(dbUsernamer);
        bean.password(dbPassword);
        return bean.build();
         */
		
	}
	
	@Bean( name = DbNames.DB_EMF )
	public LocalContainerEntityManagerFactoryBean getEntityManager() {

		Properties jpaProperties = new Properties();
		
		jpaProperties.put("hibernate.show_sql", "true");
		//jpaProperties.put("hibernate.hbm2ddl.auto", "create-drop");
		//jpaProperties.put("hibernate.hbm2ddl.auto", "create");
		//jpaProperties.put("hibernate.hbm2ddl.auto", "validate");
		//jpaProperties.put("hibernate.hbm2ddl.auto", "update");
		jpaProperties.put("hibernate.hbm2ddl.auto", "update");
		
		jpaProperties.put("hibernate.dialect", dbDialect);

		LocalContainerEntityManagerFactoryBean bean = 
				new LocalContainerEntityManagerFactoryBean();

		bean.setJpaProperties(jpaProperties);

		bean.setPersistenceUnitName(AppNames.BBDD_PU);
		bean.setPersistenceXmlLocation("classpath:/META-INF/persistence.xml");
		
		bean.setDataSource(getDataSource());
		bean.setJpaVendorAdapter(getJpaVendorAdapter());
		bean.setJpaDialect(getJpaDialect());
		
		bean.setPackagesToScan(new String[] {
				DbNames.DB_MODEL_PKG});

		return bean;

	}
	
	@Bean(	name = DbNames.DB_TX )
	public PlatformTransactionManager getTransactionManager() {
		
		JpaTransactionManager bean = 
				new JpaTransactionManager();
		
		bean.setEntityManagerFactory(getEntityManager().getObject());
		
		return bean;
	}

}
