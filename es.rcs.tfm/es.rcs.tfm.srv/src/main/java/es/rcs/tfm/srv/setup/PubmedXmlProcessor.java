package es.rcs.tfm.srv.setup;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.lang3.StringUtils;
import org.ncbi.pubmed.Abstract;
import org.ncbi.pubmed.AbstractText;
import org.ncbi.pubmed.AccessionNumber;
import org.ncbi.pubmed.AffiliationInfo;
import org.ncbi.pubmed.Article;
import org.ncbi.pubmed.ArticleDate;
import org.ncbi.pubmed.ArticleIdList;
import org.ncbi.pubmed.ArticleTitle;
import org.ncbi.pubmed.AuthorList;
import org.ncbi.pubmed.BeginningDate;
import org.ncbi.pubmed.Book;
import org.ncbi.pubmed.BookDocument;
import org.ncbi.pubmed.BookTitle;
import org.ncbi.pubmed.ChemicalList;
import org.ncbi.pubmed.CollectionTitle;
import org.ncbi.pubmed.CollectiveName;
import org.ncbi.pubmed.ContributionDate;
import org.ncbi.pubmed.DataBankList;
import org.ncbi.pubmed.DateCompleted;
import org.ncbi.pubmed.DateRevised;
import org.ncbi.pubmed.Day;
import org.ncbi.pubmed.DeleteCitation;
import org.ncbi.pubmed.ELocationID;
import org.ncbi.pubmed.EndPage;
import org.ncbi.pubmed.EndingDate;
import org.ncbi.pubmed.ForeName;
import org.ncbi.pubmed.GeneSymbolList;
import org.ncbi.pubmed.GeneralNote;
import org.ncbi.pubmed.GrantList;
import org.ncbi.pubmed.History;
import org.ncbi.pubmed.ISSN;
import org.ncbi.pubmed.Identifier;
import org.ncbi.pubmed.Initials;
import org.ncbi.pubmed.InvestigatorList;
import org.ncbi.pubmed.Isbn;
import org.ncbi.pubmed.ItemList;
import org.ncbi.pubmed.Journal;
import org.ncbi.pubmed.JournalIssue;
import org.ncbi.pubmed.Keyword;
import org.ncbi.pubmed.KeywordList;
import org.ncbi.pubmed.Language;
import org.ncbi.pubmed.LastName;
import org.ncbi.pubmed.MedlineCitation;
import org.ncbi.pubmed.MedlineDate;
import org.ncbi.pubmed.MedlineJournalInfo;
import org.ncbi.pubmed.MedlinePgn;
import org.ncbi.pubmed.MeshHeadingList;
import org.ncbi.pubmed.Month;
import org.ncbi.pubmed.ObjectList;
import org.ncbi.pubmed.OtherAbstract;
import org.ncbi.pubmed.OtherID;
import org.ncbi.pubmed.PMID;
import org.ncbi.pubmed.Pagination;
import org.ncbi.pubmed.PersonalNameSubjectList;
import org.ncbi.pubmed.PubDate;
import org.ncbi.pubmed.PubMedPubDate;
import org.ncbi.pubmed.PublicationType;
import org.ncbi.pubmed.PublicationTypeList;
import org.ncbi.pubmed.PubmedArticle;
import org.ncbi.pubmed.PubmedArticleSet;
import org.ncbi.pubmed.PubmedBookArticle;
import org.ncbi.pubmed.PubmedBookData;
import org.ncbi.pubmed.PubmedData;
import org.ncbi.pubmed.ReferenceList;
import org.ncbi.pubmed.Season;
import org.ncbi.pubmed.StartPage;
import org.ncbi.pubmed.Suffix;
import org.ncbi.pubmed.SupplMeshList;
import org.ncbi.pubmed.Year;

import es.rcs.tfm.srv.SrvException;
import es.rcs.tfm.srv.SrvException.SrvViolation;
import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.model.Autor;
import es.rcs.tfm.srv.model.Centro;
import es.rcs.tfm.srv.model.Descriptor;
import es.rcs.tfm.srv.model.Fasciculo;
import es.rcs.tfm.srv.model.Fecha;
import es.rcs.tfm.srv.model.Libro;
import es.rcs.tfm.srv.model.Localizacion;
import es.rcs.tfm.srv.model.Permiso;
import es.rcs.tfm.srv.model.Referencia;
import es.rcs.tfm.srv.model.Revista;
import es.rcs.tfm.srv.model.Termino;
import es.rcs.tfm.srv.model.Titulo;

public class PubmedXmlProcessor extends ArticleProcessor {

	private static final String YES = 					"Y";
	private static final String NO =					"N";

	private static final String DATE_FULL_FMT =			"yyyyMMdd HHmmss";
	private static final SimpleDateFormat DATE_FMT =	new SimpleDateFormat("yyyy/MM/dd");
	private static final String DATE_SIMPLE_FMT =		"%s-%s-%s";
	private static final String DATE_EUR_MADRID =		"Europe/Madrid";

	
	//private List<PMID> deletedPmidToIterate = null;
	//private boolean deletedSuccess = false;

	private List<Object> items = null;
	private boolean allOk = false;
	private int index = 0;

	public PubmedXmlProcessor(Path path) {
		
        SAXSource source = ArticleProcessor.getSourceFromPath(path);
        if (source != null) {
			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(PubmedArticleSet.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			    PubmedArticleSet pubmedArticleSet = (PubmedArticleSet)jaxbUnmarshaller.unmarshal(source);
			    if (pubmedArticleSet != null) {
			    	List<Object> pubSet = pubmedArticleSet.getPubmedArticleOrPubmedBookArticle();
			    	if ((pubSet != null) && (!pubSet.isEmpty())) {
			    		items = pubSet;
		    			allOk = true;
			    	}
			    	// TODO
			    	DeleteCitation deleteCitation = pubmedArticleSet.getDeleteCitation();
			    	if (deleteCitation != null) {
			    		if (	(deleteCitation.getPMID() != null) && 
			    				(!deleteCitation.getPMID().isEmpty())) {
			    			// deleteCitation.getPMID().get(0).getVersion();
			    			// deleteCitation.getPMID().get(0).getvalue();
			    		}
			    	} 
			    }
			} catch (JAXBException ex) {
				throw new SrvException(SrvViolation.JAXB_FAIL, ex);
			} catch (Exception ex) {
				throw new SrvException(SrvViolation.UNKNOWN, ex);
			}
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
			throw new SrvException(SrvViolation.NO_MORE_DATA, "No hay mas datos");
		}
		
		Articulo result = null; 

		Object item = items.get(index);
		if (item instanceof PubmedArticle) {
			PubmedArticle pubmedArticle = (PubmedArticle)item;
			result = getArticulo(pubmedArticle);
		} else if (item instanceof PubmedBookArticle) {
			PubmedBookArticle bookArticle = (PubmedBookArticle)item;
			result = getArticulo(bookArticle);
		}
		
		this.index++;
		return result;

	}

	/**
	 * Obtiene un artículo a partir de un artículo de una revista
	 * @see <a href="https://www.nlm.nih.gov/bsd/licensee/elements_descriptions.html">MEDLINE</a>
	 * @param pubmedArticle
	 * @return
	 */
	private Articulo getArticulo(PubmedArticle pubmedArticle) {

		if (pubmedArticle == null) return null;
		
		Articulo articulo = new Articulo();
		MedlineCitation medlineCitation = pubmedArticle.getMedlineCitation();
		PubmedData pubmedData = pubmedArticle.getPubmedData();
		articulo = getArticuloInfo(articulo, medlineCitation);
		articulo = getArticuloData(articulo, pubmedData);
		
		return articulo;
		
	}

	/**
	 * Obtiene un articulo a partir de un articulo incluido en un libro
	 * https://jats.nlm.nih.gov/extensions/bits/tag-library/2.0/index.html
	 * @see <a href="https://dtd.nlm.nih.gov/ncbi/pubmed/el-PubmedBookArticle.html">BOOK ARTICLE DTD</a>
	 * @param articulo
	 * @param bookArticle
	 * @param bookArticle
	 * @return
	 */
	private Articulo getArticulo(PubmedBookArticle bookArticle) {

		if (bookArticle == null) return null;

		Articulo articulo = new Articulo();
		articulo = getArticuloInfo(articulo, bookArticle.getBookDocument());
		articulo = getArticuloData(articulo, bookArticle.getPubmedBookData());
		
		return articulo;

	}	

