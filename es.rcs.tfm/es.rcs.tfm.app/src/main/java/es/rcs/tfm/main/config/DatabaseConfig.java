package es.rcs.tfm.main.config;

import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.commons.lang.StringUtils;
import org.h2.server.web.WebServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.main.AppNames;

@Configuration(
		AppNames.BBDD_CONFIG)
@ComponentScan(basePackages = {
		DbNames.DB_CONFIG_PKG })
@PropertySource({ 
		"classpath:/META-INF/db.properties" })
public class DatabaseConfig {

	@Value("${tfm.datasource.url}")
	private String dbUrl;
	@Value("${tfm.datasource.driverClassName}")
	private String dbDriver;
	@Value("${tfm.datasource.username}")
	private String dbUsername;
	@Value("${tfm.datasource.password}")
	private String dbPassword;
	@Value("${tfm.datasource.poolsize}")
	private Integer dbPoolSize;
	@Value("${tfm.jpa.properties.hibernate.dialect}")
	private String dbDialect;
	@Value("${tfm.jta.datasource}")
	private String jtaDatasource;
	@Value("${tfm.jta.logger}")
	private String jtaLogger;

	@Bean(name = AppNames.BBDD_JPA_DIALECT)
	public JpaDialect getJpaDialect() {
		
		HibernateJpaDialect bean = new HibernateJpaDialect();
		return bean;
		
	}

	@Bean(name = AppNames.BBDD_JPA_VENDOR)
	public HibernateJpaVendorAdapter getJpaVendorAdapter() {

		String dialect = System.getProperty("JDBC_DIALECT") ;
		if (StringUtils.isNotBlank(dialect)) dbDialect = dialect;
		String driver = System.getProperty("JDBC_DRIVER") ;
		if (StringUtils.isNotBlank(driver)) dbDriver = driver;
		String datasource = System.getProperty("JDBC_DATASOURCE") ;
		if (StringUtils.isNotBlank(datasource)) jtaDatasource = datasource;
		String url = System.getProperty("JDBC_URL") ;
		if (StringUtils.isNotBlank(url)) dbUrl = url;
		String username = System.getProperty("JDBC_USERNAME") ;
		if (StringUtils.isNotBlank(username)) dbUsername = username;
		String password = System.getProperty("JDBC_PASSWORD") ;
		if (StringUtils.isNotBlank(password)) dbPassword = password;

		HibernateJpaVendorAdapter bean = new HibernateJpaVendorAdapter();
		bean.setShowSql(true);
		bean.setGenerateDdl(true);
		bean.setDatabasePlatform(dbDialect);
		return bean;

	}

	@Bean(name = AppNames.BBDD_CONSOLE)
	public ServletRegistrationBean<HttpServlet> h2servletRegistration() {
		
		WebServlet servlet = new WebServlet();
		ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<HttpServlet>(servlet);
		bean.addUrlMappings(AppNames.BBDD_URL);
		return bean;
		
	}

	@Bean(name = AppNames.BBDD_DATASOURCE)
	public DataSource getDataSource() {

		String dialect = System.getProperty("JDBC_DIALECT") ;
		if (StringUtils.isNotBlank(dialect)) dbDialect = dialect;
		String driver = System.getProperty("JDBC_DRIVER") ;
		if (StringUtils.isNotBlank(driver)) dbDriver = driver;
		String datasource = System.getProperty("JDBC_DATASOURCE") ;
		if (StringUtils.isNotBlank(datasource)) jtaDatasource = datasource;
		String url = System.getProperty("JDBC_URL") ;
		if (StringUtils.isNotBlank(url)) dbUrl = url;
		String username = System.getProperty("JDBC_USERNAME") ;
		if (StringUtils.isNotBlank(username)) dbUsername = username;
		String password = System.getProperty("JDBC_PASSWORD") ;
		if (StringUtils.isNotBlank(password)) dbPassword = password;
		
		Properties xaProperties = new Properties();
		xaProperties.setProperty("user", dbUsername);
		xaProperties.setProperty("password", dbPassword);
		xaProperties.setProperty("URL", dbUrl);

		AtomikosDataSourceBean bean = new AtomikosDataSourceBean();
		bean.setUniqueResourceName("TFM_DB"); 
		//bean.setXaDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
		bean.setXaDataSourceClassName(jtaDatasource);
		bean.setXaProperties(xaProperties);
		bean.setLocalTransactionMode(true);
		bean.setPoolSize(dbPoolSize);
		//bean.setLogWriter(out);
		return bean;

	}

