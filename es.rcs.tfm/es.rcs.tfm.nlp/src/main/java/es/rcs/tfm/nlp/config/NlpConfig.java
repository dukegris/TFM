package es.rcs.tfm.nlp.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import es.rcs.tfm.nlp.NlpNames;

@Configuration( NlpNames.NLP_CONFIG )
@ComponentScan( NlpNames.NLP_SETUP_PKG )
public class NlpConfig {
	
}
