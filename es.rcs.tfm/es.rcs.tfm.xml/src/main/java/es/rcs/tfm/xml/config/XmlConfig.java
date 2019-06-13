package es.rcs.tfm.xml.config;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import es.rcs.tfm.xml.XmlNames;

@Configuration( XmlNames.XML_CONFIG )
@ComponentScan( basePackages = {
		XmlNames.XML_SETUP_PKG} )
public class XmlConfig {

	@Bean( name = XmlNames.JAXB_CONTEXT )
	private JAXBContext getJaxbContext() {

		JAXBContext bean = null;
		
		return bean;
		
	}
	
	@Bean( name = XmlNames.JAXB_MARSHALLER )
	private Jaxb2Marshaller getJaxbMarshaller () {
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("jaxb.formatted.output", true);

		Jaxb2Marshaller bean = new Jaxb2Marshaller();
		bean.setPackagesToScan( XmlNames.XML_JATS_PKG );
		bean.setMarshallerProperties(map);
		
		return bean;
		
	}
}
