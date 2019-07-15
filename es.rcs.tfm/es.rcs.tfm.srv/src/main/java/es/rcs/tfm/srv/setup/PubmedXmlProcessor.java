package es.rcs.tfm.srv.setup;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.lang3.StringUtils;
import org.pubmed.schema.Abstract;
import org.pubmed.schema.AffiliationInfo;
import org.pubmed.schema.Article;
import org.pubmed.schema.ArticleId;
import org.pubmed.schema.ArticleIdList;
import org.pubmed.schema.ArticleTitle;
import org.pubmed.schema.Author;
import org.pubmed.schema.AuthorList;
import org.pubmed.schema.BookDocument;
import org.pubmed.schema.Chemical;
import org.pubmed.schema.Day;
import org.pubmed.schema.DeleteCitation;
import org.pubmed.schema.EndPage;
import org.pubmed.schema.ForeName;
import org.pubmed.schema.GeneSymbol;
import org.pubmed.schema.Identifier;
import org.pubmed.schema.Initials;
import org.pubmed.schema.Journal;
import org.pubmed.schema.JournalIssue;
import org.pubmed.schema.Keyword;
import org.pubmed.schema.Language;
import org.pubmed.schema.LastName;
import org.pubmed.schema.MedlineCitation;
import org.pubmed.schema.MedlineDate;
import org.pubmed.schema.MedlineJournalInfo;
import org.pubmed.schema.MedlinePgn;
import org.pubmed.schema.MeshHeading;
import org.pubmed.schema.Month;
import org.pubmed.schema.ObjectList;
import org.pubmed.schema.PMID;
import org.pubmed.schema.Pagination;
import org.pubmed.schema.PubMedPubDate;
import org.pubmed.schema.PubmedArticle;
import org.pubmed.schema.PubmedArticleSet;
import org.pubmed.schema.PubmedBookArticle;
import org.pubmed.schema.PubmedBookData;
import org.pubmed.schema.PubmedData;
import org.pubmed.schema.ReferenceList;
import org.pubmed.schema.Season;
import org.pubmed.schema.StartPage;
import org.pubmed.schema.Year;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.model.Autor;
import es.rcs.tfm.srv.model.Centro;
import es.rcs.tfm.srv.model.Fasciculo;
import es.rcs.tfm.srv.model.Fichero;

