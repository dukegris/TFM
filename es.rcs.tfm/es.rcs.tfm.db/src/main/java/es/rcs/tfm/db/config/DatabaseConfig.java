package es.rcs.tfm.db.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import es.rcs.tfm.db.DbNames;

@Configuration( 
		value = DbNames.DB_CONFIG )
@ComponentScan(
		basePackages = { DbNames.DB_SETUP_PKG })
@EnableJpaRepositories(
		basePackages = { DbNames.DB_REPO_PKG },
		transactionManagerRef = DbNames.DB_TX,
		entityManagerFactoryRef = DbNames.DB_EMF,
		repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
public class DatabaseConfig {

}
