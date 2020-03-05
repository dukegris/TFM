package es.rcs.tfm.solr.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

import es.rcs.tfm.solr.IndexNames;

@Configuration( IndexNames.IDX_CONFIG )
@ComponentScan( IndexNames.IDX_SETUP_PKG )
@EnableSolrRepositories(
		namedQueriesLocation = "classpath:META-INF/solr-named-queries.properties",
		basePackages = IndexNames.IDX_REPOSITORY_PKG,
		solrClientRef = IndexNames.IDX_CLIENT,
		solrTemplateRef = IndexNames.IDX_TEMPLATE)
public class IndexConfig {
    
}