public class PubmedXmlProcessor 
		implements Supplier<Articulo> {

	List<PMID> deletedPmidToIterate = null;
	List<Object> articlesToIterate = null;
	boolean deletedSuccess = false;
	boolean articleSuccess = false;
	int articleIndex = 0;
	String directory;
	Fichero f;

	@Override
	public Articulo get() {
		
		// NUEVO ARTIULO
		Articulo articulo = new Articulo(f);

		if (articleSuccess) {
			Object pubmedObject = articlesToIterate.get(articleIndex);
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
    							if ((instances != null) && (!instances.isEmpty())) {
    								instances.forEach(instance -> {
    									ZonedDateTime zdt = makeDate(instance);
    									String strArticleStatus = instance.getPubStatus();
    									if (	(StringUtils.isNotBlank(strArticleStatus)) &&
    											(zdt != null)) {
        									articulo.getFechasPublicacion().put (strArticleStatus, zdt);
    									}

    								});
    							}
    						}

    						// Objects
    						ObjectList objects = pubmedData.getObjectList();
    						if (	(pubmedData.getObjectList() != null) && 
    								(pubmedData.getObjectList().getObject() != null) &&
    								(!pubmedData.getObjectList().getObject().isEmpty())) {
    							List<org.pubmed.schema.Object> instances = pubmedData.getObjectList().getObject();
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
    					
    					// MEDLINE CITATIONS
	    				MedlineCitation medlineCitation = pubmedArticle.getMedlineCitation();
	    				if (medlineCitation != null) {

	    					// ARTICLE
	    					Article article = medlineCitation.getArticle();
	    					if (article != null) {
			    				
	    						articulo.setResumen(makeResumen(article.getAbstract()));
	    						articulo.setTitulo(makeTitulo(article.getArticleTitle()));
	    						articulo.addAutores(makeAutores(article.getAuthorList()));
	    						articulo.addCentros(makeCentros(article.getAuthorList()));
	    						
	    						// Journal
	    						if (article.getJournal() != null) {

	    							Journal journal = article.getJournal();
	    							Fasciculo fasciculo = articulo.getFasciculo();
	    							fasciculo.getRevista().setNombre(journal.getTitle());
	    							fasciculo.getRevista().setCodigo(journal.getISOAbbreviation());
	    							
		    						// issn
	    							if (journal.getISSN() != null) {
	    								fasciculo.setMedio(journal.getISSN().getIssnType());
	    								if (StringUtils.isNotBlank(journal.getISSN().getvalue())) {
	    	    							fasciculo.getRevista().getIds().put("issn", journal.getISSN().getvalue());
	    								}
	    							}

		    						// issue
	    							if (journal.getJournalIssue() != null) {
										
	    								JournalIssue issue = journal.getJournalIssue();
	    								fasciculo.setMedio(issue.getCitedMedium());
	    								fasciculo.setVolumen(issue.getVolume());
	    								fasciculo.setNumero(issue.getIssue());
	    								
			    						// dates
		    							if ((issue.getPubDate() != null) &&
		    								(issue.getPubDate().getYearOrMonthOrDayOrSeasonOrMedlineDate() != null) &&
		    								(!issue.getPubDate().getYearOrMonthOrDayOrSeasonOrMedlineDate().isEmpty())) {

		    								List<Object> fechas = issue.getPubDate().getYearOrMonthOrDayOrSeasonOrMedlineDate();
		    									
	    									Calendar cal = Calendar.getInstance();
	    									cal.set(1800, 0, 0, 0, 0, 0);;
	    									Calendar test = Calendar.getInstance();
	    									test.set(1800, 0, 0, 0, 0, 0);;
	    									
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
	    										String str = String.format("%s-%s-%s", year, month, day);
	    										if (str.length()>2) fecha = str;
	    									}
	    									if (test.compareTo(cal) < 0) {
			    								ZonedDateTime zdt = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.of("Europe/Madrid"));
			    								fecha = zdt.format(DateTimeFormatter.ofPattern("yyyyMMdd HHmmss"));
	    									}
		    								fasciculo.setFecha(fecha);
		    								
	    								}
	    							}
		    						articulo.setFasciculo(fasciculo);
	    						}
	    						
	    						// pages
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
	    						
	    						// language
	    						if (	(article.getLanguage() != null) &&
	    								(!article.getLanguage().isEmpty())) {
	    							StringBuffer sb = new StringBuffer();
	    							for (Language l: article.getLanguage()) {
	    								if (sb.length()>0) sb.append(";");
	    								sb.append(l.getvalue());
	    							}
	    							if (sb.length()>0) articulo.setIdioma(sb.toString());
	    						}
	    						
	    						if (StringUtils.isNotBlank(article.getPubModel())) articulo.setMedio(article.getPubModel()); 

	    						article.getArticleDate();
	    						article.getDataBankList();
	    						article.getGrantList();
	    						article.getPublicationTypeList();
	    						article.getVernacularTitle();
	    						
	    					}

	    					// JOURNAL
	    					if (medlineCitation.getMedlineJournalInfo() != null) {
	    						MedlineJournalInfo journal = medlineCitation.getMedlineJournalInfo();
    							Fasciculo fasciculo = articulo.getFasciculo();
    							fasciculo.getRevista().setNombre(journal.getMedlineTA());
    							fasciculo.getRevista().setPais(journal.getCountry());
    							if (journal.getISSNLinking() != null) {
    								if (StringUtils.isNotBlank(journal.getISSNLinking())) {
    	    							fasciculo.getRevista().getIds().put("issn", journal.getISSNLinking());
    								}
    							}

								if (StringUtils.isNotBlank(journal.getNlmUniqueID())) {
	    							fasciculo.getRevista().getIds().put("nlm", journal.getNlmUniqueID());
								}
	    					}
    							
	    					// PMID
    						if (	(medlineCitation.getPMID() != null) && 
    								(StringUtils.isNotBlank(medlineCitation.getPMID().getvalue()))) {
    							articulo.getIds().put("pubmed", medlineCitation.getPMID().getvalue());
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

	    					medlineCitation.getCitationSubset();
	    					medlineCitation.getCoiStatement();
	    					medlineCitation.getCommentsCorrectionsList();
	    					medlineCitation.getDateCompleted();
	    					medlineCitation.getDateRevised();
	    					medlineCitation.getGeneralNote();
	    					medlineCitation.getIndexingMethod();
	    					medlineCitation.getInvestigatorList();
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
    			} else if (pubmedObject instanceof PubmedBookArticle) {
    				
    				PubmedBookArticle bookArticle = (PubmedBookArticle)pubmedObject;
    				
    				if (	(bookArticle != null) &&
    						(bookArticle.getBookDocument() != null)) {
	    				BookDocument article = bookArticle.getBookDocument();
    					if (article != null) {

    						articulo.setResumen(makeResumen(article.getAbstract()));
    						articulo.setTitulo(makeTitulo(article.getArticleTitle()));
    						articulo.addIds(makeIds(article.getArticleIdList()));
    						List<AuthorList> listaAutores = article.getAuthorList();
    						if (	(listaAutores != null) &&
    								(!listaAutores.isEmpty())) {
    							listaAutores.forEach(a -> articulo.addAutores(makeAutores(a)));
    						}
    						
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

    		}
			
    		if (StringUtils.isBlank(articulo.getTitulo())) {
    			System.out.println("OJO");
    		}
    		if (StringUtils.isBlank(articulo.getResumen())) {
    			System.out.println("OJO");
    		}
		}
		
		this.articleIndex++;
		return articulo;
	}
	
	private ZonedDateTime makeDate(PubMedPubDate instance) {
		
		if (instance == null) return null;
		
		Calendar test = Calendar.getInstance();
		test.add(Calendar.YEAR, 1);
		
		Calendar calArticleStatusDate = Calendar.getInstance();
		calArticleStatusDate.setTime(test.getTime());
		
		if (instance.getSecond() != null)	calArticleStatusDate.set(Calendar.SECOND,		Integer.parseInt(instance.getSecond()));
		if (instance.getMinute() != null)	calArticleStatusDate.set(Calendar.MINUTE,		Integer.parseInt(instance.getMinute()));
		if (instance.getHour() != null)		calArticleStatusDate.set(Calendar.HOUR,			Integer.parseInt(instance.getHour()));
		if (instance.getDay() != null)		calArticleStatusDate.set(Calendar.DAY_OF_MONTH,	Integer.parseInt(instance.getDay().getvalue()));
		if (instance.getMonth() != null)	calArticleStatusDate.set(Calendar.MONTH,		Integer.parseInt(instance.getMonth().getvalue()));
		if (instance.getYear() != null)		calArticleStatusDate.set(Calendar.YEAR,			Integer.parseInt(instance.getYear().getvalue()));
		
		ZonedDateTime zdt = null;
		if (test.compareTo(calArticleStatusDate) > 0) {
			zdt = ZonedDateTime.ofInstant(calArticleStatusDate.toInstant(), ZoneId.of("Europe/Madrid"));
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
					
					if ("Y".equals(instance.getValidYN())) {

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
					
					if ("Y".equals(instance.getValidYN())) {

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
	
	public PubmedXmlProcessor(
			String directory,
			Fichero f) {
		
		this.directory=directory;
		this.f = f;
		Path pathXML = Paths.get(directory + "\\" + f.getXmlFichero());

		JAXBContext jaxbContext;
		
		try {
		    
		    SAXParserFactory spf = SAXParserFactory.newInstance();
	        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
	        //spf.setFeature(XMLConstants.ACCESS_EXTERNAL_DTD, false);
	        
	        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
	        InputSource inputSource = new InputSource(new FileReader(pathXML.toFile()));
	        
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
		    			deletedPmidToIterate = deletedPmids;
		    			deletedSuccess = true;
		    		}
		    	} 
		    	if ((pubSet != null) && (!pubSet.isEmpty())) {
		    		articlesToIterate = pubSet;
	    			articleSuccess = true;
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
	
	public void close() {
		
	}
	
}
