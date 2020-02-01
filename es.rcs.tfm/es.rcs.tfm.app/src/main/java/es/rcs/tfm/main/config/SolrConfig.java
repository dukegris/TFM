package es.rcs.tfm.main.config;

import java.io.IOException;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.solr.core.SolrTemplate;
import org.xml.sax.SAXException;

import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.solr.IndexNames;
import es.rcs.tfm.solr.model.PubArticleIdx;

@Configuration( AppNames.SOLR_CONFIG )
@PropertySource("classpath:/META-INF/solr.properties")
public class SolrConfig {

	@Value("${tfm.solr.home}") private String solrHome;

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
    	EmbeddedSolrServer server = new EmbeddedSolrServer(Paths.get(solrHome), PubArticleIdx.CORE);
    	return server;
    	
    }
 
    @Bean( name = IndexNames.IDX_TEMPLATE )
    public SolrTemplate solrTemplate() throws Exception {
        return new SolrTemplate(solrClient());
    }

}
