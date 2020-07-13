package es.rcs.tfm.main.config;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.core.CoreContainer;
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

	@Value("${tfm.solr.embedded}") 
	private Boolean solrEmbedded;
	
	@Value("${tfm.solr.home}")
	private String solrHome;
	
	@Value("${tfm.solr.host}")
	private String solrHost;

    /*
    @Bean( name = IndexNames.IDX_SERVER )
	public SolrServer solrServer(@Value("${solr.host}") String solrHost) {
		return new HttpSolrServer(solrHost);
	}
	*/
	
    @Bean( name = IndexNames.IDX_CLIENT )
    public SolrClient solrClient() throws ParserConfigurationException, IOException, SAXException {
    	
    	SolrClient server = null;
		if (solrEmbedded) {
			CoreContainer container = new CoreContainer(solrHome);
			container.load();
			server = new EmbeddedSolrServer(container, PubArticleIdx.IDX_CORE);
		} else {
			server = new HttpSolrClient.Builder(solrHost).build();
		}
    	return server;
    }
 
    @Bean( name = IndexNames.IDX_TEMPLATE )
    public SolrTemplate solrTemplate() throws Exception {
        return new SolrTemplate(solrClient());
    }

}
