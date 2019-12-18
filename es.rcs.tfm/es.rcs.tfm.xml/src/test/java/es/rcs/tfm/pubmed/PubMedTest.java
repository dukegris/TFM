package es.rcs.tfm.pubmed;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Calendar;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ncbi.pubmed.Abstract;
import org.ncbi.pubmed.Article;
import org.ncbi.pubmed.ArticleId;
import org.ncbi.pubmed.ArticleTitle;
import org.ncbi.pubmed.BookDocument;
import org.ncbi.pubmed.DeleteCitation;
import org.ncbi.pubmed.MedlineCitation;
import org.ncbi.pubmed.PMID;
import org.ncbi.pubmed.PubMedPubDate;
import org.ncbi.pubmed.PubmedArticle;
import org.ncbi.pubmed.PubmedArticleSet;
import org.ncbi.pubmed.PubmedBookArticle;
import org.ncbi.pubmed.PubmedBookData;
import org.ncbi.pubmed.PubmedData;
import org.ncbi.pubmed.ReferenceList;
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
		PubMedTest.class })
public class PubMedTest {

	public static final String PUBMED_FILE = "../es.rcs.tfm.corpus/data/corpus/pubmed_xml/pubmed19n0973.xml";
	
	@Test
	public void unmarshallFile() {
		
		File file = new File(PUBMED_FILE);
		assertTrue(unmarshall(unmarshaller, file));
		
	}
	
