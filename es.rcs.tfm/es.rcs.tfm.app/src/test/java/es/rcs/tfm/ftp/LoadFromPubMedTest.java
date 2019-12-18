package es.rcs.tfm.ftp;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
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
import org.ncbi.pubmed.ObjectList;
import org.ncbi.pubmed.PMID;
import org.ncbi.pubmed.PubMedPubDate;
import org.ncbi.pubmed.PubmedArticle;
import org.ncbi.pubmed.PubmedArticleSet;
import org.ncbi.pubmed.PubmedBookArticle;
import org.ncbi.pubmed.PubmedBookData;
import org.ncbi.pubmed.PubmedData;
import org.ncbi.pubmed.ReferenceList;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import junit.framework.TestCase;

@RunWith(
		SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		LoadFromPubMedTest.class })
@SuppressWarnings("unused")
public class LoadFromPubMedTest extends TestCase {
    
	String corpusBaseLineDirectory = "D:\\Workspace-TFM\\TFM\\es.rcs.tfm\\es.rcs.tfm.corpus\\data\\pubmed";

	@Test
	public void testXml() {
		
		String fileXML = "pubmed19n0001.xml";
		Path pathXML = Paths.get(corpusBaseLineDirectory + "\\" + fileXML);

		JAXBContext jaxbContext;
		try {
		    
		    SAXParserFactory spf = SAXParserFactory.newInstance();
	        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
	        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
	        InputSource inputSource = new InputSource(new FileReader(pathXML.toFile()));
	        SAXSource source = new SAXSource(xmlReader, inputSource);
	        
			jaxbContext = JAXBContext.newInstance(PubmedArticleSet.class);
		    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		    //jaxbUnmarshaller.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, Boolean.FALSE);
		    
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

			    									String strArticleStatus = instance.getPubStatus();

			    								});
			    							}
			    						}

			    						// Objects
			    						ObjectList objects = pubmedData.getObjectList();
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
			    						List<ReferenceList> references = pubmedData.getReferenceList();
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
		    
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (SAXNotRecognizedException e) {
			e.printStackTrace();
		} catch (SAXNotSupportedException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGzip() {
		
		String fileGZ = "pubmed19n0001.xml.gz";
		Path pathGZ = Paths.get(corpusBaseLineDirectory + "\\" + fileGZ);
		String fileXML = "pubmed19n0001.xml";
		Path pathXML = Paths.get(corpusBaseLineDirectory + "\\" + fileXML);
		
		try {
			FileInputStream fis = new FileInputStream(pathGZ.toFile());
			GZIPInputStream gis = new GZIPInputStream(fis);
            FileOutputStream fos = new FileOutputStream(pathXML.toFile());
			//FileOutputStream fos = new FileOutputStream(fileXML);
			//GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = gis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			// close resources
			fos.close();
			gis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCheck() {
		
		boolean result = false;
		
		String fileGZ = "pubmed19n0001.xml.gz";
		String fileMD5 = "pubmed19n0001.xml.gz.md5";

		Path pathGZ = Paths.get(corpusBaseLineDirectory + "\\" + fileGZ);
		Path pathMD5 = Paths.get(corpusBaseLineDirectory + "\\" + fileMD5);
		
		String md5loaded = null;
		String md5Calculated = null;

		StringBuilder sb = new StringBuilder();
		try (Stream<String> stream = Files.lines( pathMD5, StandardCharsets.UTF_8)) {
			stream.forEach(s -> sb.append(s));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (sb.length()>0) {
			String[] strs = sb.toString().split("\\=\\s*");
			if (strs.length == 2) {
				md5loaded = strs[1];
			}
		}
		
		try (InputStream is = Files.newInputStream(pathGZ)) {
		    md5Calculated = DigestUtils.md5Hex(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (StringUtils.isNotBlank(md5loaded)) {
			if (md5loaded.equals(md5Calculated)) {
				result = true;
			}
		}
		
		Assert.assertTrue(result);
		
	}

	@Test
	public void testFtp() {

		String server = "ftp.ncbi.nlm.nih.gov";
		int port = 21;
		String username = "anonymous";
		String password = "password";
		
		String baselineDirectory = "/pubmed/baseline";
		//String baselineDirectory2016 = "/pubmed/.baseline-2016";
		//String baselineDirectory2017 = "/pubmed/.baseline-2017";
		//String baselineDirectory2018 = "/pubmed/.baseline-2018";
		//String baselineDirectory2019 = "/pubmed/.baseline-2019";

		String updateDirectory = "/pubmed/updatefiles";
		//String updateDirectory2016 = "/pubmed/.updatefiles-2016";
		//String updateDirectory2017 = "/pubmed/.updatefiles-2017";
		//String updateDirectory2018 = "/pubmed/.updatefiles-2018";
		//String updateDirectory2019 = "/pubmed/.updatefiles-2019";
		
		//Path corpusDirectory_0 = Paths.get("/data/corpus");
		//Path corpusDirectory_1 = Paths.get(URI.create("file:///data/corpus/"));
		//Path corpusDirectory_2 = Paths.get("C:\\home\\joe\\foo");
		
		FTPClient ftp = new FTPClient();
		ftp.setControlKeepAliveTimeout(300);

		FTPClientConfig config = new FTPClientConfig();
	    ftp.configure(config );
	    
	    boolean error = false;
	    try {
			int reply;
			ftp.connect(server, port);
			ftp.login(username, password);
			ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            
System.out.print(ftp.getReplyString());
			
			reply = ftp.getReplyCode();
			
			FTPFile[] ftpFiles = ftp.listFiles(baselineDirectory);
			if ((ftpFiles != null) && (ftpFiles.length > 0)) {
				int i = 0;
				for (FTPFile ftpFile: ftpFiles) {

					i++;
					if (i == 4) break;
					
					String group = ftpFile.getGroup();
					int hardLinkCount = ftpFile.getHardLinkCount();
					String link = ftpFile.getLink();
					String name = ftpFile.getName();
					String rawListing = ftpFile.getRawListing();
					long size = ftpFile.getSize();
					Calendar timestamp = ftpFile.getTimestamp();
					int type = ftpFile.getType();
					String user = ftpFile.getUser();
					boolean isDirectory = ftpFile.isDirectory();
					boolean isFile = ftpFile.isFile();
					boolean isSymbolicLink = ftpFile.isSymbolicLink();
					boolean isUnknown = ftpFile.isUnknown();
					boolean isValid = ftpFile.isValid();
					
					boolean result = false;

					if ((isFile) && (StringUtils.isNotBlank(name))) {
						
						try {
							Path fileInCorpusDirectory = Paths.get(corpusBaseLineDirectory + "\\" + name);
							OutputStream out = new BufferedOutputStream(new FileOutputStream(fileInCorpusDirectory.toFile().getAbsolutePath()));
							result = ftp.retrieveFile(baselineDirectory + "/" + ftpFile.getName(), out);
							out.close();
							if (result) {
				                System.out.println("File #1 has been downloaded successfully.");
				            } else {
				                System.out.println("--FAIL--: File #1.");
				            }
							reply = ftp.getReplyCode();
				            if (FTPReply.isPositiveCompletion(reply)) {
				            	System.out.println("Vamos bien");
				            } else {
				            	result = false;
				            	break;
				            }

						} catch (InvalidPathException e) {
							e.printStackTrace();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (SecurityException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
				}
			}
			
			boolean result = ftp.completePendingCommand();
            if (result) {
                System.out.println("Igual nos vmaos");
            } else {
                System.out.println("--FAIL--");
            }

            if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
System.err.println("FTP server refused connection.");
			}
			ftp.logout();
		} catch(IOException e) {
			error = true;
			e.printStackTrace();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch(IOException ioe) {
					error = true;
					ioe.printStackTrace();
				}
			}
		}		
	}

	@BeforeClass
	public static void startUp() {
	}
	
	@Before
	public void setUp() {
	}
		
    @After
    public void tearDown() {
    }
	

	@AfterClass
	public static void shutDown() {
	}

}
