package es.rcs.tfm.bioc;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.bioc.Collection;
import org.bioc.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ncbi.mesh.DescriptorRecordSet;
import org.ncbi.pubmed.PubmedArticleSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import es.rcs.tfm.xml.XmlNames;

@RunWith(
		SpringJUnit4ClassRunner.class)
@ComponentScan(basePackages = {
		XmlNames.XML_CONFIG_PKG})
@ContextConfiguration(classes = {
		BiocTest.class })
public class BiocTest {

	public static final String BIOC_FILE = "../es.rcs.tfm.corpus/datasets/tmVar/tmVarCorpus/train.BioC.xml";
	
	@Test
	public void unmarshallFile() {
		
		File file = new File(BIOC_FILE);
		assertTrue(unmarshall(unmarshaller, file));
		
	}
	
	public static void main(String[] args) {

		try {
			File file = new File(BIOC_FILE);
	        JAXBContext jaxbContext = JAXBContext.newInstance(PubmedArticleSet.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		    unmarshall(jaxbUnmarshaller, file);
		} catch (JAXBException e) {
			e.printStackTrace();
		}

	}
	
	private static boolean unmarshall(Unmarshaller jaxbUnmarshaller, File file) {
		boolean resultOk = true;
		try {
		    
		    SAXParserFactory spf = SAXParserFactory.newInstance();
	        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
	        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
	        InputSource inputSource = new InputSource(new FileReader(file));
	        SAXSource source = new SAXSource(xmlReader, inputSource);

	        Collection collection = (Collection) jaxbUnmarshaller.unmarshal(source);
			if (collection != null) {
				if (	(collection.getDocument() != null) &&
						(!collection.getDocument().isEmpty())) {
					collection.getDocument().forEach(instance -> {
						instance.getId();
						instance.getInfon();
						instance.getPassage();
						instance.getRelation();
					});
					
				}
			}
		    
		} catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) {
			e.printStackTrace();
			resultOk = false;
		} catch (JAXBException | SAXException e) {
			e.printStackTrace();
			resultOk = false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			resultOk = false;
		}
		return resultOk;
		
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
	@Qualifier(	value = XmlNames.BIOC_MARSHALLER )
	private Jaxb2Marshaller marshaller;
		
	@Autowired
	@Qualifier( value = XmlNames.BIOC_UNMARSHALLER )
	public Unmarshaller unmarshaller;

}