	@Bean(name = DbNames.DB_EMF)
	public LocalContainerEntityManagerFactoryBean getEntityManager() {

		String dialect = System.getProperty("JDBC_DIALECT") ;
		if (StringUtils.isNotBlank(dialect)) dbDialect = dialect;
		String driver = System.getProperty("JDBC_DRIVER") ;
		if (StringUtils.isNotBlank(driver)) dbDriver = driver;
		String datasource = System.getProperty("JDBC_DATASOURCE") ;
		if (StringUtils.isNotBlank(datasource)) jtaDatasource = datasource;
		String url = System.getProperty("JDBC_URL") ;
		if (StringUtils.isNotBlank(url)) dbUrl = url;
		String username = System.getProperty("JDBC_USERNAME") ;
		if (StringUtils.isNotBlank(username)) dbUsername = username;
		String password = System.getProperty("JDBC_PASSWORD") ;
		if (StringUtils.isNotBlank(password)) dbPassword = password;

		Properties jpaProperties = new Properties();
		jpaProperties.put("hibernate.show_sql", "true");
		// jpaProperties.put("hibernate.format_sql", "false");
		// jpaProperties.put("hibernate.hbm2ddl.auto", "create-drop");
		// jpaProperties.put("hibernate.hbm2ddl.auto", "create");
		// jpaProperties.put("hibernate.hbm2ddl.auto", "validate");
		// jpaProperties.put("hibernate.hbm2ddl.auto", "update");
		jpaProperties.put("hibernate.hbm2ddl.auto", "update");
		jpaProperties.put("hibernate.dialect", dbDialect);
		// JTA
		jpaProperties.put("javax.persistence.transactionType", "jta");
		jpaProperties.put("hibernate.current_session_context_class", "jta");
		//jpaProperties.put("hibernate.transaction.manager_lookup_class", "com.atomikos.icatch.jta.hibernate3.TransactionManagerLookup");
		jpaProperties.put("hibernate.transaction.jta.platform", "com.atomikos.icatch.jta.hibernate4.AtomikosPlatform");
		///jpaProperties.put("hibernate.transaction.factory_class", "org.hibernate.transaction.CMTTransactionFactory");
		//jpaProperties.put("hibernate.enable_lazy_load_no_trans", "false");
		jpaProperties.put("hibernate.connection.handling_mode", "DELAYED_ACQUISITION_AND_RELEASE_AFTER_STATEMENT");
		jpaProperties.put("hibernate.connection.autocommit", "false");
		jpaProperties.put("hibernate.connection.release_mode", "after_transaction");
		
		LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
		bean.setDataSource(getDataSource());
		bean.setJpaVendorAdapter(getJpaVendorAdapter());
		bean.setJpaDialect(getJpaDialect());
		bean.setJpaProperties(jpaProperties);
		bean.setPersistenceUnitName(AppNames.BBDD_PU);
		//bean.setPersistenceXmlLocation("classpath:/META-INF/persistence.xml");
		bean.setPackagesToScan(new String[] { DbNames.DB_MODEL_PKG });
		return bean;

	}

	@Bean(
			name = AppNames.BBDD_TR_SERVICE,
			initMethod = "init",
			destroyMethod = "shutdownForce")
	public UserTransactionService getUserTransactionService() {

		Properties config = new Properties();
		config.setProperty("com.atomikos.icatch.force_shutdown_on_vm_exit",	"true");
		config.setProperty("com.atomikos.icatch.log_base_name",				"UserTransactionServiceImpLog");
		config.setProperty("com.atomikos.icatch.log_base_dir",				jtaLogger);
		config.setProperty("com.atomikos.icatch.enable_logging",			"false");

		UserTransactionService bean = new UserTransactionServiceImp(config);
		return bean;

	}

	@Bean(
			name = AppNames.BBDD_TR_MANAGER,
			initMethod = "init",
			destroyMethod = "close")
	public TransactionManager getUserTransactionManager() {

		TransactionManager bean = new UserTransactionManager();
		try {
			bean.setTransactionTimeout(60);
		} catch (SystemException e) {
			e.printStackTrace();
		}
		return bean;

	}

	@Bean(
			name = AppNames.BBDD_TR_USER_MANAGER)
	public UserTransaction getUserTransaction() {

		UserTransaction bean = new UserTransactionImp();
		return bean;

	}


	@Bean(name = DbNames.DB_TX)
	public PlatformTransactionManager getTransactionManager() {

		JtaTransactionManager bean = new JtaTransactionManager();
		//UserTransaction userTransaction = new UserTransactionImp();
		UserTransaction userTransaction = getUserTransaction();
		try {
			userTransaction.setTransactionTimeout(10);
			bean.setTransactionManager(getUserTransactionManager());
			bean.setUserTransaction(getUserTransaction());
			bean.setAllowCustomIsolationLevels(true);
		} catch (SystemException e) {
			e.printStackTrace();
		}
		return bean;
		
	}

}