	/**
	 * Obtiene un articulo a partir de los datos marcados en Medline
	 * @see <a href="https://www.nlm.nih.gov/bsd/licensee/elements_descriptions.html">MEDLINE</a>
	 * @see <a href="https://dtd.nlm.nih.gov/ncbi/pubmed/el-MedlineCitation.html">MEDLINE DTD</a>
	 * @param articulo
	 * @param medlineCitation
	 * @return
	 */
	private Articulo getArticuloInfo(Articulo articulo, MedlineCitation medlineCitation) {

		if (medlineCitation == null) return null;
		
		if (articulo == null) articulo = new Articulo();
		
		articulo = getArticuloInfo(articulo, medlineCitation.getArticle());
		articulo.setEstado(			medlineCitation.getStatus());
		articulo.setVersion(		medlineCitation.getVersionID());
		articulo.setVersionFecha(	makeDate(medlineCitation.getVersionDate()));
		articulo.addIds(			makeId(medlineCitation.getPMID()));
		
		// Generalmente afectan a ampliaciones como el abstract
		articulo.addIds(			makeIdsOther(medlineCitation.getOtherID())); 
		articulo.addResumen(		makeResumen(medlineCitation.getOtherAbstract()));

		articulo.addFecha(			makeFecha(medlineCitation.getDateCompleted()));
		articulo.addFecha(			makeFecha(medlineCitation.getDateRevised()));
		articulo.setRevista(		makeRevista(medlineCitation.getMedlineJournalInfo()));
		articulo.setFasciculo(		makeFasciculo(medlineCitation.getMedlineJournalInfo()));

		articulo.addAutores(		makeAutores(medlineCitation.getPersonalNameSubjectList()));
		articulo.addAutores(		makeAutores(Articulo.INVESTIGADOR, medlineCitation.getInvestigatorList()));
		
		articulo.addTerminos(		makeTerminos(medlineCitation.getMeshHeadingList()));
		articulo.addDescriptores(	makeDescriptores(medlineCitation.getKeywordList()));

		articulo.addObservaciones(	makeObservaciones(medlineCitation.getGeneralNote()));
		
		// Class 1 (chemical and drug)
		articulo.addFarmacos(		makeFarmacos(medlineCitation.getChemicalList())); 
		articulo.addTerminos(		makeTerminos(medlineCitation.getChemicalList())); 
		// Class 2 (protocol) y Class 3 (disease) y Class 4 (organism)
		articulo.addTerminos(		makeTerminos(medlineCitation.getSupplMeshList())); 

		articulo.addGenes(			makeGenes(medlineCitation.getGeneSymbolList())); // Solo en citas desde 1991 a 1995	

		medlineCitation.getNumberOfReferences(); // No utilizado desde 2010
		medlineCitation.getSpaceFlightMission(); // NASA
		medlineCitation.getIndexingMethod(); // Curated vs Automated. No usado en el proyecto
		medlineCitation.getCitationSubset(); // Citas procedentes del modelo de antiguo
		medlineCitation.getCoiStatement(); // conflict of interest
		medlineCitation.getCommentsCorrectionsList(); // Correciones invocadas por otros artículos
		
		return articulo;

	}

	/**
	 * Obtiene un articulo a partir de los datos marcados en Medline
	 * @param articulo
	 * @param article
	 * @return
	 */
	private Articulo getArticuloInfo(Articulo articulo, Article article) {

		if (article == null) return articulo;

		if (articulo == null) articulo = new Articulo();
		
		articulo.setTitulo(			makeTitulo(article.getArticleTitle()));
		articulo.setTituloOriginal(	article.getVernacularTitle());
		articulo.setResumen(		makeResumen(article.getAbstract()));
		articulo.setIdioma(			makeIdioma(article.getLanguage()));
		articulo.setMedio(			article.getPubModel()); 
		articulo.addIds(			makeIdsELocationIDS(article.getPaginationOrELocationID()) );
		articulo.addFechas(			makeFechas(article.getArticleDate()));
		articulo.addAutores(		makeAutores(Articulo.AUTOR, article.getAuthorList()));
		articulo.setRevista(		makeRevista(article.getJournal()));
		articulo.setFasciculo(		makeFasciculo(article.getJournal()));
		articulo.setLocalizacion(	makeLocalizacion(article.getPaginationOrELocationID()));
		articulo.addPermisos(		makePermisos(article.getGrantList()));
		articulo.addDatos(			makeData(article.getDataBankList()));
		articulo.addTerminos(		makeTerminos(article.getPublicationTypeList()));

		return articulo;

	}

	private Articulo getArticuloInfo(Articulo articulo, BookDocument bookDocument) {

		if (bookDocument == null) return articulo;

		if (articulo == null) articulo = new Articulo();
		articulo.setTitulo(			makeTitulo(bookDocument.getArticleTitle()));
		articulo.setTituloOriginal(	bookDocument.getVernacularTitle());
		articulo.setResumen(		makeResumen(bookDocument.getAbstract()));
		articulo.setIdioma(			makeIdioma(bookDocument.getLanguage()));
		articulo.addIds(			makeIds(bookDocument.getArticleIdList()));
		articulo.addIds(			makeId(bookDocument.getPMID()));
		articulo.addFecha(			makeFecha(bookDocument.getDateRevised()));
		articulo.addFecha(			makeFecha(bookDocument.getContributionDate()));
		articulo.addAutores(		makeAutores(bookDocument.getAuthorList()));
		articulo.addAutores(		makeAutores(Articulo.INVESTIGADOR, bookDocument.getInvestigatorList()));
		articulo.setLibro(			makeLibro(bookDocument.getBook()));
		articulo.addPermisos(		makePermisos(bookDocument.getGrantList()));
		articulo.addReferencias(	makeReferencias(bookDocument.getReferenceList()));
		articulo.addDescriptores(	makeDescriptores(bookDocument.getKeywordList()));
		articulo.addTerminos(		makeTerminos(bookDocument.getPublicationType()));
		//articulo.addPropiedades(	makePropiedades(bookDocument.getItemList()));
		
		articulo.setLocalizacion(	makeLocalizacion(bookDocument.getPagination()) );
		bookDocument.getLocationLabel(); // NO UTILIZADO EN EL PROYECTO
		//TODO articulo.addSecciones(		makeSecciones(bookDocument.getSections()));

		return articulo;

	}

	/**
	 * Obtiene un articulo a partir de los datos de referencia Pubmed
	 * @param pubmedData
	 * @return
	 */
	private Articulo getArticuloData(Articulo articulo, PubmedData pubmedData) {
		
		if (pubmedData == null) return null;

		if (articulo == null) articulo = new Articulo();
		articulo.setEstado(			pubmedData.getPublicationStatus());
		articulo.addIds(			makeIds(pubmedData.getArticleIdList()));
		articulo.addFechas(			makeFechas(pubmedData.getHistory()));
		articulo.addReferencias(	makeReferencias(pubmedData.getReferenceList()));
		//TODO
		//articulo.addPropiedades(	makePropiedades(pubmedData.getObjectList()));

		return articulo;
		
	}

	private Articulo getArticuloData(Articulo articulo, PubmedBookData pubmedBookData) {

		if (pubmedBookData == null) return articulo;

		if (articulo == null) articulo = new Articulo();
		articulo.setEstado(			pubmedBookData.getPublicationStatus());
		articulo.addIds(			makeIds(pubmedBookData.getArticleIdList()));
		articulo.addFechas(			makeFechas(pubmedBookData.getHistory()));
		// TODO
		//articulo.addPropiedades(	makePropiedades(pubmedBookData.getObjectList())

		return articulo;

	}

