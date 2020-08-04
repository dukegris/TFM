package es.rcs.tfm.db.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import es.rcs.tfm.db.DbNames;

@EnableJpaRepositories(
		transactionManagerRef = DbNames.DB_TX,
		entityManagerFactoryRef = DbNames.DB_EMF,
		repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class,
		basePackages = {
				DbNames.DB_REPO_PKG })
@Configuration( 
		DbNames.DB_CONFIG )
@ComponentScan({
		DbNames.DB_SETUP_PKG })
public class DbConfig {

}
