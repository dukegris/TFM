package es.rcs.tfm.srv.setup;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.lang3.StringUtils;
import org.ncbi.pubmed.Abstract;
import org.ncbi.pubmed.AffiliationInfo;
import org.ncbi.pubmed.Article;
import org.ncbi.pubmed.ArticleDate;
import org.ncbi.pubmed.ArticleId;
import org.ncbi.pubmed.ArticleIdList;
import org.ncbi.pubmed.ArticleTitle;
import org.ncbi.pubmed.Author;
import org.ncbi.pubmed.AuthorList;
import org.ncbi.pubmed.Book;
import org.ncbi.pubmed.BookDocument;
import org.ncbi.pubmed.Chemical;
import org.ncbi.pubmed.Day;
import org.ncbi.pubmed.DeleteCitation;
import org.ncbi.pubmed.EndPage;
import org.ncbi.pubmed.ForeName;
import org.ncbi.pubmed.GeneSymbol;
import org.ncbi.pubmed.Identifier;
import org.ncbi.pubmed.Initials;
import org.ncbi.pubmed.Isbn;
import org.ncbi.pubmed.Journal;
import org.ncbi.pubmed.JournalIssue;
import org.ncbi.pubmed.Keyword;
import org.ncbi.pubmed.Language;
import org.ncbi.pubmed.LastName;
import org.ncbi.pubmed.MedlineCitation;
import org.ncbi.pubmed.MedlineDate;
import org.ncbi.pubmed.MedlineJournalInfo;
import org.ncbi.pubmed.MedlinePgn;
import org.ncbi.pubmed.MeshHeading;
import org.ncbi.pubmed.Month;
import org.ncbi.pubmed.PMID;
import org.ncbi.pubmed.Pagination;
import org.ncbi.pubmed.PubDate;
import org.ncbi.pubmed.PubMedPubDate;
import org.ncbi.pubmed.PubmedArticle;
import org.ncbi.pubmed.PubmedArticleSet;
import org.ncbi.pubmed.PubmedBookArticle;
import org.ncbi.pubmed.PubmedBookData;
import org.ncbi.pubmed.PubmedData;
import org.ncbi.pubmed.ReferenceList;
import org.ncbi.pubmed.Season;
import org.ncbi.pubmed.StartPage;
import org.ncbi.pubmed.Year;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.model.Autor;
import es.rcs.tfm.srv.model.Centro;
import es.rcs.tfm.srv.model.Fasciculo;

public class PubmedXmlProcessor implements Iterator<Articulo> {

	private static final String CENTRE_Y = "Y";
	private static final String ID_NLM =			"nlm";
	private static final String ID_PUBMED =			"pubmed";
	private static final String ID_ISSN =			"issn";
	private static final String SEPARATOR = 		";";
	
	private static final String DATE_PUBDATE =		"pubdate";

	private static final String DATE_FULL_FMT =		"yyyyMMdd HHmmss";
	private static final String DATE_SIMPLE_FMT =	"%s-%s-%s";

	private static final String DATE_EUR_MADRID =	"Europe/Madrid";
	
	//private List<PMID> deletedPmidToIterate = null;
	//private boolean deletedSuccess = false;

	private List<Object> items = null;
	private boolean allOk = false;
	private int index = 0;