	public static void main(String[] args) {

		try {
			File file = new File(PUBMED_FILE);
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
	        spf.setNamespaceAware(true);
	        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
	        InputSource inputSource = new InputSource(new FileReader(file));
	        SAXSource source = new SAXSource(xmlReader, inputSource);
		    
		    PubmedArticleSet pubmedArticleSet = (PubmedArticleSet)jaxbUnmarshaller.unmarshal(source);
		    if (pubmedArticleSet != null) {
		    	
		    	DeleteCitation deleteCitation = pubmedArticleSet.getDeleteCitation();
		    	List<Object> pubSet = pubmedArticleSet.getPubmedArticleOrPubmedBookArticle();

		    	if (deleteCitation != null) {
		    		List<PMID> deletedPmids = deleteCitation.getPMID();
		    		if ((deletedPmids != null) && (!deletedPmids.isEmpty())) {
		    			deletedPmids.forEach(deletedPmid -> {
		    				String strArticlePmid = deletedPmid.getvalue();
		    				String strArticleVersion = deletedPmid.getVersion();
		    			});
		    		}
		    	} else if ((pubSet != null) && (!pubSet.isEmpty())) {
			    	pubSet.forEach(p -> {
			    		if (p!=null) {

				    		String strArticleTitle = null;
		    				String strArticleAbstract = null;

		    				ArticleTitle articleTitle = null;
			    			Abstract articleAbstract = null;
			    			if (p instanceof  PubmedArticle) {
			    				PubmedArticle pubmedArticle = (PubmedArticle)p;
			    				if (pubmedArticle != null) {
			    					PubmedData pubmedData = pubmedArticle.getPubmedData();
			    					if (pubmedData != null) {
			    						
			    						// IDS
			    						if (	(pubmedData.getArticleIdList() != null) && 
			    								(pubmedData.getArticleIdList().getArticleId() != null) &&
			    								(!pubmedData.getArticleIdList().getArticleId().isEmpty())) {
			    							List<ArticleId> instances = pubmedData.getArticleIdList().getArticleId();
			    							if ((instances != null) && (!instances.isEmpty())) {
			    								instances.forEach(instance -> {
			    									
			    									String strArticleIdType = instance.getIdType();
			    									String strArticleIdValue = instance.getvalue();

			    								});
			    							}
			    						}
			    						
			    						// History
			    						if (	(pubmedData.getHistory() != null) && 
			    								(pubmedData.getHistory().getPubMedPubDate() != null) &&
			    								(!pubmedData.getHistory().getPubMedPubDate().isEmpty())) {
			    							List<PubMedPubDate> instances = pubmedData.getHistory().getPubMedPubDate();
			    							if ((instances != null) && (!instances.isEmpty())) {
			    								instances.forEach(instance -> {
			    									
			    									Calendar calArticleStatusDate = Calendar.getInstance();
			    									if (instance.getSecond() != null)	calArticleStatusDate.set(Calendar.SECOND,		Integer.parseInt(instance.getSecond()));
			    									if (instance.getMinute() != null)	calArticleStatusDate.set(Calendar.MINUTE,		Integer.parseInt(instance.getMinute()));
			    									if (instance.getHour() != null)		calArticleStatusDate.set(Calendar.HOUR,			Integer.parseInt(instance.getHour()));
			    									if (instance.getDay() != null)		calArticleStatusDate.set(Calendar.DAY_OF_MONTH,	Integer.parseInt(instance.getDay().getvalue()));
			    									if (instance.getMonth() != null)	calArticleStatusDate.set(Calendar.MONTH,		Integer.parseInt(instance.getMonth().getvalue()));
			    									if (instance.getYear() != null)		calArticleStatusDate.set(Calendar.YEAR,			Integer.parseInt(instance.getYear().getvalue()));

			    									// TODO
			    									@SuppressWarnings("unused")
			    									String strArticleStatus = instance.getPubStatus();

			    								});
			    							}
			    						}

			    						// Objects
			    						if (	(pubmedData.getObjectList() != null) && 
			    								(pubmedData.getObjectList().getObject() != null) &&
			    								(!pubmedData.getObjectList().getObject().isEmpty())) {
			    							List<org.ncbi.pubmed.Object> instances = pubmedData.getObjectList().getObject();
			    							if ((instances != null) && (!instances.isEmpty())) {
			    								instances.forEach(instance -> {
			    									
			    									if (	(instance.getParam() != null) &&
			    											(!instance.getParam().isEmpty())) {
			    										instance.getParam().forEach(param -> {

			    											String strParamName = param.getName();
			    											String strParamValue = param.getvalue();

			    										});
			    									}

			    									String strParamType = instance.getType();

			    								});
			    							}
			    						}

			    						// Status
			    						String strArticleStatus = pubmedData.getPublicationStatus();
			    						
			    						// References
			    						if (	(pubmedData.getReferenceList() != null) && 
			    								(!pubmedData.getReferenceList().isEmpty())) {
			    							List<ReferenceList> instances = pubmedData.getReferenceList();
			    							if ((instances != null) && (!instances.isEmpty())) {
			    								instances.forEach(instance -> {
			    									
			    									String strArticleReferenceTitle = instance.getTitle();
			    									
			    									if (	(instance.getReference() != null) &&
			    											(!instance.getReference().isEmpty())) {
			    										instance.getReference().forEach(reference -> {

			    											String strArticleReferenceCitation = reference.getCitation();

			    										});
			    									}
			    								});
			    							}
			    						}

			    					}
			    					
				    				MedlineCitation medlineCitation = pubmedArticle.getMedlineCitation();
				    				if (medlineCitation != null) {

				    					// Article
				    					Article article = medlineCitation.getArticle();
				    					if (article != null) {
						    				
				    						articleAbstract = article.getAbstract();
				    						
				    						article.getArticleDate();

				    						articleTitle = article.getArticleTitle();
				    						
				    						article.getAuthorList();
				    						article.getDataBankList();
				    						article.getGrantList();
				    						article.getJournal();
				    						article.getLanguage();
				    						article.getPaginationOrELocationID();
				    						article.getPublicationTypeList();
				    						article.getPubModel();
				    						article.getVernacularTitle();
				    						
				    					}
				    					
				    					medlineCitation.getChemicalList();
				    					medlineCitation.getCitationSubset();
				    					medlineCitation.getCoiStatement();
				    					medlineCitation.getCommentsCorrectionsList();
				    					medlineCitation.getDateCompleted();
				    					medlineCitation.getDateRevised();
				    					medlineCitation.getGeneralNote();
				    					medlineCitation.getGeneSymbolList();
				    					medlineCitation.getIndexingMethod();
				    					medlineCitation.getInvestigatorList();
				    					medlineCitation.getKeywordList();
				    					medlineCitation.getMedlineJournalInfo();
				    					medlineCitation.getMeshHeadingList();
				    					medlineCitation.getNumberOfReferences();
				    					medlineCitation.getOtherAbstract();
				    					medlineCitation.getOtherID();
				    					medlineCitation.getOwner();
				    					medlineCitation.getPersonalNameSubjectList();
				    					medlineCitation.getPMID();
				    					medlineCitation.getSpaceFlightMission();
				    					medlineCitation.getStatus();
				    					medlineCitation.getSupplMeshList();
				    					medlineCitation.getVersionDate();
				    					medlineCitation.getVersionID();

				    				}
			    				}
			    			} else if (p instanceof PubmedBookArticle) {
			    				
			    				PubmedBookArticle bookArticle = (PubmedBookArticle)p;
			    				
			    				if (	(bookArticle != null) &&
			    						(bookArticle.getBookDocument() != null)) {
				    				BookDocument article = bookArticle.getBookDocument();
			    					if (article != null) {
					    				
			    						articleAbstract = article.getAbstract();
			    						
			    						article.getArticleIdList();
	
			    						articleTitle = article.getArticleTitle();
			    						
			    						article.getAuthorList();
			    						article.getBook();
			    						article.getContributionDate();
			    						article.getDateRevised();
			    						article.getGrantList();
			    						article.getInvestigatorList();
			    						article.getItemList();
			    						article.getKeywordList();
			    						article.getLanguage();
			    						article.getLocationLabel();
			    						article.getPagination();
			    						article.getPMID();
			    						article.getPublicationType();
			    						article.getReferenceList();
			    						article.getReferenceList();
			    						article.getSections();
			    						article.getVernacularTitle();
			    						
			    					}
			    				}
				    			
			    				if (	(bookArticle != null) &&
			    						(bookArticle.getPubmedBookData() != null)) {
				    				
			    					PubmedBookData data = bookArticle.getPubmedBookData();    		
			    					if (data != null) {
			    						data.getArticleIdList();
			    						data.getHistory();
			    						data.getObjectList();
			    						data.getPublicationStatus();
		    						}
			    				}

			    			}
			    			if (articleTitle != null) strArticleTitle = articleTitle.getvalue();
			    			StringBuffer sb = new StringBuffer();
			    			articleAbstract.getAbstractText().forEach(t -> {
			    				sb.append("\t" + t.getvalue() + "\r\n");
			    			});
			    			strArticleAbstract = sb.toString();

				    		System.out.printf("\r\nTitulo: %s", strArticleTitle);
				    		System.out.printf("\r\nAbstract: \r\n%s", strArticleAbstract);
			    		}
			    		
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
	@Qualifier(	value = XmlNames.NCBI_PUBMED_MARSHALLER )
	private Jaxb2Marshaller marshaller;
		
	@Autowired
	@Qualifier( value = XmlNames.NCBI_PUBMED_UNMARSHALLER )
	public Unmarshaller unmarshaller;

}