	/**
	 * Datos del libro
	 * @param book
	 * @return
	 */
	private Libro makeLibro(Book book) {
		
		if (book == null) return null;
		
		Libro libro = new Libro();
		
		libro.setTitulo(			makeTitulo(book.getBookTitle()));
		libro.setTituloColeccion(	makeTitulo(book.getCollectionTitle()));
		libro.setVolumen(			book.getVolume()); // Sin uso
		libro.setTituloVolumen(		book.getVolumeTitle()); // Sin uso
		libro.setMedio(				book.getMedium());
		libro.addIds(				makeIdsIsbn(book.getIsbn()));
		libro.addIds(				makeIdsELocation(book.getELocationID()));
		libro.addFecha(				makeFecha(book.getPubDate()));
		libro.addFecha(				makeFecha(book.getBeginningDate()));
		libro.addFecha(				makeFecha(book.getEndingDate()));
		libro.addAutores(			makeAutores(book.getAuthorList()));
		libro.addAutores(			makeAutores(Articulo.INVESTIGADOR, book.getInvestigatorList()));

		libro.setInforme(			book.getReportNumber());
		libro.setEdicion(			book.getEdition());
		libro.setEditor(			(book.getPublisher() != null) ? book.getPublisher().getPublisherName() : null);
		libro.setCiudad(			(book.getPublisher() != null) ? book.getPublisher().getPublisherLocation() : null);

		return libro;

	}

	/*
	private List<Seccion> makeSecciones(Sections sections) {
		
		if (	(sections == null) ||
				(sections.getSection() == null) || 
				(sections.getSection().isEmpty())) return null;
		
		List<Seccion> resultado = sections.getSection().stream().
			filter(p ->		(p != null) ).
			map(instance -> {
				return new Seccion(
						instance.getSectionTitle(),
						instance.getLocationLabel(),
						makeSecciones(instance.getSection()) );
			}).
			filter(p ->		(p != null) ).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}
	private Object makeSecciones(List<Section> section) {

		if (	(section == null) ||
				(section.isEmpty())) return null;
		
		List<Seccion> resultado = section.stream().
			filter(p ->		(p != null) ).
			map(instance -> {
				return new Seccion(
						instance.getSectionTitle(),
						instance.getLocationLabel());
			}).
			filter(p ->		(p != null) ).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}
*/

	// -------------------------------------------------------------------------------
	// TITULO
	private Titulo makeTitulo(ArticleTitle articleTitle) {

		if (	(articleTitle == null) ||
				(StringUtils.isBlank(articleTitle.getvalue()))) return null;

		Titulo resultado = new Titulo(articleTitle.getvalue());
		resultado.setLibroId(articleTitle.getBook());
		resultado.setParteId(articleTitle.getPart());
		resultado.setSeccionId(articleTitle.getSec());
		
		return resultado;
		
	}

	private Titulo makeTitulo(BookTitle bookTitle) {

		if (	(bookTitle == null) ||
				(StringUtils.isBlank(bookTitle.getvalue()))) return null;

		Titulo resultado = new Titulo(bookTitle.getvalue());
		resultado.setLibroId(bookTitle.getBook());
		resultado.setParteId(bookTitle.getPart());
		resultado.setSeccionId(bookTitle.getSec());
		
		return resultado;

	}

	private Titulo makeTitulo(CollectionTitle collectionTitle) {

		if (	(collectionTitle == null) ||
				(StringUtils.isBlank(collectionTitle.getvalue()))) return null;

		Titulo resultado = new Titulo(collectionTitle.getvalue());
		resultado.setLibroId(collectionTitle.getBook());
		resultado.setParteId(collectionTitle.getPart());
		resultado.setSeccionId(collectionTitle.getSec());
		
		return resultado;

	}

	// -------------------------------------------------------------------------------
	// RESUMEN
	private Object makeResumen(String copyrightInformation) {
		
		if (StringUtils.isBlank(copyrightInformation)) return null;
		return String.format("%s:\t%s\r\n", Articulo.COPYRIGHT, copyrightInformation);

	}

	private String makeResumenOriginal(List<AbstractText> abstractText) {
		
		if (	(abstractText == null) ||
				(abstractText.isEmpty())) return null;

		StringBuffer sb = new StringBuffer();
		abstractText.forEach(p -> {
			if (StringUtils.isNotBlank(p.getvalue())) sb.append(String.format("%s(%s):\t%s\r\n", p.getLabel(), p.getNlmCategory(), p.getvalue()));
		});
		
		String resultado = sb.toString();
		if (StringUtils.isBlank(resultado)) resultado = null;
		return resultado;

	}

	private String makeResumen(Abstract articleAbstract) {
		
		if (articleAbstract == null) return null;

		StringBuffer sb = new StringBuffer();
		sb.append(		makeResumenOriginal(articleAbstract.getAbstractText()));
		sb.append(		makeResumen(articleAbstract.getCopyrightInformation()));

		String resultado = sb.toString();
		if (StringUtils.isBlank(resultado)) resultado = null;
		return resultado;
		
	}

	private String makeResumen(List<OtherAbstract> otherAbstract) {

		if (	(otherAbstract == null) ||
				(otherAbstract.isEmpty())) return null;

		otherAbstract.get(0).getLanguage();
		otherAbstract.get(0).getType();

		StringBuffer sb = new StringBuffer();
		otherAbstract.forEach(p -> {
			sb.append(	String.format("%s\t(%s)\t{\r\n", p.getType(), p.getLanguage()));
			sb.append(	makeResumenOriginal(p.getAbstractText()));
			sb.append(	makeResumen(p.getCopyrightInformation()));
			sb.append(	"}\r\n");
		});
		
		String resultado = sb.toString();
		if (StringUtils.isBlank(resultado)) resultado = null;
		return resultado;

	}

	// -------------------------------------------------------------------------------
	// IDENTIFICACION

	private Entry<String, String> makeId(ISSN issn) {
		
		if (	(issn == null) ||
				(StringUtils.isBlank(issn.getIssnType())) ||
				(StringUtils.isBlank(issn.getvalue()))) {
			return null;
		}
		//issn.getIssnType()  Electronic | Print, 

		return new SimpleEntry<String, String>(
					Articulo.ISSN_ID_NAME,
					issn.getvalue());
		
	}

	private Entry<String, String> makeId(PMID pmid) {
		
		if (	(pmid == null) || 
				(StringUtils.isBlank(pmid.getvalue()))) return null;

		return new SimpleEntry<String, String>(
				Articulo.PUBMED_ID_NAME, 
				pmid.getvalue());
		
	}

	private Entry<String, String> makeId(String type, String value) {
		
		if (	(StringUtils.isBlank(type)) ||
				(StringUtils.isBlank(value)) ) return null;
		
		return new SimpleEntry<String, String>(
					type, 
					value);
		
	}

