package es.rcs.tfm.mesh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.junit.runner.RunWith;
import org.ncbi.mesh.DescriptorRecordSet;
import org.springframework.context.annotation.ComponentScan;
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
		MeshTest.class })
public class MeshTest {

	public static void main(String[] args) {

		File file = new File("../es.rcs.tfm.corpus/data/mesh/desc2019.xml");

		JAXBContext jaxbContext;
		try {
		    
		    SAXParserFactory spf = SAXParserFactory.newInstance();
	        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
	        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
	        InputSource inputSource = new InputSource(new FileReader(file));
	        SAXSource source = new SAXSource(xmlReader, inputSource);
	        
			jaxbContext = JAXBContext.newInstance(DescriptorRecordSet.class);
		    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		    //jaxbUnmarshaller.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, Boolean.FALSE);

		    DescriptorRecordSet descriptorRecordSet = (DescriptorRecordSet)jaxbUnmarshaller.unmarshal(source);
		    if (descriptorRecordSet != null) {
		    	descriptorRecordSet.getLanguageCode();
		    	if ((descriptorRecordSet.getDescriptorRecord() != null) && (!descriptorRecordSet.getDescriptorRecord().isEmpty())) {
		    		descriptorRecordSet.getDescriptorRecord().forEach(instance -> {
		    			instance.getAllowableQualifiersList();
		    			instance.getAnnotation();
		    			instance.getConceptList();
		    			instance.getConsiderAlso();
		    			instance.getDateCreated();
		    			instance.getDateEstablished();
		    			instance.getDateRevised();
		    			instance.getDescriptorClass();
		    			instance.getDescriptorName();
		    			instance.getDescriptorUI();
		    			instance.getEntryCombinationList();
		    			instance.getHistoryNote();
		    			instance.getNLMClassificationNumber();
		    			instance.getOnlineNote();
		    			instance.getPharmacologicalActionList();
		    			instance.getPreviousIndexingList();
		    			instance.getPublicMeSHNote();
		    			instance.getSeeRelatedList();
		    			instance.getTreeNumberList();
		    			/*
A. Anatomy
B. Organisms
C. Diseases
D. Chemicals and Drugs
E. Analytical, Diagnostic and Therapeutic Techniques and Equipment
F. Psychiatry and Psychology
G. Phenomena and Processes
H. Disciplines and Occupations
I. Anthropology, Education, Sociology and Social Phenomena
J. Technology, Industry, Agriculture
K. Humanities 
L. Information Science 
M. Named Groups
N. Health Care
V. Publication Characteristics 
Z. Geographicals
		    			 */
		    		});
		    	}
		    }
		    
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXNotRecognizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
		    
}