	public PubmedXmlProcessor(
			Path path) {

		JAXBContext jaxbContext;
		
		try {
		    
		    SAXParserFactory spf = SAXParserFactory.newInstance();
	        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
	        //spf.setFeature(XMLConstants.ACCESS_EXTERNAL_DTD, false);
	        
	        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
	        InputSource inputSource = new InputSource(new FileReader(path.toFile()));
	        
	        SAXSource source = new SAXSource(xmlReader, inputSource);
	        
			jaxbContext = JAXBContext.newInstance(PubmedArticleSet.class);
		    
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		    //SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI );
		    //Schema schema = sf.newSchema( new File( "countries_validation.xsd" ) );  
		    //StreamSource source = new StreamSource(getClass().getResourceAsStream(schemaLocation));
            //Schema schema = schemaFactory.newSchema(source);
            //jaxbUnmarshaller.setSchema(schema);
		    
		    PubmedArticleSet pubmedArticleSet = (PubmedArticleSet)jaxbUnmarshaller.unmarshal(source);
		    
		    if (pubmedArticleSet != null) {
		    	
		    	DeleteCitation deleteCitation = pubmedArticleSet.getDeleteCitation();
		    	List<Object> pubSet = pubmedArticleSet.getPubmedArticleOrPubmedBookArticle();

		    	if (deleteCitation != null) {
		    		List<PMID> deletedPmids = deleteCitation.getPMID();
		    		if ((deletedPmids != null) && (!deletedPmids.isEmpty())) {
		    			//TODO deletedPmidToIterate = deletedPmids;
		    			//TODO deletedSuccess = true;
		    		}
		    	} 
		    	if ((pubSet != null) && (!pubSet.isEmpty())) {
		    		items = pubSet;
	    			allOk = true;
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

	@Override
	public boolean hasNext() {
		return (index < items.size()-1);
	}

	@Override
	public Articulo next() {
		
		if (!allOk) return null;
		if (index == items.size()) {
			allOk = false;
			throw new NoSuchElementException();
		}
		
		// NUEVO ARTIULO
		Articulo articulo = new Articulo(); 
		Object pubmedObject = items.get(index);
		if (pubmedObject != null) {
			
			if (pubmedObject instanceof  PubmedArticle) {
				PubmedArticle pubmedArticle = (PubmedArticle)pubmedObject;
				if (pubmedArticle != null) {
					PubmedData pubmedData = pubmedArticle.getPubmedData();

					// PUBMED
					if (pubmedData != null) {
						
						// Ids
						articulo.addIds(makeIds(pubmedData.getArticleIdList()));
						
						// History
						if (	(pubmedData.getHistory() != null) && 
								(pubmedData.getHistory().getPubMedPubDate() != null) &&
								(!pubmedData.getHistory().getPubMedPubDate().isEmpty())) {
							List<PubMedPubDate> instances = pubmedData.getHistory().getPubMedPubDate();
							instances.forEach(instance -> {
								ZonedDateTime zdt = makeDate(instance);
								String strArticleStatus = instance.getPubStatus();
								if (	(StringUtils.isNotBlank(strArticleStatus)) &&
										(zdt != null)) {
									articulo.getFechasPublicacion().put(strArticleStatus, zdt);
								}
							});
						}

						// Objects
						if (	(pubmedData.getObjectList() != null) && 
								(pubmedData.getObjectList().getObject() != null) &&
								(!pubmedData.getObjectList().getObject().isEmpty())) {
							List<org.ncbi.pubmed.Object> instances = pubmedData.getObjectList().getObject();
							instances.forEach(instance -> {
								
								if (	(instance.getParam() != null) &&
										(!instance.getParam().isEmpty())) {
									instance.getParam().forEach(param -> {

										param.getName();
										param.getvalue();

									});
								}

								instance.getType();

							});
						}

						// Status
						pubmedData.getPublicationStatus();
						
						// References
						if (	(pubmedData.getReferenceList() != null) && 
								(!pubmedData.getReferenceList().isEmpty())) {
							List<ReferenceList> instances = pubmedData.getReferenceList();
							instances.forEach(instance -> {
								
								instance.getTitle();; // TODO
								
								if (	(instance.getReference() != null) &&
										(!instance.getReference().isEmpty())) {
									instance.getReference().forEach(reference -> {

										reference.getCitation(); // TODO

									});
								}
							});
						}

					}
					
					// MEDLINE CITATIONS
    				MedlineCitation medlineCitation = pubmedArticle.getMedlineCitation();
    				if (medlineCitation != null) {

    					Article article = medlineCitation.getArticle();
    					if (article != null) {
		    				
	    					// ARTICLE
    						articulo.setResumen(makeResumen(article.getAbstract()));
    						articulo.setTitulo(makeTitulo(article.getArticleTitle()));
    						articulo.addAutores(makeAutores(article.getAuthorList()));
    						articulo.addCentros(makeCentros(article.getAuthorList()));

    						// DATE
    						if (	(article.getArticleDate() != null) && 
    								(!article.getArticleDate().isEmpty())) {
    							List<ArticleDate> instances = article.getArticleDate();
    							instances.forEach(instance -> {
									ZonedDateTime zdt = makeDate(instance);
									String dateType = instance.getDateType();
									if (	(StringUtils.isNotBlank(dateType)) &&
											(zdt != null)) {
    									articulo.getFechasPublicacion().put(dateType, zdt);
									}
    							});
    						}
    						
    						if (StringUtils.isNotBlank(article.getPubModel())) articulo.setMedio(article.getPubModel()); 

    						// JOURNAL
    						if (article.getJournal() != null) {

    							Journal journal = article.getJournal();
    							Fasciculo fasciculo = articulo.getFasciculo();
    							fasciculo.setTipo(Fasciculo.REVISTA);
    							fasciculo.getRevista().setNombre(journal.getTitle());
    							fasciculo.getRevista().setCodigo(journal.getISOAbbreviation());
    							
	    						// JOURNAL - ID: ISSN
    							if (journal.getISSN() != null) {
    								fasciculo.setMedio(journal.getISSN().getIssnType());
    								if (StringUtils.isNotBlank(journal.getISSN().getvalue())) {
    	    							fasciculo.getRevista().getIds().put(ID_ISSN, journal.getISSN().getvalue());
    								}
    							}

	    						// JOURNAL - ISSUE
    							if (journal.getJournalIssue() != null) {
									
    								JournalIssue issue = journal.getJournalIssue();
    								fasciculo.setMedio(issue.getCitedMedium());
    								fasciculo.setVolumen(issue.getVolume());
    								fasciculo.setNumero(issue.getIssue());
    								
		    						// JOURNAL - DATES
    								ZonedDateTime zdt = makeDate(issue.getPubDate());
									if (	(zdt != null)) {
										String fecha = zdt.format(DateTimeFormatter.ofPattern(DATE_FULL_FMT));
										if (StringUtils.isNotBlank(fecha)) {
    										fasciculo.setFecha(fecha);
    									}
	    							}

    							}
	    						articulo.setFasciculo(fasciculo);
    						}
    						
    						// JOURNAL - PAGINATION
    						if (	(articulo.getFasciculo() != null) &&
    								(article.getPaginationOrELocationID() != null)) {
    							if (article.getPaginationOrELocationID() instanceof Pagination) {
    								Pagination pagination = (Pagination) article.getPaginationOrELocationID();
    								if (	(pagination.getStartPageOrEndPageOrMedlinePgn() != null) &&
    										(!pagination.getStartPageOrEndPageOrMedlinePgn().isEmpty())) {
    									for (Object p: pagination.getStartPageOrEndPageOrMedlinePgn()) {
    										if (p instanceof StartPage) articulo.getFasciculo().setPaginaInicial(((StartPage)p).getvalue());
    										if (p instanceof EndPage) articulo.getFasciculo().setPaginaFinal(((EndPage)p).getvalue());
    										if (p instanceof MedlinePgn) articulo.getFasciculo().setPaginaInicial(((MedlinePgn)p).getvalue());
    									}
    								}
    							}
    						}
    						
    						// LANGUAGE
    						if (	(article.getLanguage() != null) &&
    								(!article.getLanguage().isEmpty())) {
    							StringBuffer sb = new StringBuffer();
    							for (Language l: article.getLanguage()) {
    								if (sb.length()>0) sb.append(SEPARATOR);
    								sb.append(l.getvalue());
    							}
    							if (sb.length()>0) articulo.setIdioma(sb.toString());
    						}
    						

    						article.getDataBankList(); // TODO
    						article.getGrantList(); // TODO
    						article.getPublicationTypeList(); // TODO
    						article.getVernacularTitle(); // TODO
    						
    					}
						
    					// PMID
						if (	(medlineCitation.getPMID() != null) && 
								(StringUtils.isNotBlank(medlineCitation.getPMID().getvalue()))) {
							articulo.getIds().put(ID_PUBMED, medlineCitation.getPMID().getvalue());
						}

    					// JOURNAL - INFO
    					if (medlineCitation.getMedlineJournalInfo() != null) {
    						MedlineJournalInfo journal = medlineCitation.getMedlineJournalInfo();
							Fasciculo fasciculo = articulo.getFasciculo();
							fasciculo.getRevista().setNombre(journal.getMedlineTA());
							fasciculo.getRevista().setPais(journal.getCountry());
							if (journal.getISSNLinking() != null) {
								if (StringUtils.isNotBlank(journal.getISSNLinking())) {
	    							fasciculo.getRevista().getIds().put(ID_ISSN, journal.getISSNLinking());
								}
							}

							if (StringUtils.isNotBlank(journal.getNlmUniqueID())) {
    							fasciculo.getRevista().getIds().put(ID_NLM, journal.getNlmUniqueID());
							}
    					}

    					// MESH
						if (	(medlineCitation.getMeshHeadingList() != null) && 
								(medlineCitation.getMeshHeadingList().getMeshHeading() != null) &&
								(!medlineCitation.getMeshHeadingList().getMeshHeading().isEmpty())) {
							List<MeshHeading> instances = medlineCitation.getMeshHeadingList().getMeshHeading();
							instances.forEach(mesh -> {
								if (	(mesh.getDescriptorName() != null) &&
										(StringUtils.isNotBlank(mesh.getDescriptorName().getUI())) &&
										(StringUtils.isNotBlank(mesh.getDescriptorName().getvalue()))) {
									articulo.getMesh().put(
											mesh.getDescriptorName().getUI(),
											mesh.getDescriptorName().getvalue());
								} 

								if (	(mesh.getQualifierName() != null) &&
										(!mesh.getQualifierName().isEmpty())) {
									mesh.getQualifierName().forEach(m -> {
	    								if (	(m != null) &&
	    										(StringUtils.isNotBlank(m.getUI())) &&
	    										(StringUtils.isNotBlank(m.getvalue()))) {
	    									articulo.getMesh().put(
	    											m.getUI(),
	    											m.getvalue());
	    								} 
									});
								}
							});

						}

						// KEYWORDS
						if (	(medlineCitation.getKeywordList() != null) && 
								(!medlineCitation.getKeywordList().isEmpty())) {
							List<Keyword> instances = medlineCitation.getKeywordList().parallelStream()
									.flatMap(i -> (i != null) ? i.getKeyword().stream() : null)
									.collect(Collectors.toList());
							instances.forEach(k -> {
								if ((k != null) && (StringUtils.isNotBlank(k.getvalue()))) articulo.getKeywords().add(k.getvalue());
							});
						}

    					// CHEMICAL
						if (	(medlineCitation.getChemicalList() != null) && 
								(medlineCitation.getChemicalList().getChemical() != null) &&
								(!medlineCitation.getChemicalList().getChemical().isEmpty())) {
							List<Chemical> instances = medlineCitation.getChemicalList().getChemical();
							instances.forEach(q -> {
								if (	(q != null) &&
										(q.getNameOfSubstance() != null) &&
										(StringUtils.isNotBlank(q.getNameOfSubstance().getUI())) &&
										(StringUtils.isNotBlank(q.getNameOfSubstance().getvalue())) &&
										(StringUtils.isNotBlank(q.getRegistryNumber()))) {
									articulo.getMesh().put(
											q.getNameOfSubstance().getUI(),
											q.getNameOfSubstance().getvalue());
								} 
							});

						}

    					// GENE
						if (	(medlineCitation.getGeneSymbolList() != null) && 
								(medlineCitation.getGeneSymbolList().getGeneSymbol() != null) &&
								(!medlineCitation.getGeneSymbolList().getGeneSymbol().isEmpty())) {
							List<GeneSymbol> instances = medlineCitation.getGeneSymbolList().getGeneSymbol();
							instances.forEach(g -> {
								if ((g != null) && (StringUtils.isNotBlank(g.getvalue()))) articulo.getGenes().add(g.getvalue());
							});

						}
						
    					medlineCitation.getCitationSubset(); // TODO
    					medlineCitation.getCoiStatement(); // TODO
    					medlineCitation.getCommentsCorrectionsList(); // TODO
    					medlineCitation.getDateCompleted(); // TODO
    					medlineCitation.getDateRevised(); // TODO
    					medlineCitation.getGeneralNote(); // TODO
    					medlineCitation.getIndexingMethod(); // TODO
    					medlineCitation.getInvestigatorList(); // TODO
    					medlineCitation.getNumberOfReferences(); // TODO
    					medlineCitation.getOtherAbstract(); // TODO
    					medlineCitation.getOtherID(); // TODO
    					medlineCitation.getOwner(); // TODO
    					medlineCitation.getPersonalNameSubjectList(); // TODO
    					medlineCitation.getSpaceFlightMission(); // TODO
    					medlineCitation.getStatus(); // TODO
    					medlineCitation.getSupplMeshList(); // TODO
    					medlineCitation.getVersionDate(); // TODO
    					medlineCitation.getVersionID(); // TODO

    				}
				}
			} else if (pubmedObject instanceof PubmedBookArticle) {
				
				PubmedBookArticle bookArticle = (PubmedBookArticle)pubmedObject;
				
				if (	(bookArticle != null) &&
						(bookArticle.getBookDocument() != null)) {
    				BookDocument article = bookArticle.getBookDocument();
					if (article != null) {

						// ARTICLE
						articulo.setResumen(makeResumen(article.getAbstract()));
						articulo.setTitulo(makeTitulo(article.getArticleTitle()));
						articulo.addIds(makeIds(article.getArticleIdList()));
						
						// AUTORES
						List<AuthorList> listaAutores = article.getAuthorList();
						if (	(listaAutores != null) &&
								(!listaAutores.isEmpty())) {
							listaAutores.forEach(a -> articulo.addAutores(makeAutores(a)));
						}
						
    					// PMID
						if (	(article.getPMID() != null) && 
								(StringUtils.isNotBlank(article.getPMID().getvalue()))) {
							articulo.getIds().put(ID_PUBMED, article.getPMID().getvalue());
						}

						// KEYWORDS
    						if (	(article.getKeywordList() != null) && 
    								(!article.getKeywordList().isEmpty())) {
    							List<Keyword> instances = article.getKeywordList().parallelStream()
    									.flatMap(i -> (i != null) ? i.getKeyword().stream() : null)
    									.collect(Collectors.toList());
    							instances.forEach(k -> {
    								if ((k != null) && (StringUtils.isNotBlank(k.getvalue()))) articulo.getKeywords().add(k.getvalue());
    							});
    						}
 
    						// LANGUAGE
						if (	(article.getLanguage() != null) &&
								(!article.getLanguage().isEmpty())) {
							StringBuffer sb = new StringBuffer();
							for (Language l: article.getLanguage()) {
								if (sb.length()>0) sb.append(SEPARATOR);
								sb.append(l.getvalue());
							}
							if (sb.length()>0) articulo.setIdioma(sb.toString());
						}
						
						// BOOK
						if (article.getBook() != null) {

							Book journal = article.getBook();
							Fasciculo fasciculo = articulo.getFasciculo();
							fasciculo.setTipo(Fasciculo.LIBRO);
							fasciculo.setMedio(journal.getMedium());
							
							// BOOK - TITLE
							if (journal.getBookTitle() != null) {
								String str = journal.getBookTitle().getBook();
								if (StringUtils.isBlank(str)) str = journal.getBookTitle().getvalue();
								if (StringUtils.isNotBlank(str)) fasciculo.getRevista().setNombre(str);
								
								journal.getBookTitle().getPart();
								journal.getBookTitle().getSec();
								
							}

							
    						// BOOK - ID: ISSN
							if (	(journal.getIsbn() != null) &&
									(!journal.getIsbn().isEmpty())){
    							List<Isbn> instances = journal.getIsbn();
    							instances.forEach(issn -> {
    								if (StringUtils.isNotBlank(issn.getvalue())) {
    	    							fasciculo.getRevista().getIds().put(ID_ISSN, issn.getvalue());
    								}
    							});
							}
							
    						// JOURNAL - DATES
							ZonedDateTime zdt = makeDate(journal.getPubDate());
							if (	(zdt != null)) {
								String fecha = zdt.format(DateTimeFormatter.ofPattern(DATE_FULL_FMT));
								if (StringUtils.isNotBlank(fecha)) {
									fasciculo.setFecha(fecha);
								}
								articulo.getFechasPublicacion().put(DATE_PUBDATE, zdt);
							}
							
							journal.getAuthorList(); // TODO
							journal.getCollectionTitle(); // TODO
							journal.getEdition(); // TODO
							journal.getEndingDate(); // TODO
							journal.getInvestigatorList(); // TODO
							journal.getPublisher(); // TODO
							journal.getReportNumber(); // TODO
							journal.getVolume(); // TODO
							journal.getVolumeTitle(); // TODO
							
						}
						
						// BOOK - PAGINATION
						if (	(articulo.getFasciculo() != null) &&
								(article.getPagination() != null)) {
							Pagination pagination = article.getPagination();
							if (	(pagination.getStartPageOrEndPageOrMedlinePgn() != null) &&
									(!pagination.getStartPageOrEndPageOrMedlinePgn().isEmpty())) {
								for (Object p: pagination.getStartPageOrEndPageOrMedlinePgn()) {
									if (p instanceof StartPage) articulo.getFasciculo().setPaginaInicial(((StartPage)p).getvalue());
									if (p instanceof EndPage) articulo.getFasciculo().setPaginaFinal(((EndPage)p).getvalue());
									if (p instanceof MedlinePgn) articulo.getFasciculo().setPaginaInicial(((MedlinePgn)p).getvalue());
								}
							}
						}
						
						article.getContributionDate(); // TODO
						article.getDateRevised(); // TODO
						article.getGrantList(); // TODO
						article.getInvestigatorList(); // TODO
						article.getItemList(); // TODO
						article.getLocationLabel(); // TODO
						article.getPublicationType(); // TODO
						article.getReferenceList(); // TODO
						article.getSections(); // TODO
						article.getVernacularTitle(); // TODO
						
					}
				}
    			
				if (	(bookArticle != null) &&
						(bookArticle.getPubmedBookData() != null)) {
    				
					PubmedBookData data = bookArticle.getPubmedBookData(); // TODO    		
					if (data != null) {
						data.getArticleIdList(); // TODO
						data.getHistory(); // TODO
						data.getObjectList(); // TODO
						data.getPublicationStatus(); // TODO
					}
				}

			}

		}
		
		if (StringUtils.isBlank(articulo.getTitulo())) {
			System.out.println("OJO");
		}
		if (StringUtils.isBlank(articulo.getResumen())) {
			System.out.println("OJO");
		}
		
		this.index++;
	
		return articulo;
		
	}
	
	private ZonedDateTime makeDate(PubMedPubDate instance) {
		
		if (instance == null) return null;
		
		Calendar test = Calendar.getInstance();
		test.add(Calendar.YEAR, 1);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(test.getTime());
		
		if (instance.getSecond() != null)	cal.set(Calendar.SECOND,		Integer.parseInt(instance.getSecond()));
		if (instance.getMinute() != null)	cal.set(Calendar.MINUTE,		Integer.parseInt(instance.getMinute()));
		if (instance.getHour() != null)		cal.set(Calendar.HOUR,			Integer.parseInt(instance.getHour()));
		if (instance.getDay() != null)		cal.set(Calendar.DAY_OF_MONTH,	Integer.parseInt(instance.getDay().getvalue()));
		if (instance.getMonth() != null)	cal.set(Calendar.MONTH,			Integer.parseInt(instance.getMonth().getvalue()));
		if (instance.getYear() != null)		cal.set(Calendar.YEAR,			Integer.parseInt(instance.getYear().getvalue()));
		
		ZonedDateTime zdt = null;
		if (test.compareTo(cal) > 0) {
			zdt = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.of(DATE_EUR_MADRID));
		}
		return zdt;
	}
	
	private ZonedDateTime makeDate(ArticleDate instance) {
		
		if (instance == null) return null;
		
		Calendar test = Calendar.getInstance();
		test.add(Calendar.YEAR, 1);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(test.getTime());
		
		if (instance.getDay() != null)		cal.set(Calendar.DAY_OF_MONTH,	Integer.parseInt(instance.getDay().getvalue()));
		if (instance.getMonth() != null)	cal.set(Calendar.MONTH,			Integer.parseInt(instance.getMonth().getvalue()));
		if (instance.getYear() != null)		cal.set(Calendar.YEAR,			Integer.parseInt(instance.getYear().getvalue()));
		
		ZonedDateTime zdt = null;
		if (test.compareTo(cal) > 0) {
			zdt = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.of(DATE_EUR_MADRID));
		}
		return zdt;
	}
	
	private ZonedDateTime makeDate(PubDate instance) {
		
		if (instance == null) return null;

		Calendar test = Calendar.getInstance();
		test.set(1800, 0, 0, 0, 0, 0);;

		Calendar cal = Calendar.getInstance();
		cal.set(1800, 0, 0, 0, 0, 0);;
		
		if ((instance.getYearOrMonthOrDayOrSeasonOrMedlineDate() != null) &&
			(!instance.getYearOrMonthOrDayOrSeasonOrMedlineDate().isEmpty())) {

			List<Object> fechas = instance.getYearOrMonthOrDayOrSeasonOrMedlineDate();
				
			String fecha = null;
			String year = "";
			String month = "";
			String day = "";
			for (Object f: fechas) {
				if (f != null) {
					if (f instanceof Year) year = ((Year)f).getvalue();
					if (f instanceof Month) month = ((Month)f).getvalue();
					if (f instanceof Day) day = ((Day)f).getvalue();
					if (f instanceof Season) fecha = ((Season)f).getvalue();
					if (f instanceof MedlineDate) fecha = ((MedlineDate)f).getvalue();
				}
			}
			if (StringUtils.isBlank(fecha)) {
				String str = String.format(DATE_SIMPLE_FMT, year, month, day);
				if (str.length()>2) fecha = str;
			}

		}
		ZonedDateTime zdt = null;
		if (test.compareTo(cal) < 0) {
			zdt = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.of(DATE_EUR_MADRID));
		}
		return zdt;
		
	}

	private List<Autor> makeAutores(AuthorList autores) {

		ArrayList<Autor> resultado = new ArrayList<Autor>();
		if (autores != null) {
			autores.getType();
			autores.getCompleteYN();
			List<Author> instances = autores.getAuthor();
			if ((instances != null) && (!instances.isEmpty())) {
				for(Author instance: instances) {
					
					if (CENTRE_Y.equals(instance.getValidYN())) {

						Autor autor = new Autor();
						
						instance.getEqualContrib();
						autor.addIds(makeIds(instance.getIdentifier()));
						if (instance.getLastNameOrForeNameOrInitialsOrSuffixOrCollectiveName() != null) {
							instance.getLastNameOrForeNameOrInitialsOrSuffixOrCollectiveName().forEach(d -> {
								if (d!=null) {
									if (d instanceof ForeName) autor.setNombre(((ForeName)d).getvalue());
									if (d instanceof LastName) autor.setApellidos(((LastName)d).getvalue());
									if (d instanceof Initials) autor.setIniciales(((Initials)d).getvalue());
								}
							});
						}
											
						resultado.add(autor);
						
					}

				}
			}
			
		}

		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}
	
	private List<Centro> makeCentros(AuthorList autores) {

		ArrayList<Centro> resultado = new ArrayList<Centro>();
		if (autores != null) {
			autores.getType();
			autores.getCompleteYN();
			List<Author> instances = autores.getAuthor();
			if ((instances != null) && (!instances.isEmpty())) {
				for(Author instance: instances) {
					
					if (CENTRE_Y.equals(instance.getValidYN())) {

						Centro centro = new Centro();
						boolean addCentro = false;
						if (instance.getAffiliationInfo() != null) {
							for (AffiliationInfo c: instance.getAffiliationInfo()) {
								if (	(c!=null) && 
										(StringUtils.isNotBlank(c.getAffiliation()))) {
									centro.setNombre(c.getAffiliation());
									centro.addIds(makeIds(c.getIdentifier()));
									addCentro = true;
								}
							}
						}
											
						if (addCentro)resultado.add(centro);
						
					}

				}
			}
			
		}

		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}
	
	private Map<String, String> makeIds(List<Identifier> ids) {

		HashMap<String, String> resultado = new HashMap<String, String>();

		if (	(ids != null) &&
				(!ids.isEmpty())) {
			for (Identifier id: ids) {
				if (	(StringUtils.isNotBlank(id.getSource())) &&
						(StringUtils.isNotBlank(id.getvalue()))) {
					resultado.put(id.getSource(), id.getvalue());
				}
			}
		}
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}
	
	private Map<String, String> makeIds(ArticleIdList articleIdList) {

		HashMap<String, String> resultado = new HashMap<String, String>();
		
		if (articleIdList != null) {
			List<ArticleId> instances = articleIdList.getArticleId();
			if ((instances != null) && (!instances.isEmpty())) {
				
				for (ArticleId instance: instances) {
					String strArticleIdType = instance.getIdType();
					String strArticleIdValue = instance.getvalue();
					if (	(StringUtils.isNotBlank(strArticleIdType)) &&
							(StringUtils.isNotBlank(strArticleIdValue))) {
						resultado.put(strArticleIdType, strArticleIdValue);
					}

				};
				
			}
		}

		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}
	
	private String makeTitulo(ArticleTitle articleTitle) {

		String resultado = null;
		if (articleTitle != null) resultado = articleTitle.getvalue();
		if (StringUtils.isBlank(resultado)) resultado = null;
		return resultado;
		
	}
	
	private String makeResumen(Abstract articleAbstract) {
		
		StringBuffer sb = new StringBuffer();
		if (articleAbstract != null) {
			articleAbstract.getAbstractText().forEach(t -> {
				sb.append("\t" + t.getvalue() + "\r\n");
			});
		}
		
		String resultado = sb.toString();
		if (StringUtils.isBlank(resultado)) resultado = null;
		return resultado;
		
	}

}