	Pattern IS_NUMERIC = Pattern.compile("-?\\d+(\\.\\d+)?");
	private List<Entry<String, String>> makeIds(ArticleIdList articleIdList) {

		if (	(articleIdList == null)  ||
				(articleIdList.getArticleId() == null) ||
				(articleIdList.getArticleId().isEmpty())) return null; 
		
		List<Entry<String, String>> resultado = articleIdList.getArticleId().stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getIdType())) &&
							(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance ->	{
				String type = null;
			    if (StringUtils.isBlank(instance.getIdType())) {
			    	if (IS_NUMERIC.matcher(instance.getIdType()).matches()) type = "pmid";
			    	if ("rspb".indexOf(instance.getIdType()) >= 0) type = "doi";
				}
				return new SimpleEntry<String, String>(
							type,
							instance.getvalue());
			}).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	private Map<String, String> makeIds(List<Identifier> identifiers) {

		if (	(identifiers == null)  ||
				(identifiers.isEmpty())) {
			return null; 
		}

		Map<String, String> resultado = identifiers.stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getSource())) &&
							(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> 	{
				return new SimpleEntry<String, String>(
						instance.getSource(),
						instance.getvalue());
			}).
			collect(Collectors.toMap(
					p -> p.getKey(), 
					p -> p.getValue(),
					(o1, o2) -> o1 + ", " + o2 ));
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}

	private Map<String, String> makeIdsELocation(List<ELocationID> eLocationIds) {
		
		if (	(eLocationIds == null) ||
				(eLocationIds.isEmpty()) ) return null;
		
		Map<String, String> resultado = eLocationIds.stream().
			filter(p -> (p != null) &&
						(YES.equals(p.getValidYN())) && 
						(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> new SimpleEntry<String, String>(
					instance.getEIdType(), // doi | pii
					instance.getvalue())).
			collect(Collectors.toMap(	
					p -> p.getKey(), 
					p -> p.getValue(),
					(o1, o2) -> o1 + ", " + o2 ));

		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}

	private Map<String, String> makeIdsELocationIDS(List<Object> paginationOrELocationID) {
		
		if (	(paginationOrELocationID == null) ||
				(paginationOrELocationID.isEmpty()) ) return null;
		
		Map<String, String> resultado = paginationOrELocationID.stream().
				filter(p -> (p != null) &&
							(p instanceof ELocationID) ).
				map(instance -> 
						(ELocationID)instance).
				filter(p ->	(YES.equals(p.getValidYN())) && 
							(StringUtils.isNotBlank(p.getvalue())) ).
				map(instance -> new SimpleEntry<String, String>(
						instance.getEIdType(), // doi | pii
						instance.getvalue())).
				collect(Collectors.toMap(	
						p -> p.getKey(), 
						p -> p.getValue(),
						(o1, o2) -> o1 + ", " + o2 ));

			if (resultado.isEmpty()) resultado = null;
			return resultado;
		
	}
	
	private Map<String, String> makeIdsIsbn(List<Isbn> isbn) {

		if (	(isbn == null)  ||
				(isbn.isEmpty()))  return null; 
		
		Map<String, String> resultado = isbn.stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> 	{
				return new SimpleEntry<String, String>(
						Articulo.ISBN_ID_NAME,
						instance.getvalue());
			}).
			collect(Collectors.toMap(
					p -> p.getKey(), 
					p -> p.getValue(),
					(o1, o2) -> o1 + ", " + o2 ));
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}

	private Map<String, String> makeIdsOther(List<OtherID> otherIds) {

		if (	(otherIds == null)  ||
				(otherIds.isEmpty()))  return null; 

		Map<String, String> resultado = otherIds.stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getSource())) &&
							(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> 	{
				return new SimpleEntry<String, String>(
						instance.getSource(), // NASA | KIE | PIP | POP | ARPL | CPC | IND | CPFH | CLML | NRCBL | NLM | QCIM
						instance.getvalue());
			}).
			collect(Collectors.toMap(
					p -> p.getKey(), 
					p -> p.getValue(),
					(o1, o2) -> o1 + ", " + o2 ));
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	// -------------------------------------------------------------------------------
	// FECHAS

	/**
	 * @param beginningDate
	 * @return
	 */
	private Fecha makeFecha(BeginningDate beginningDate) {
		
		if (beginningDate == null) return null;
		
		String dia = null;
		String mes = null;
		String anio = null;
		String ses = null;
		
		if (	(beginningDate.getMonthOrDayOrSeason() != null) && 
				(!beginningDate.getMonthOrDayOrSeason().isEmpty()) ){
			for (Object p: beginningDate.getMonthOrDayOrSeason()) {
				if (p instanceof Month)		mes = ((Month)p).getvalue();
				if (p instanceof Day) 		dia = ((Day)p).getvalue();
				if (p instanceof Season)	ses = ((Season)p).getvalue();
			}
			
		}
		if (	(beginningDate.getYear() != null) &&
				(StringUtils.isNotBlank(beginningDate.getYear().getvalue())) ) 
			anio = beginningDate.getYear().getvalue();

		
		Calendar test = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		test.add(Calendar.YEAR, 1);
		cal.setTime(test.getTime());
		
		if (StringUtils.isNotBlank(dia))	cal.set(Calendar.DAY_OF_MONTH,	Integer.parseInt(dia));
		if (StringUtils.isNotBlank(mes))	cal.set(Calendar.MONTH,			Integer.parseInt(mes));
		if (StringUtils.isNotBlank(anio))	cal.set(Calendar.YEAR,			Integer.parseInt(anio));

		ZonedDateTime zdf = null;
		if (test.compareTo(cal) > 0) {
			zdf = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.of(DATE_EUR_MADRID));
		}
		
		Fecha resultado = null;
		if (zdf == null) {
			resultado = new Fecha(Articulo.FECHA_INICIO, zdf);
		}
		
		if (StringUtils.isNotBlank(ses)) {
			if (resultado == null) resultado = new Fecha(Articulo.FECHA_INICIO);
			resultado.setSesion(ses);
			resultado.setAnio(anio);
		}
			
		return resultado;

	}

	/**
	 * Obtiene la fecha de contribucion
	 * @param contributionDate
	 * @return
	 */
	private Fecha makeFecha(ContributionDate contributionDate) {
		
		if (contributionDate == null) return null;
		
		String dia = null;
		String mes = null;
		String anio = null;
		String ses = null;
		
		if (	(contributionDate.getMonthOrDayOrSeason() != null) && 
				(!contributionDate.getMonthOrDayOrSeason().isEmpty()) ){
			for (Object p: contributionDate.getMonthOrDayOrSeason()) {
				if (p instanceof Month)		mes = ((Month)p).getvalue();
				if (p instanceof Day) 		dia = ((Day)p).getvalue();
				if (p instanceof Season)	ses = ((Season)p).getvalue();
			}
			
		}
		if (	(contributionDate.getYear() != null) &&
				(StringUtils.isNotBlank(contributionDate.getYear().getvalue())) ) 
			anio = contributionDate.getYear().getvalue();

		
		Calendar test = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		test.add(Calendar.YEAR, 1);
		cal.setTime(test.getTime());
		
		if (StringUtils.isNotBlank(dia))	cal.set(Calendar.DAY_OF_MONTH,	Integer.parseInt(dia));
		if (StringUtils.isNotBlank(mes))	cal.set(Calendar.MONTH,			Integer.parseInt(mes));
		if (StringUtils.isNotBlank(anio))	cal.set(Calendar.YEAR,			Integer.parseInt(anio));

		ZonedDateTime zdf = null;
		if (test.compareTo(cal) > 0) {
			zdf = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.of(DATE_EUR_MADRID));
		}
		
		Fecha resultado = null;
		if (zdf == null) {
			resultado = new Fecha(Articulo.FECHA_CONTRIBUCION, zdf);
		}
		
		if (StringUtils.isNotBlank(ses)) {
			if (resultado == null) resultado = new Fecha(Articulo.FECHA_CONTRIBUCION);
			resultado.setSesion(ses);
			resultado.setAnio(anio);
		}
			
		return resultado;

	}
	
	/**
	 * Obtiene la fecha de finalización
	 * @param dateCompleted
	 * @return
	 */
	private Fecha makeFecha(DateCompleted dateCompleted) {
		
		if (dateCompleted == null) return null;
		
		Calendar test = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		test.add(Calendar.YEAR, 1);
		cal.setTime(test.getTime());
		
		if (dateCompleted.getDay() != null)		cal.set(Calendar.DAY_OF_MONTH,	Integer.parseInt(dateCompleted.getDay().getvalue()));
		if (dateCompleted.getMonth() != null)	cal.set(Calendar.MONTH,			Integer.parseInt(dateCompleted.getMonth().getvalue()));
		if (dateCompleted.getYear() != null)	cal.set(Calendar.YEAR,			Integer.parseInt(dateCompleted.getYear().getvalue()));
		
		ZonedDateTime zdf = null;
		if (test.compareTo(cal) > 0) {
			zdf = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.of(DATE_EUR_MADRID));
		}
		
		Fecha resultado = null;
		if (zdf == null) {
			resultado = new Fecha(Articulo.FECHA_COMPLETA, zdf);
		}
		return resultado;

	}

	/**
	 * Obtiene la fecha de revisión
	 * @param dateRevised
	 * @return
	 */
	private Fecha makeFecha(DateRevised dateRevised) {
		
		if (dateRevised == null) return null;
		
		Calendar test = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		test.add(Calendar.YEAR, 1);
		cal.setTime(test.getTime());
		
		if (dateRevised.getDay() != null)	cal.set(Calendar.DAY_OF_MONTH,	Integer.parseInt(dateRevised.getDay().getvalue()));
		if (dateRevised.getMonth() != null)	cal.set(Calendar.MONTH,			Integer.parseInt(dateRevised.getMonth().getvalue()));
		if (dateRevised.getYear() != null)	cal.set(Calendar.YEAR,			Integer.parseInt(dateRevised.getYear().getvalue()));
		
		ZonedDateTime zdf = null;
		if (test.compareTo(cal) > 0) {
			zdf = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.of(DATE_EUR_MADRID));
		}
		
		Fecha resultado = null;
		if (zdf == null) {
			resultado = new Fecha(Articulo.FECHA_REVISION, zdf);
		}
		return resultado;
		
	}

	/**
	 * Obtiene la fecha de edición
	 * @param endingDate
	 * @return
	 */
	private Fecha makeFecha(EndingDate endingDate) {
		
		if (endingDate == null) return null;
		
		String dia = null;
		String mes = null;
		String anio = null;
		String ses = null;
		
		if (	(endingDate.getMonthOrDayOrSeason() != null) && 
				(!endingDate.getMonthOrDayOrSeason().isEmpty()) ){
			for (Object p: endingDate.getMonthOrDayOrSeason()) {
				if (p instanceof Month)		mes = ((Month)p).getvalue();
				if (p instanceof Day) 		dia = ((Day)p).getvalue();
				if (p instanceof Season)	ses = ((Season)p).getvalue();
			}
			
		}
		if (	(endingDate.getYear() != null) &&
				(StringUtils.isNotBlank(endingDate.getYear().getvalue())) ) 
			anio = endingDate.getYear().getvalue();

		
		Calendar test = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		test.add(Calendar.YEAR, 1);
		cal.setTime(test.getTime());
		
		if (StringUtils.isNotBlank(dia))	cal.set(Calendar.DAY_OF_MONTH,	Integer.parseInt(dia));
		if (StringUtils.isNotBlank(mes))	cal.set(Calendar.MONTH,			Integer.parseInt(mes));
		if (StringUtils.isNotBlank(anio))	cal.set(Calendar.YEAR,			Integer.parseInt(anio));

		ZonedDateTime zdf = null;
		if (test.compareTo(cal) > 0) {
			zdf = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.of(DATE_EUR_MADRID));
		}
		
		Fecha resultado = null;
		if (zdf == null) {
			resultado = new Fecha(Articulo.FECHA_EDICION, zdf);
		}
		
		if (StringUtils.isNotBlank(ses)) {
			if (resultado == null) resultado = new Fecha(Articulo.FECHA_CONTRIBUCION);
			resultado.setSesion(ses);
			resultado.setAnio(anio);
		}
			
		return resultado;

	}

	/**
	 * Fecha de publicacion
	 * @param pubDate
	 * @return
	 */
	private Fecha makeFecha(PubDate pubDate) {
		
		if (pubDate == null) return null;
		
		ZonedDateTime zdf = makeDate(pubDate);

		Fecha resultado = null;
		if (zdf == null) {
			resultado = new Fecha(Articulo.FECHA_PUBLICACION, zdf);
		}
		return resultado;

	}

	/**
	 * Obtiene las fechas asociadas al articulo
	 * @param instances
	 * @return las fechas
	 */
	private List<Fecha> makeFechas(List<ArticleDate> instances) {

		if (	(instances == null) || 
				(instances.isEmpty())) return null; 

		List<Fecha> resultado = instances.stream().
			filter(p -> 	(p != null) &&
							(StringUtils.isNotBlank(p.getDateType())) ).
			map(instance -> 	{
				Fecha fecha = null;
				ZonedDateTime zdt = makeDate(instance);
				String dateType = instance.getDateType();
				if (	(StringUtils.isNotBlank(dateType)) &&
						(zdt != null)) {
					fecha = new Fecha(dateType, zdt);
				}
				return fecha;
			}).
			filter(p -> 	(p != null) ).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}
	
	/**
	 * Obtiene la lista de eventos que corresponden con el historial
	 * @param history
	 * @return
	 */
	private List<Fecha> makeFechas(History history) {
		
		if (	(history == null) || 
				(history.getPubMedPubDate() == null) ||
				(history.getPubMedPubDate().isEmpty())) return null;

		List<Fecha> resultado = history.getPubMedPubDate().stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getPubStatus())) ).
			map(instance -> {
				ZonedDateTime zdt = makeDate(instance);
				String pubStatus = instance.getPubStatus();
				if (	(zdt == null)) {
					return null;
				}
				return new Fecha(pubStatus, zdt);
			}).
			filter(p ->		(p != null) ).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}

	// -------------------------------------------------------------------------------
	// AUTORES

	/**
	 * Lista de Autores a partir de listas de autores por tipo
	 * @param authorList
	 * @return
	 */
	private List<Autor> makeAutores(List<AuthorList> authorList) {

		if (	(authorList == null)  ||
				(authorList.isEmpty())) return null; 
		
		List<Autor> resultado = authorList.stream().
			filter(p -> (p != null) &&
						(YES.equals(p.getCompleteYN())) && 
						(p.getAuthor() != null) &&
						(!p.getAuthor().isEmpty())).
			flatMap(list -> makeAutores(list.getType(), list).stream()).
			collect(Collectors.toList());

		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	/**
	 * @param personalNameSubjectList
	 * @return
	 */
	private List<Autor> makeAutores(PersonalNameSubjectList personalNameSubjectList) {

		if (	(personalNameSubjectList == null) ||
				(personalNameSubjectList.getPersonalNameSubject() == null) ||
				(personalNameSubjectList.getPersonalNameSubject().isEmpty())) return null; 
		
		List<Autor> resultado = personalNameSubjectList.getPersonalNameSubject().stream().
			filter(p -> (p != null) ).
			map(instance ->		{
				Autor autor = new Autor();
				if (instance.getInitials() != null)	autor.setIniciales(	instance.getInitials().getvalue());
				if (instance.getForeName() != null)	autor.setNombre(	instance.getForeName().getvalue());
				if (instance.getLastName() != null)	autor.setApellidos(	instance.getLastName().getvalue());
				if (instance.getSuffix() != null)	autor.setSufijo(	instance.getSuffix().getvalue());
				return autor;
			}).
			collect(Collectors.toList());

		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	/**
	 * Obtiene los autores
	 * @param type
	 * @param authorList
	 * @return los autores
	 */
	private List<Autor> makeAutores(String type, AuthorList authorList) {

		if (	(authorList == null)  ||
				(authorList.getAuthor() == null) ||
				(authorList.getAuthor().isEmpty())) return null; 

		List<Autor> resultado = authorList.getAuthor().stream().
			filter(p ->		(p !=null ) && 
							(YES.equals(p.getValidYN())) ).
			map(instance ->		{
				Autor autor = new Autor();
				autor.setTipo(type);
				//TODO
				instance.getEqualContrib();
				autor.addIds(makeIds(instance.getIdentifier()));
				if (instance.getLastNameOrForeNameOrInitialsOrSuffixOrCollectiveName() != null) {
					instance.getLastNameOrForeNameOrInitialsOrSuffixOrCollectiveName().forEach(d -> {
						if (d!=null) {
							if (d instanceof Initials)			autor.setIniciales(((Initials)d).getvalue());
							if (d instanceof Suffix)			autor.setSufijo(((Suffix)d).getvalue());
							if (d instanceof ForeName)			autor.setNombre(((ForeName)d).getvalue());
							if (d instanceof LastName)			autor.setApellidos(((LastName)d).getvalue());
							if (d instanceof CollectiveName)	autor.setGrupo(((CollectiveName)d).getvalue());
						}
					});
				}
				autor.addCentros(makeCentros(instance.getAffiliationInfo()));
				return autor;
			}).
			filter(p ->		(p != null) ).
			collect(Collectors.toList());

		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}

	private List<Autor> makeAutores(String type, InvestigatorList investigatorList) {

		if (	(investigatorList == null)  ||
				(investigatorList.getInvestigator() == null)  ||
				(investigatorList.getInvestigator().isEmpty())) return null; 

		List<Autor> resultado = investigatorList.getInvestigator().stream().
			filter(p ->		(p !=null ) && 
							(YES.equals(p.getValidYN())) ).
			map(instance ->		{
				Autor autor = new Autor();
				autor.setTipo(type);
				if (	(instance.getInitials() != null) &&
						(StringUtils.isNotBlank(instance.getInitials().getvalue())) )
					autor.setIniciales(		instance.getInitials().getvalue());
				if (	(instance.getSuffix() != null) &&
						(StringUtils.isNotBlank(instance.getSuffix().getvalue())) )
					autor.setSufijo(		instance.getSuffix().getvalue());
				if (	(instance.getForeName() != null) &&
						(StringUtils.isNotBlank(instance.getForeName().getvalue())) )
					autor.setNombre(		instance.getForeName().getvalue());
				if (	(instance.getLastName() != null) &&
						(StringUtils.isNotBlank(instance.getLastName().getvalue())) )
					autor.setApellidos(		instance.getLastName().getvalue());
				autor.addCentros(makeCentros(instance.getAffiliationInfo()));
				return autor;
			}).
			filter(p ->		(p != null) ).
			collect(Collectors.toList());

		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	private List<Centro> makeCentros(List<AffiliationInfo> affiliationInfo) {

		if (	(affiliationInfo == null)  ||
				(affiliationInfo.isEmpty())) return null; 
		
		List<Centro> resultado = affiliationInfo.stream().
			filter(p ->	(	(p != null) && 
							(StringUtils.isNotBlank(p.getAffiliation()))) ). 
			map(instance ->	{
				Centro centro = new Centro();
				centro.setNombre(instance.getAffiliation());
				centro.addIds(	makeIds(instance.getIdentifier()));
				return centro;
			}).
			collect(Collectors.toList());

		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	// -------------------------------------------------------------------------------
	// OTROS

	/**
	 * Indice para la localizacion del fasciculo en la revista pagina, url, ...
	 * @param paginationOrELocationID
	 * @return
	 */
	private Localizacion makeLocalizacion(List<Object> paginationOrELocationID) {
		
		if (	(paginationOrELocationID == null) ||
				(paginationOrELocationID.isEmpty()) ) return null;
		
		Localizacion resultado = null;
		if (paginationOrELocationID instanceof Pagination) {
			Pagination pagination = (Pagination) paginationOrELocationID;
			resultado = makeLocalizacion(pagination);
		} else if (paginationOrELocationID instanceof ELocationID) {
			ELocationID location = (ELocationID) paginationOrELocationID;
			if (YES.equals(location.getValidYN())) {
				resultado = new Localizacion();
				resultado.setTipo(location.getEIdType());
				resultado.setPath(location.getvalue());
			}
		}

		return resultado;
		
	}

	/**
	 * Crea una localización de un documento paginado
	 * @param pagination
	 * @return
	 */
	private Localizacion makeLocalizacion(Pagination pagination) {

		if (	(pagination == null) ||
				(pagination.getStartPageOrEndPageOrMedlinePgn() == null) ||
				(pagination.getStartPageOrEndPageOrMedlinePgn().isEmpty())) return null;
		
		String ref = null;
		String ini = null;
		String fin = null;
		for (Object p: pagination.getStartPageOrEndPageOrMedlinePgn()) {
			if (p instanceof StartPage) ini = ((StartPage)p).getvalue();
			if (p instanceof EndPage) fin = ((EndPage)p).getvalue();
			if (p instanceof MedlinePgn) ref = ((MedlinePgn)p).getvalue();
		}

		Localizacion resultado =  new Localizacion();
		resultado.setPagina(ref, ini, fin);
		return resultado;
		
	}

	/**
	 * Crea una lista de idiomas 
	 * @param language
	 * @return
	 */
	private String makeIdioma(List<Language> language) {
		
		if (	(language == null) ||
				(language.isEmpty())) return null;
	
		StringBuffer sb = new StringBuffer();
		for (Language l: language) {
			if (sb.length()>0) sb.append(Articulo.SEPARATOR);
			sb.append(l.getvalue());
		}
		
		return sb.toString();
	
	}
	
	/**
	 * Obtiene una fecha compatible Java
	 * @param articleDate
	 * @return la fecha
	 */
	private ZonedDateTime makeDate(ArticleDate articleDate) {
		
		if (articleDate == null) return null;
		
		Calendar test = Calendar.getInstance();
		test.add(Calendar.YEAR, 1);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(test.getTime());
		
		if (articleDate.getDay() != null)		cal.set(Calendar.DAY_OF_MONTH,	Integer.parseInt(articleDate.getDay().getvalue()));
		if (articleDate.getMonth() != null)		cal.set(Calendar.MONTH,			Integer.parseInt(articleDate.getMonth().getvalue()));
		if (articleDate.getYear() != null)		cal.set(Calendar.YEAR,			Integer.parseInt(articleDate.getYear().getvalue()));
		
		ZonedDateTime resultado = null;
		if (test.compareTo(cal) > 0) {
			resultado = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.of(DATE_EUR_MADRID));
		}
		return resultado;

	}
	
	/**
	 * Obtiene una fecha compatible Java
	 * @param articleDate
	 * @return la fecha
	 */
	private ZonedDateTime makeDate(String fecha) {
		
		if (fecha == null) return null;
		
		ZonedDateTime resultado = null;

		try {
			Date date = DATE_FMT.parse(fecha);
			resultado = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(DATE_EUR_MADRID));
		} catch (Exception ex) {}
		
		return resultado;

	}

	/**
	 * Obtiene una fecha compatible Java
	 * @param instance
	 * @return la fecha
	 */
	private ZonedDateTime makeDate(PubDate pubDate) {
		
		if (	(pubDate == null) || 
				(pubDate.getYearOrMonthOrDayOrSeasonOrMedlineDate() == null) ||
				(pubDate.getYearOrMonthOrDayOrSeasonOrMedlineDate().isEmpty())) {
			return null;
		}

		Calendar test = Calendar.getInstance();
		test.set(1800, 0, 0, 0, 0, 0);;

		Calendar cal = Calendar.getInstance();
		cal.set(1800, 0, 0, 0, 0, 0);;

		List<Object> instances = pubDate.getYearOrMonthOrDayOrSeasonOrMedlineDate();
				
		String fecha = null;
		String year = "";
		String month = "";
		String day = "";
		for (Object instance: instances) {
			if (instance != null) {
				if (instance instanceof Year)			year = ((Year)instance).getvalue();
				if (instance instanceof Month)			month = ((Month)instance).getvalue();
				if (instance instanceof Day)			day = ((Day)instance).getvalue();
				// TODO
				// if (instance instanceof Season)			fecha = ((Season)instance).getvalue();
				// TODO
				if (instance instanceof MedlineDate)	fecha = ((MedlineDate)instance).getvalue();
			}
		}
		if (StringUtils.isBlank(fecha)) {
			// TODO MedlineDate
			// Contains the entire date string for a <PubDate> that does not fit the available date patterns: 
			// YYYY, YYYY + MM, YYYY + MM + DD, YYYY + SEASON.
			try {
				String str = String.format(DATE_SIMPLE_FMT, year, month, day);
				if (str.length()>2) fecha = str;
			} catch (Exception ex) {}
		}

		ZonedDateTime resultado = null;
		if (test.compareTo(cal) < 0) {
			resultado = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.of(DATE_EUR_MADRID));
		}
		
		return resultado;
		
	}

	/**
	 * Obtiene una fecha compatible Java
	 * @param pubmedPubDate
	 * @return la fecha
	 */
	private ZonedDateTime makeDate(PubMedPubDate pubmedPubDate) {
		
		if (pubmedPubDate == null) return null;
		
		Calendar test = Calendar.getInstance();
		test.add(Calendar.YEAR, 1);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(test.getTime());
		
		if (pubmedPubDate.getSecond() != null)	cal.set(Calendar.SECOND,		Integer.parseInt(pubmedPubDate.getSecond()));
		if (pubmedPubDate.getMinute() != null)	cal.set(Calendar.MINUTE,		Integer.parseInt(pubmedPubDate.getMinute()));
		if (pubmedPubDate.getHour() != null)	cal.set(Calendar.HOUR,			Integer.parseInt(pubmedPubDate.getHour()));
		if (pubmedPubDate.getDay() != null)		cal.set(Calendar.DAY_OF_MONTH,	Integer.parseInt(pubmedPubDate.getDay().getvalue()));
		if (pubmedPubDate.getMonth() != null)	cal.set(Calendar.MONTH,			Integer.parseInt(pubmedPubDate.getMonth().getvalue()));
		if (pubmedPubDate.getYear() != null)	cal.set(Calendar.YEAR,			Integer.parseInt(pubmedPubDate.getYear().getvalue()));
		
		ZonedDateTime resultado = null;
		if (test.compareTo(cal) > 0) {
			resultado = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.of(DATE_EUR_MADRID));
		}
		return resultado;
	}

	/**
	 * Construir los datos de la revista
	 * @param journal
	 * @return
	 */
	private Revista makeRevista(Journal journal) {
		
		if (	(journal == null) || (	
				(StringUtils.isBlank(journal.getTitle())) &&
				(StringUtils.isBlank(journal.getISOAbbreviation())) &&
				(journal.getISSN() != null) )) return null;

		Revista revista = new Revista();
		revista.setTipo(			Articulo.REVISTA);
		revista.setNombre(			journal.getTitle());
		revista.setAbreviatura(		journal.getISOAbbreviation());
		revista.setMedio(			(journal.getISSN() != null) ? journal.getISSN().getIssnType() : null);
		revista.addId(				makeId(journal.getISSN()));
		
		return revista;
	
	}

	/**
	 * Construye los datos de la revista
	 * @param medlineJournalInfo
	 * @return
	 */
	private Revista makeRevista(MedlineJournalInfo medlineJournalInfo) {
		
		if (	(medlineJournalInfo == null) || (
				(StringUtils.isBlank(medlineJournalInfo.getMedlineTA())) &&
				(StringUtils.isBlank(medlineJournalInfo.getCountry())) &&
				(StringUtils.isBlank(medlineJournalInfo.getISSNLinking())) &&
				(StringUtils.isBlank(medlineJournalInfo.getNlmUniqueID())) )) return null;

		Revista revista = new Revista();
		revista.setTipo(		Articulo.REVISTA);
		revista.setAbreviatura(	medlineJournalInfo.getMedlineTA());
		revista.setPais(		medlineJournalInfo.getCountry());
		revista.addId(			makeId(Articulo.ISSN_ID_NAME, medlineJournalInfo.getISSNLinking()));
		revista.addId(			makeId(Articulo.NLM_ID_NAME, medlineJournalInfo.getNlmUniqueID()));

		return revista;
		
	}
	
	/**
	 * Construir los datos del fasciculo
	 * @param journal
	 * @return
	 */
	private Fasciculo makeFasciculo(Journal journal) {
		
		if (	(journal == null) || 
				(journal.getJournalIssue() == null) ) return null;
	
		return makeFasciculo(journal.getJournalIssue());
	
	}

	/**
	 * Construye los datos del fascículo
	 * @param journalIssue
	 * @return
	 */
	public Fasciculo makeFasciculo(JournalIssue journalIssue) {

		if (journalIssue == null) return null;
		
		Fasciculo fasciculo = new Fasciculo();
		fasciculo.setMedio(		journalIssue.getCitedMedium());
		fasciculo.setVolumen(	journalIssue.getVolume());
		fasciculo.setNumero(	journalIssue.getIssue());
		fasciculo.setFecha( 	makeFecha(journalIssue.getPubDate()));
		
		return fasciculo;
		
	}

	/**
	 * Construye los datos del fascículo
	 * @param medlineJournalInfo
	 * @return
	 */
	private Fasciculo makeFasciculo(MedlineJournalInfo medlineJournalInfo) {
		
		if (medlineJournalInfo == null) return null;

		Fasciculo fasciculo = new Fasciculo();
		
		medlineJournalInfo.getCountry();
		medlineJournalInfo.getISSNLinking();
		medlineJournalInfo.getMedlineTA();
		medlineJournalInfo.getNlmUniqueID();
		
		/*
		fasciculo.setMedio(		medlineJournalInfo.getCitedMedium());
		fasciculo.setVolumen(	medlineJournalInfo.getVolume());
		fasciculo.setNumero(	medlineJournalInfo.getIssue());
		fasciculo.setFecha( 	makeFecha(medlineJournalInfo.getPubDate()));
		*/
		return fasciculo;

	}

	/**
	 * Conjuntos de datos utilizados por el artículo
	 * @param dataBankList
	 * @return
	 */
	private Map<String, String> makeData(DataBankList dataBankList) {

		if (	(dataBankList == null) ||
				(dataBankList.getDataBank() == null) ||
				(dataBankList.getDataBank().isEmpty()) ||
				(NO.contentEquals(dataBankList.getCompleteYN()))) return null;
		
		Map<String, String> resultado = dataBankList.getDataBank().stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getDataBankName())) &&
							(p.getAccessionNumberList() != null) ||
							(p.getAccessionNumberList().getAccessionNumber() != null) ||
							(!p.getAccessionNumberList().getAccessionNumber().isEmpty()) ).
			map(instance -> 	{
				StringBuffer sb = new StringBuffer();
				for (AccessionNumber number: instance.getAccessionNumberList().getAccessionNumber()) {
					if (sb.length()>0) sb.append(Articulo.SEPARATOR);
					sb.append(number.getvalue());
				}
				return new SimpleEntry<String, String>(
						instance.getDataBankName(),
						sb.toString());
			}).
			collect(Collectors.toMap(
					p -> p.getKey(), 
					p -> p.getValue(),
					(o1, o2) -> o1 + ", " + o2 ));
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}

	/**
	 * Permisos de reproducción (pais, agencia y permiso
	 * @param grantList
	 * @return
	 */
	private List<Permiso> makePermisos(GrantList grantList) {

		if (	(grantList == null) ||
				(grantList.getGrant() == null) ||
				(grantList.getGrant().isEmpty()) ||
				("N".contentEquals(grantList.getCompleteYN()))) return null;
		
		List<Permiso> resultado = grantList.getGrant().stream().
			filter(p ->			(p != null) ).
			map(instance -> 	{
				return new Permiso(
						instance.getCountry(),
						instance.getAgency(),
						instance.getGrantID(),
						instance.getAcronym());
			}).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}

	/**
	 * Obtiene la lista de objetos que extienden el artículo
	 * @param objects
	 * @return
	 */
	private Map<String, Map<String, String>> makePropiedades(ObjectList objects) {
		
		if (	(objects == null) || 
				(objects.getObject() == null) ||
				(objects.getObject().isEmpty())) return null;

		Map<String, Map<String, String>> resultado = objects.getObject().stream().
			filter(p ->		(p != null) &&
							(p.getType() != null) &&
							(p.getParam() != null) &&
							(!p.getParam().isEmpty()) ).
			map(instance -> {

				String tipo = instance.getType();
				Map<String, String> parametros = instance.getParam().stream().
					filter(p ->		(p != null) &&
									(StringUtils.isNotBlank(p.getName())) &&
									(StringUtils.isNotBlank(p.getvalue())) ).
					map(param ->	{
						return new SimpleEntry<String, String>(
									param.getName(),
									param.getvalue());
					}).
					collect(Collectors.toMap(
							p -> p.getKey(), 
							p -> p.getValue(),
							(o1, o2) -> o1 + ", " + o2 ));
					
				return new SimpleEntry<String, Map<String, String>>(
						tipo, 
						parametros);
				
			}).
			collect(Collectors.toMap(
					p -> p.getKey(), 
					p -> p.getValue(),
					(o1, o2) -> {
						o1.putAll(o2);
						return o1;
					}));
						
		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}
	

	private Map<String, List<String>> makePropiedades(List<ItemList> itemList) {

		if (	(itemList == null) || 
				(itemList.isEmpty())) return null;

		Map<String, List<String>> resultado = itemList.stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getListType())) &&
							(p.getItem() != null) &&
							(!p.getItem().isEmpty()) ).
			map(instance -> {

				String tipo = instance.getListType();
				List<String> parametros = instance.getItem().stream().
					filter(p ->		(p != null) &&
									(StringUtils.isNotBlank(p.getvalue())) ).
					map(param ->	{
						return param.getvalue();
					}).
					collect(Collectors.toList());
					
				return new SimpleEntry<String, List<String>>(
						tipo, 
						parametros);
				
			}).
			collect(Collectors.toMap(
					p -> p.getKey(), 
					p -> p.getValue(),
					(o1, o2) -> {o1.addAll(o2); return o1;} ));
						
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}
	
	/**
	 * Observaciones de diversas fuentas
	 * @param generalNote
	 * @return
	 */
	// TODO	private Map<String, String> makeObservacionesMap(List<GeneralNote> generalNote) {
	public Map<String, String> makeObservacionesMap(List<GeneralNote> generalNote) {
		
		if (	(generalNote == null) || 
				(generalNote.isEmpty())) return null;
		
		Map<String, String> resultado = generalNote.stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> {
					return new SimpleEntry<String, String>(
							instance.getOwner(),
							instance.getvalue());
			}).
			collect(Collectors.toMap(
					p -> p.getKey(), 
					p -> p.getValue(),
					(o1, o2) -> o1 + ", " + o2 ));
						
		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}
	
	/**
	 * Observaciones de diversas fuentas
	 * @param generalNote
	 * @return
	 */
	private String makeObservaciones(List<GeneralNote> generalNote) {
		
		if (	(generalNote == null) || 
				(generalNote.isEmpty())) return null;
		
		StringBuffer sb = new StringBuffer();
		generalNote.forEach(p -> {
			if (StringUtils.isNotBlank(p.getvalue())) sb.append(String.format("%s:\t%s\r\n", p.getOwner(), p.getvalue()));
		});

		String resultado = sb.toString();
		if (StringUtils.isBlank(resultado)) resultado = null;
		return resultado;
		
	}

	/**
	 * Obtiene la lista de citas al articulo
	 * @param referenceList
	 * @return
	 */
	private List<Referencia> makeReferencias(List<ReferenceList> referenceList) {

		if (	(referenceList == null) || 
				(referenceList.isEmpty())) return null;

		List<Referencia> resultado = referenceList.stream().
			filter(p ->		(p != null) &&
							(p.getReference() != null) &&
							(!p.getReference().isEmpty()) ).
			flatMap(list -> {
				if (StringUtils.isNotBlank(list.getTitle())) {
					System.out.println ("GET_REFERENCE_TITLE: " + list.getTitle());
				}
				return list.getReference().stream();} ).
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getCitation())) &&
							(p.getArticleIdList() != null) &&
							(p.getArticleIdList().getArticleId() != null) ||
							(!p.getArticleIdList().getArticleId().isEmpty()) ). 
			map(instance -> {
				
				Map<String, String> refIds = instance.getArticleIdList().getArticleId().stream().
					filter(p -> (p != null) &&
								(StringUtils.isNotBlank(p.getIdType())) &&
								(StringUtils.isNotBlank(p.getvalue())) ).
					map(id -> {
						return new SimpleEntry<String, String>(
									id.getIdType(),
									id.getvalue());
					}).
					collect(Collectors.toMap(
							p -> p.getKey(), 
							p -> p.getValue(),
							(o1, o2) -> o1 + ", " + o2 ));
				
				Referencia ref = new Referencia();
				ref.setCita(instance.getCitation());
				ref.setIds(refIds);
				return ref;
				
			}).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	/**
	 * Lista de terminos del tesauros. Son descriptores y cualificadores MESH
	 * @param meshHeadingList
	 * @return
	 */
	private List<Termino> makeTerminos(MeshHeadingList meshHeadingList) {

		if (	(meshHeadingList == null) || 
				(meshHeadingList.getMeshHeading() == null) ||
				(meshHeadingList.getMeshHeading().isEmpty())) return null;

		List<Termino> resultado = meshHeadingList.getMeshHeading().stream().
			filter(p ->		(p != null) &&
							(p.getDescriptorName() != null) &&
							(StringUtils.isNotBlank(p.getDescriptorName().getUI())) &&
							(StringUtils.isNotBlank(p.getDescriptorName().getvalue())) &&
							(p.getQualifierName() != null) &&
							(!p.getQualifierName().isEmpty())).
			map(instance -> {
				Map<String, String> items = instance.getQualifierName().stream().
					filter(p ->	(p != null)  &&
								(StringUtils.isNotBlank(p.getUI())) &&
								(StringUtils.isNotBlank(p.getvalue())) ).
					map(item -> {
						return new SimpleEntry<String, String>(
								item.getUI(),
								item.getvalue());
					}).
					collect(Collectors.toMap(
							p -> p.getKey(), 
							p -> p.getValue(),
							(o1, o2) -> o1 + ", " + o2 ));
				return new Termino(
						instance.getDescriptorName().getUI(),
						instance.getDescriptorName().getvalue(),
						items);			
			}).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	/**
	 * Obtiene los tipos (UI, value). Son Descriptores de MESH
	 * @param chemicalList
	 * @return
	 */
	private List<Termino> makeTerminos(ChemicalList chemicalList) {
		
		if (	(chemicalList == null) || 
				(chemicalList.getChemical() == null) ||
				(chemicalList.getChemical().isEmpty())) return null;

		List<Termino> resultado = chemicalList.getChemical().stream().
			filter(p ->		(p != null) &&
							(p.getNameOfSubstance() != null) &&
							(StringUtils.isNotBlank(p.getNameOfSubstance().getUI())) &&
							(StringUtils.isNotBlank(p.getNameOfSubstance().getvalue()))).
			map(instance -> {
				return new Termino(
						Termino.FARMACO,
						instance.getNameOfSubstance().getUI(),
						instance.getNameOfSubstance().getvalue());			
			}).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	/**
	 * Establece términos de clase 2, 3 y 4 protocolos, dolencias y organismos
	 * @param supplMeshList
	 * @return
	 */
	private List<Termino> makeTerminos(SupplMeshList supplMeshList) {
		
		if (	(supplMeshList == null) || 
				(supplMeshList.getSupplMeshName() == null) ||
				(supplMeshList.getSupplMeshName().isEmpty())) return null;

		List<Termino> resultado = supplMeshList.getSupplMeshName().stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getUI())) &&
							(StringUtils.isNotBlank(p.getvalue()))).
			map(instance -> {
				return new Termino(
						instance.getType(),
						instance.getUI(),
						instance.getvalue());			
			}).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	/**
	 * Obtiene los tipos (UI, value). Son Descriptores de MESH
	 * @param publicationTypeList
	 * @return
	 */
	private List<Termino> makeTerminos(PublicationTypeList publicationTypeList) {

		if (	(publicationTypeList == null) ||
				(publicationTypeList.getPublicationType() == null) ||
				(publicationTypeList.getPublicationType().isEmpty())) return null;
		
		return makeTerminos(publicationTypeList.getPublicationType());
		
	}

	/**
	 * Obtiene los tipos (UI, value). Son Descriptores de MESH
	 * @param publicationTypeList
	 * @return
	 */
	private List<Termino> makeTerminos(List<PublicationType> publicationType) {

		if (	(publicationType == null) ||
				(publicationType.isEmpty())) return null;
		
		List<Termino> resultado = publicationType.stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getUI())) &&
							(StringUtils.isNotBlank(p.getvalue()))).
			map(instance -> {
				return new Termino(
						instance.getUI(),
						instance.getvalue());			
			}).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	/**
	 * Lista de descriptoes a partir de listas de descriptores por procedencia
	 * NLM | NLM-AUTO | NASA | PIP | KIE | NOTNLM | HHS) "NLM"
	 * @param keywordList
	 * @return
	 */
	private List<Descriptor> makeDescriptores(List<KeywordList> keywordList) {

		if (	(keywordList == null)  ||
				(keywordList.isEmpty())) {
			return null; 
		}
		
		List<Descriptor> resultado = keywordList.stream().
			filter(p -> (p != null) &&
						(p.getKeyword() != null) &&
						(!p.getKeyword().isEmpty())).
			flatMap(list -> makeDescriptores(list.getOwner(), list.getKeyword()).stream()).
			collect(Collectors.toList());

		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	private List<Descriptor> makeDescriptores(String owner, List<Keyword> keyword) {

		if (	(keyword == null) || 
				(keyword.isEmpty())) return null;
		
		List<Descriptor> resultado = keyword.stream().
			filter(p ->		(p != null) ).
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> {
				return new Descriptor(owner, instance.getvalue());
			}).
			collect(Collectors.toList());

		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	/**
	 * Prepara una lista de farmacos según los modelos
	 * Chemical Abstracts Service, Enzyme Nomenclature, or Food and Drug Administration's Unique Ingredient Identifiers
	 * corresponds to Class 1 (chemical and drug) 
	 * @param chemicalList
	 * @return
	 */
	private List<String> makeFarmacos(ChemicalList chemicalList) {

		if (	(chemicalList == null) ||
				(chemicalList.getChemical() == null) ||
				(chemicalList.getChemical().isEmpty())) return null;
		
		List <String> resultado = chemicalList.getChemical().stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getRegistryNumber())) ).
			map(instance -> {
				return instance.getRegistryNumber();
			}).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	/**
	 * Prepara una lista de genes
	 * @param geneSymbolList
	 * @return
	 */
	private List<String> makeGenes(GeneSymbolList geneSymbolList) {

		if (	(geneSymbolList == null) || 
				(geneSymbolList.getGeneSymbol() == null) || 
				(geneSymbolList.getGeneSymbol().isEmpty())) return null;
		
		List<String> resultado = geneSymbolList.getGeneSymbol().stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> {
				return instance.getvalue();
			}).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

}
