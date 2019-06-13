package es.rcs.tfm.main.config;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.util.ResourceUtils;
import org.xml.sax.SAXException;

import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.solr.IndexNames;
import es.rcs.tfm.solr.model.Article;

@Configuration( AppNames.SOLR_CONFIG )
public class SolrConfig {

    /*
    @Bean( name = IndexNames.IDX_SERVER )
	public SolrServer solrServer(@Value("${solr.host}") String solrHost) {
		return new HttpSolrServer(solrHost);
	}
	*/
	
    @Bean( name = IndexNames.IDX_CLIENT )
    public SolrClient solrClient() throws ParserConfigurationException, IOException, SAXException {

    	// return new HttpSolrClient.Builder(solrURL).build();
        //EmbeddedSolrServerFactory factory = new EmbeddedSolrServerFactory("classpath:solr");
    	//return factory.getSolrClient();
    	EmbeddedSolrServer server = new EmbeddedSolrServer(ResourceUtils.getFile("classpath:solr").toPath(), Article.CORE);
    	return server;
    	
    }
 
    @Bean( name = IndexNames.IDX_TEMPLATE )
    public SolrTemplate solrTemplate() throws Exception {
        return new SolrTemplate(solrClient());
    }

}
