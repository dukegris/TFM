package es.rcs.tfm.jats.model;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import es.rcs.tfm.jats.Article;
import es.rcs.tfm.xml.XmlNames;

@RunWith(
		SpringJUnit4ClassRunner.class)
@ComponentScan(basePackages = {
		XmlNames.XML_CONFIG_PKG})
@ContextConfiguration(classes = {
		ModelTest.class })
public class ModelTest {

	public static void main(String[] args) {

		File file1 = new File("./data/PMC1790863/pone.0000217.nxml");
		//File file2 = new File("./data/PMC6317384/PAMJ-30-287.nxml");

		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(Article.class);
		    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		    //jaxbUnmarshaller.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, Boolean.TRUE);
		    Article article1 = (Article)jaxbUnmarshaller.unmarshal(file1);
		    System.out.println(article1.getBody().getBase());
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testMarshall() {
		
	}

	@BeforeClass
	public static void startUp() throws InterruptedException {
	}
	
	@Before
	public void setUp() throws Exception {
		
	}
		
    @After
    public void tearDown() {
    	
    }
	
	@AfterClass
	public static void shutDown() {
	}

	@Autowired 
	@Qualifier(	value = XmlNames.JAXB_MARSHALLER )
	private Jaxb2Marshaller marshaller;
		
}
