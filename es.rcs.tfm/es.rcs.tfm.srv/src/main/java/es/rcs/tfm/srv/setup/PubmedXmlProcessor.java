package es.rcs.tfm.srv.setup;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;

import org.apache.commons.io.FilenameUtils;
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
import org.ncbi.pubmed.SpaceFlightMission;
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
import es.rcs.tfm.srv.model.Fichero;
import es.rcs.tfm.srv.model.Libro;
import es.rcs.tfm.srv.model.Localizacion;
import es.rcs.tfm.srv.model.Permiso;
import es.rcs.tfm.srv.model.Referencia;
import es.rcs.tfm.srv.model.Revista;
import es.rcs.tfm.srv.model.Termino;
import es.rcs.tfm.srv.model.Termino.DescType;
import es.rcs.tfm.srv.model.Termino.TermType;
import es.rcs.tfm.srv.model.Texto;
import es.rcs.tfm.srv.model.Titulo;

public class PubmedXmlProcessor extends ArticleProcessor {

	private static final String YES 					= "Y";
	private static final String NO						= "N";

	private static final String DATE_FMT_STR			= "yyyy/MM/dd";
	private static final DateTimeFormatter DATE_FMT		= DateTimeFormatter.ofPattern(DATE_FMT_STR);
	//private static final String DATE_SIMPLE_FMT			= "%s-%s-%s";
	//private static final String DATE_EUR_MADRID			= "Europe/Madrid";

	
	//private List<PMID> deletedPmidToIterate = null;
	//private boolean deletedSuccess = false;

	private List<?> items = null;
	private boolean allOk = false;
	private int index = 0;
	private Fichero fichero = null;
	public int getItemsSize() {
		return (items != null) ? items.size() : 0;
	}

	private static final int getMonthFrom(String monthName) {

		if (StringUtils.isBlank(monthName)) return -1;
		
		DateFormatSymbols dfs = new DateFormatSymbols(Locale.ENGLISH);
		String[] months = null;
		
		months = dfs.getShortMonths();
		for (int i = 0; i < 12; i++) {
			if (months[i].equalsIgnoreCase(monthName)) {
				return i + 1; // month index is zero-based as usual in old JDK pre 8!
			}
		}
		
		months = dfs.getMonths();
		for (int i = 0; i < 12; i++) {
			if (months[i].equalsIgnoreCase(monthName)) {
				return i + 1; // month index is zero-based as usual in old JDK pre 8!
			}
		}
		
		return java.time.Month.valueOf(monthName.toUpperCase()).getValue();
		
	}


	public PubmedXmlProcessor(Fichero fichero, String directory) {
		
		if ((fichero == null) || (StringUtils.isBlank(directory))) return;
		
		Path path = Paths.get(
				FilenameUtils.concat(
						directory, 
						fichero.getUncompressFichero()));
		
		if (!path.toFile().exists()) return;
		
		this.fichero = fichero;
			
        SAXSource source = ArticleProcessor.getSourceFromPath(path);
        if (source != null) {
			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(PubmedArticleSet.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				Schema schema = null;
				jaxbUnmarshaller.setSchema(schema);
			    Object articleSet = jaxbUnmarshaller.unmarshal(source);
			    if ((articleSet != null) && (articleSet instanceof PubmedArticleSet)) {
			    	PubmedArticleSet pubmedArticleSet = (PubmedArticleSet)articleSet;
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
		if (items == null) return false;
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
		result.setXml(item);
		return result;

	}

	/**
	 * Obtiene un art�culo a partir de un art�culo de una revista
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
		articulo.setFicheroPubmed(this.fichero);
		
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
		articulo.setPropietario(	medlineCitation.getOwner());
		articulo.setEstado(			medlineCitation.getStatus());
		articulo.setVersion(		medlineCitation.getVersionID());
		articulo.setVersionFecha(	makeDate(medlineCitation.getVersionDate()));
		articulo.addIds(			makeId(medlineCitation.getPMID()));
		
		// Generalmente afectan a ampliaciones como el abstract
		articulo.addIds(			makeIdsOther(medlineCitation.getOtherID())); 
		articulo.addTextos(			makeTextos(medlineCitation.getOtherAbstract()));

		articulo.addFecha(			makeFecha(medlineCitation.getDateCompleted()));
		articulo.addFecha(			makeFecha(medlineCitation.getDateRevised()));

		articulo.addAutores(		makeAutores(medlineCitation.getPersonalNameSubjectList()));
		articulo.addAutores(		makeAutores(medlineCitation.getInvestigatorList()));
		
		// Class 1 (chemical and drug)
		//articulo.addFarmacos(		makeFarmacos(medlineCitation.getChemicalList())); 
		articulo.addTerminos(		makeTerminos(medlineCitation.getChemicalList())); 
		// Class 2 (protocol) y Class 3 (disease) y Class 4 (organism)
		articulo.addTerminos(		makeTerminos(medlineCitation.getSupplMeshList())); 
		articulo.addTerminos(		makeTerminos(medlineCitation.getMeshHeadingList()));

		articulo.addKeywords(		makeDescriptores(medlineCitation.getKeywordList()));
		articulo.addGenes(			makeGenes(medlineCitation.getGeneSymbolList())); // Solo en citas desde 1991 a 1995	
		articulo.addVuelos(			makeData(medlineCitation.getSpaceFlightMission()));
		articulo.addNotas(			makeNotes(medlineCitation.getGeneralNote()));
		articulo.addTextos(			makeTextosNotas(medlineCitation.getGeneralNote()));
		
		articulo.mergeRevista(		makeRevista(medlineCitation.getMedlineJournalInfo()));
		
		// TODO
		medlineCitation.getNumberOfReferences(); // No utilizado desde 2010
		// TODO
		medlineCitation.getIndexingMethod(); // Curated vs Automated. No usado en el proyecto
		// TODO
		medlineCitation.getCitationSubset(); // Citas procedentes del modelo de antiguo
		// TODO
		medlineCitation.getCoiStatement(); // conflict of interest
		// TODO
		medlineCitation.getCommentsCorrectionsList(); // Correciones invocadas por otros art�culos
		
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
		articulo.addTexto(			makeTexto(article.getArticleTitle()));
		articulo.addTexto(			makeTexto(
										Articulo.VERNACULAR_TITLE,
										Articulo.VERNACULAR_TITLE_START, 
										article.getVernacularTitle()));
		articulo.addTextos(			makeTextos(article.getAbstract()));
		articulo.setIdioma(			makeIdioma(article.getLanguage()));
		articulo.setMedio(			article.getPubModel()); 
		//articulo.addIds(			makeIdsELocationIDS(article.getPaginationOrELocationID()) );
		articulo.addFechas(			makeFechas(article.getArticleDate()));
		articulo.addAutores(		makeAutores(article.getAuthorList()));
		articulo.addLocalizaciones(	makeLocalizacion(article.getPaginationOrELocationID()));
		articulo.addPermisos(		makePermisos(article.getGrantList()));
		articulo.addDatos(			makeData(article.getDataBankList()));
		articulo.addTerminos(		makeTerminos(article.getPublicationTypeList()));

		articulo.mergeRevista(		makeRevista(article.getJournal()));
		articulo.mergeFasciculo(	makeFasciculo(article.getJournal()));

		return articulo;

	}

	private Articulo getArticuloInfo(Articulo articulo, BookDocument bookDocument) {

		if (bookDocument == null) return articulo;

		if (articulo == null) articulo = new Articulo();
		articulo.setTitulo(			makeTitulo(bookDocument.getArticleTitle()));
		articulo.addTexto(			makeTexto(bookDocument.getArticleTitle()));
		articulo.addTexto(			makeTexto(
										Articulo.VERNACULAR_TITLE, 
										Articulo.VERNACULAR_TITLE_START, 
										bookDocument.getVernacularTitle()));		
		articulo.addTextos(			makeTextos(bookDocument.getAbstract()));
		articulo.setIdioma(			makeIdioma(bookDocument.getLanguage()));
		articulo.addIds(			makeIds(bookDocument.getArticleIdList()));
		articulo.addIds(			makeId(bookDocument.getPMID()));
		articulo.addFecha(			makeFecha(bookDocument.getDateRevised()));
		articulo.addFecha(			makeFecha(bookDocument.getContributionDate()));
		articulo.addAutores(		makeAutores(bookDocument.getAuthorList()));
		articulo.addAutores(		makeAutores(bookDocument.getInvestigatorList()));
		articulo.setLibro(			makeLibro(bookDocument.getBook()));
		articulo.addPermisos(		makePermisos(bookDocument.getGrantList()));
		articulo.addReferencias(	makeReferencias(bookDocument.getReferenceList()));
		articulo.addTerminos(		makeTerminos(bookDocument.getPublicationType()));
		articulo.addLocalizacion(	makeLocalizacion(bookDocument.getPagination()) );
		articulo.addKeywords(		makeDescriptores(bookDocument.getKeywordList()));
		articulo.addItems(			makeItems(bookDocument.getItemList()));
		
		/*
		// TODO (part | chapter | section | appendix | figure | table | box)
		bookDocument.getLocationLabel(); // NO UTILIZADO EN EL PROYECTO
		//TODO 
		articulo.addSecciones(		makeSecciones(bookDocument.getSections()));
		 */
		
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
		articulo.addPropiedades(	makePropiedades(pubmedData.getObjectList()));

		return articulo;
		
	}

	private Articulo getArticuloData(Articulo articulo, PubmedBookData pubmedBookData) {

		if (pubmedBookData == null) return articulo;

		if (articulo == null) articulo = new Articulo();
		articulo.setEstado(			pubmedBookData.getPublicationStatus());
		articulo.addIds(			makeIds(pubmedBookData.getArticleIdList()));
		articulo.addFechas(			makeFechas(pubmedBookData.getHistory()));
		articulo.addPropiedades(	makePropiedades(pubmedBookData.getObjectList()));

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
		libro.addAutores(			makeAutores(book.getInvestigatorList()));

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
		
		List<Seccion> resultado = sections.getSection().
			stream().
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
		
		List<Seccion> resultado = section.
			stream().
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

		Titulo resultado = new Titulo(articleTitle.getvalue().trim());
		if (StringUtils.isNotBlank(articleTitle.getBook())) {
			resultado.setLibroId(articleTitle.getBook().trim());
		}
		if (StringUtils.isNotBlank(articleTitle.getPart())) {
			resultado.setLibroId(articleTitle.getPart().trim());
		}
		if (StringUtils.isNotBlank(articleTitle.getSec())) {
			resultado.setLibroId(articleTitle.getSec().trim());
		}
		
		if (StringUtils.isBlank(resultado.getTitulo())) {
			System.out.println("DEBUG - titulo vacio");
		}

		return resultado;
		
	}

	private Titulo makeTitulo(BookTitle bookTitle) {

		if (	(bookTitle == null) ||
				(StringUtils.isBlank(bookTitle.getvalue()))) return null;

		Titulo resultado = new Titulo(bookTitle.getvalue().trim());
		if (StringUtils.isNotBlank(bookTitle.getBook())) {
			resultado.setLibroId(bookTitle.getBook().trim());
		}
		if (StringUtils.isNotBlank(bookTitle.getPart())) {
			resultado.setLibroId(bookTitle.getPart().trim());
		}
		if (StringUtils.isNotBlank(bookTitle.getSec())) {
			resultado.setLibroId(bookTitle.getSec().trim());
		}
		
		return resultado;

	}

	private Titulo makeTitulo(CollectionTitle collectionTitle) {

		if (	(collectionTitle == null) ||
				(StringUtils.isBlank(collectionTitle.getvalue()))) return null;

		Titulo resultado = new Titulo(collectionTitle.getvalue().trim());
		if (StringUtils.isNotBlank(collectionTitle.getBook())) {
			resultado.setLibroId(collectionTitle.getBook().trim());
		}
		if (StringUtils.isNotBlank(collectionTitle.getPart())) {
			resultado.setLibroId(collectionTitle.getPart().trim());
		}
		if (StringUtils.isNotBlank(collectionTitle.getSec())) {
			resultado.setLibroId(collectionTitle.getSec().trim());
		}
		
		return resultado;

	}

	// -------------------------------------------------------------------------------
	// RESUMEN

	private List<Texto> makeTextosNotas(List<GeneralNote> generalNote) {
		
		if (	(generalNote == null) ||
				(generalNote.isEmpty())) return null;

		String type = Articulo.OBSERVATIONS;
		int order = Articulo.OBSERVATIONS_START;
		
		AtomicInteger atomicorder = new AtomicInteger(order);
		List<Texto> result = generalNote.
				stream().
				filter(	p -> p!= null).
				map(p -> makeTexto(type, atomicorder.getAndAdd(1), p.getvalue())).
				collect(Collectors.toList());
		
		if (result.isEmpty()) result = null;
		return result;

	}

	private List<Texto> makeTextos(Abstract articleAbstract) {
		
		if (articleAbstract == null) return null;

		String type = Articulo.ABSTRACT;
		int order = Articulo.ABSTRACT_START;
		String subtype = null;
		String language = "en";
		String copyright = articleAbstract.getCopyrightInformation();
		
		List<Texto> result = makeTextos(type, subtype, language, copyright, order, articleAbstract.getAbstractText());
		
		if (result.isEmpty()) result = null;
		return result;
		
	}

	private List<Texto> makeTextos(List<OtherAbstract> otherAbstract) {

		if (	(otherAbstract == null) ||
				(otherAbstract.isEmpty())) return null;
		
		String type = Articulo.OTHER_ABSTRACT;
		int order = Articulo.OTHER_ABSTRACT_START;
		List<Texto> result = otherAbstract.
			stream().
			filter(	p -> p!= null &&
					p.getAbstractText() != null && 
					!p.getAbstractText().isEmpty()).
			flatMap(p -> {
				String subtype = p.getType();
				String language = p.getLanguage();
				String copyright = p.getCopyrightInformation();
				return makeTextos(type, subtype, language, copyright, order, p.getAbstractText()).stream();
			}).
			collect(Collectors.toList());

		if (result.isEmpty()) result = null;
		return result;

	}
	
	private List<Texto> makeTextos(
			String type,
			String subtype,
			String language,
			String copyright,
			int order,
			List<AbstractText> abstractText) {
		
		if (	(abstractText == null) ||
				(abstractText.isEmpty())) return null;

		AtomicInteger atomicorder = new AtomicInteger(order);
		List<Texto> result = abstractText.
			stream().
			filter(p -> p!= null).
			map(p -> new Texto(
						type, 
						subtype, 
						language, 
						copyright, 
						atomicorder.getAndAdd(1), 
						p.getLabel(),
						p.getNlmCategory(),
						p.getNlmCategory())).
			collect(Collectors.toList());

		if (result.isEmpty()) result = null;
		return result;

	}

	private Texto makeTexto(ArticleTitle articleTitle) {
		
		if (	(articleTitle == null) ||
				(StringUtils.isBlank(articleTitle.getvalue()))) return null;
	
		String type = Articulo.TITLE;
		int order = Articulo.TITLE_START;
		String subtype = null;
		String language = null;
		String copyright = null;
		String label = null;
		String category = null;
		String text = articleTitle.getvalue().trim();

		return new Texto(
				type, 
				subtype, 
				language, 
				copyright, 
				order, 
				label, 
				category, 
				text);
		/*
		if (StringUtils.isNotBlank(articleTitle.getBook())) {
			resultado.setLibroId(articleTitle.getBook().trim());
		}
		if (StringUtils.isNotBlank(articleTitle.getPart())) {
			resultado.setLibroId(articleTitle.getPart().trim());
		}
		if (StringUtils.isNotBlank(articleTitle.getSec())) {
			resultado.setLibroId(articleTitle.getSec().trim());
		}
		
		if (StringUtils.isBlank(resultado.getTitulo())) {
			System.out.println("DEBUG - titulo vacio");
		}
		*/
		
	}
	
	private Texto makeTexto(
			String type,
			int order,
			String text) {
		return new Texto(
				type,
				null,
				null,
				null,
				order,
				null,
				null,
				text);
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
		
		List<Entry<String, String>> resultado = articleIdList.getArticleId().
			stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getIdType())) &&
							(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance ->	{
				String type = null;
		    	if ((type == null) && (IS_NUMERIC.matcher(instance.getIdType()).matches())) type = Articulo.PUBMED_ID_NAME;
		    	if ((type == null) && ("pubmed".equals(instance.getIdType()))) type = Articulo.PUBMED_ID_NAME;
		    	if ((type == null) && ("rspb".indexOf(instance.getIdType()) >= 0)) type = Articulo.DOI_ID_NAME;
		    	if (type == null) type = instance.getIdType();
				return new SimpleEntry<String, String>(
							type,
							instance.getvalue());
			}).
			filter(p -> 	(p != null)).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	private Map<String, String> makeIds(List<Identifier> identifiers) {

		if (	(identifiers == null)  ||
				(identifiers.isEmpty())) {
			return null; 
		}

		Map<String, String> resultado = identifiers.
			stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getSource())) &&
							(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> 	{
				return new SimpleEntry<String, String>(
						instance.getSource(),
						instance.getvalue());
			}).
			filter(p -> 	(p != null)).
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
		
		Map<String, String> resultado = eLocationIds.
			stream().
			filter(p -> (p != null) &&
						(YES.equals(p.getValidYN())) && 
						(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> new SimpleEntry<String, String>(
					instance.getEIdType(), // doi | pii
					instance.getvalue())).
			filter(p -> 	(p != null)).
			collect(Collectors.toMap(	
					p -> p.getKey(), 
					p -> p.getValue(),
					(o1, o2) -> o1 + ", " + o2 ));

		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}

	protected Map<String, String> makeIdsELocationIDS(List<Object> paginationOrELocationID) {
		
		if (	(paginationOrELocationID == null) ||
				(paginationOrELocationID.isEmpty()) ) return null;
		
		Map<String, String> resultado = paginationOrELocationID.
			stream().
			filter(p -> (p != null) &&
						(p instanceof ELocationID) ).
			map(instance -> 
					(ELocationID)instance).
			filter(p ->	(YES.equals(p.getValidYN())) && 
						(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> new SimpleEntry<String, String>(
					instance.getEIdType(), // doi | pii
					instance.getvalue())).
			filter(p -> 	(p != null)).
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
		
		Map<String, String> resultado = isbn.
			stream().
			filter(p ->	(p != null) &&
						(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> 	{
				return new SimpleEntry<String, String>(
						Articulo.ISBN_ID_NAME,
						instance.getvalue());
			}).
			filter(p -> (p != null)).
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

		Map<String, String> resultado = otherIds.
			stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getSource())) &&
							(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> 	{
				return new SimpleEntry<String, String>(
						instance.getSource(), // NASA | KIE | PIP | POP | ARPL | CPC | IND | CPFH | CLML | NRCBL | NLM | QCIM
						instance.getvalue());
			}).
			filter(p -> 	(p != null)).
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
	 * @param date
	 * @return
	 */
	private Fecha makeFecha(BeginningDate date) {
		
		if (date == null) return null;
		
		String dia = null;
		String mes = null;
		String anio = null;
		String ses = null;
		
		if (	(date.getMonthOrDayOrSeason() != null) && 
				(!date.getMonthOrDayOrSeason().isEmpty()) ){
			for (Object p: date.getMonthOrDayOrSeason()) {
				if (p instanceof Month)		mes = ((Month)p).getvalue();
				if (p instanceof Day) 		dia = ((Day)p).getvalue();
				if (p instanceof Season)	ses = ((Season)p).getvalue();
			}
			
		}

		int year = 1;
		if (	(date.getYear() != null) &&
				(StringUtils.isNotBlank(date.getYear().getvalue())) ) {
			anio = date.getYear().getvalue();
			try { year = Integer.parseInt(anio); } catch (Exception ex) {}
		}

		int month = 1;
		try { 
			if (StringUtils.isNotBlank(mes)) month = Integer.parseInt(mes); 
		} catch (Exception ex) {
			month = getMonthFrom(mes);
		}
		
		int dayOfMonth = 1;
		try { if (StringUtils.isNotBlank(dia)) dayOfMonth = Integer.parseInt(dia); } catch (Exception ex) {}
		
		Fecha resultado = null;
		LocalDate localdate = LocalDate.of(year, month, dayOfMonth);
		if (localdate != null) {
			resultado = new Fecha(Articulo.FECHA_INICIO, localdate);
		}
		
		if (StringUtils.isNotBlank(ses)) {
			if (resultado == null) {
				resultado = new Fecha(Articulo.FECHA_INICIO);
				resultado.setAnio(anio);
			}
			resultado.setSesion(ses);
		}
			
		return resultado;

	}

	/**
	 * Obtiene la fecha de contribucion
	 * @param date
	 * @return
	 */
	private Fecha makeFecha(ContributionDate date) {
		
		if (date == null) return null;
		
		String dia = null;
		String mes = null;
		String anio = null;
		String ses = null;
		
		if (	(date.getMonthOrDayOrSeason() != null) && 
				(!date.getMonthOrDayOrSeason().isEmpty()) ){
			for (Object p: date.getMonthOrDayOrSeason()) {
				if (p instanceof Month)		mes = ((Month)p).getvalue();
				if (p instanceof Day) 		dia = ((Day)p).getvalue();
				if (p instanceof Season)	ses = ((Season)p).getvalue();
			}
			
		}

		int year = 1;
		if (	(date.getYear() != null) &&
				(StringUtils.isNotBlank(date.getYear().getvalue())) ) {
			anio = date.getYear().getvalue();
			try { year = Integer.parseInt(anio); } catch (Exception ex) {}
		}

		int month = 1;
		try { 
			if (StringUtils.isNotBlank(mes)) month = Integer.parseInt(mes); 
		} catch (Exception ex) {
			month = getMonthFrom(mes);
		}
		
		int dayOfMonth = 1;
		try { if (StringUtils.isNotBlank(dia)) dayOfMonth = Integer.parseInt(dia); } catch (Exception ex) {}
		
		Fecha resultado = null;
		LocalDate localdate = LocalDate.of(year, month, dayOfMonth);
		if (localdate != null) {
			resultado = new Fecha(Articulo.FECHA_CONTRIBUCION, localdate);
		}
		
		if (StringUtils.isNotBlank(ses)) {
			if (resultado == null) {
				resultado = new Fecha(Articulo.FECHA_CONTRIBUCION);
				resultado.setAnio(anio);
			}
			resultado.setSesion(ses);
		}
			
		return resultado;

	}
	
	/**
	 * Obtiene la fecha de finalizaci�n
	 * @param date
	 * @return
	 */
	private Fecha makeFecha(DateCompleted date) {
		
		if (date == null) return null;

		int year = 1;
		try { if ((date.getYear() != null) && StringUtils.isNotBlank(date.getYear().getvalue())) year = Integer.parseInt(date.getYear().getvalue()); } catch (Exception ex) {}

		int month = 1;
		try { if ((date.getMonth() != null) && StringUtils.isNotBlank(date.getMonth().getvalue())) month = Integer.parseInt(date.getMonth().getvalue()); } catch (Exception ex) {}
		
		int dayOfMonth = 1;
		try { if ((date.getDay() != null) && StringUtils.isNotBlank(date.getDay().getvalue())) dayOfMonth = Integer.parseInt(date.getDay().getvalue()); } catch (Exception ex) {}
		
		Fecha resultado = null;
		LocalDate localdate = LocalDate.of(year, month, dayOfMonth);
		if (localdate != null) {
			resultado = new Fecha(Articulo.FECHA_COMPLETA, localdate);
		}
	
		return resultado;

	}

	/**
	 * Obtiene la fecha de revisi�n
	 * @param date
	 * @return
	 */
	private Fecha makeFecha(DateRevised date) {
		
		if (date == null) return null;

		int year = 1;
		try { if ((date.getYear() != null) && StringUtils.isNotBlank(date.getYear().getvalue())) year = Integer.parseInt(date.getYear().getvalue()); } catch (Exception ex) {}

		int month = 1;
		try { if ((date.getMonth() != null) && StringUtils.isNotBlank(date.getMonth().getvalue())) month = Integer.parseInt(date.getMonth().getvalue()); } catch (Exception ex) {}
		
		int dayOfMonth = 1;
		try { if ((date.getDay() != null) && StringUtils.isNotBlank(date.getDay().getvalue())) dayOfMonth = Integer.parseInt(date.getDay().getvalue()); } catch (Exception ex) {}
		
		Fecha resultado = null;
		LocalDate localdate = LocalDate.of(year, month, dayOfMonth);
		if (localdate != null) {
			resultado = new Fecha(Articulo.FECHA_REVISION, localdate);
		}
	
		return resultado;
		
	}

	/**
	 * Obtiene la fecha de edici�n
	 * @param date
	 * @return
	 */
	private Fecha makeFecha(EndingDate date) {
		
		if (date == null) return null;
		
		String dia = null;
		String mes = null;
		String anio = null;
		String ses = null;
		
		if (	(date.getMonthOrDayOrSeason() != null) && 
				(!date.getMonthOrDayOrSeason().isEmpty()) ){
			for (Object p: date.getMonthOrDayOrSeason()) {
				if (p instanceof Month)		mes = ((Month)p).getvalue();
				if (p instanceof Day) 		dia = ((Day)p).getvalue();
				if (p instanceof Season)	ses = ((Season)p).getvalue();
			}
		}

		int year = 1;
		if (	(date.getYear() != null) &&
				(StringUtils.isNotBlank(date.getYear().getvalue())) ) {
			anio = date.getYear().getvalue();
			try { year = Integer.parseInt(anio); } catch (Exception ex) {}
		}

		int month = 1;
		try { 
			if (StringUtils.isNotBlank(mes)) month = Integer.parseInt(mes); 
		} catch (Exception ex) {
			month = getMonthFrom(mes);
		}
		
		int dayOfMonth = 1;
		try { if (StringUtils.isNotBlank(dia)) dayOfMonth = Integer.parseInt(dia); } catch (Exception ex) {}
		
		Fecha resultado = null;
		LocalDate localdate = LocalDate.of(year, month, dayOfMonth);
		if (localdate != null) {
			resultado = new Fecha(Articulo.FECHA_EDICION, localdate);
		}
		
		if (StringUtils.isNotBlank(ses)) {
			if (resultado == null) {
				resultado = new Fecha(Articulo.FECHA_EDICION);
				resultado.setAnio(anio);
			}
			resultado.setSesion(ses);
		}

		return resultado;
		
	}

	/**
	 * Fecha de publicacion
	 * @param date
	 * @return
	 */
	//2019 Jan-Mar
	private Pattern MEDLINE_DATE_PTR = Pattern.compile("(\\d{4})\\s+(\\w{3})");
	private Fecha makeFecha(PubDate date) {
		
		if (	(date == null) || 
				(date.getYearOrMonthOrDayOrSeasonOrMedlineDate() == null) ||
				(date.getYearOrMonthOrDayOrSeasonOrMedlineDate().isEmpty())) {
			return null;
		}
		
		String dia = null;
		String mes = null;
		String anio = null;
		String ses = null;
		String fecha = null;		
		
		if (	(date.getYearOrMonthOrDayOrSeasonOrMedlineDate() != null) && 
				(!date.getYearOrMonthOrDayOrSeasonOrMedlineDate().isEmpty()) ){
			for (Object p: date.getYearOrMonthOrDayOrSeasonOrMedlineDate()) {
				if (p instanceof Year)			anio = ((Year)p).getvalue();
				if (p instanceof Month)			mes = ((Month)p).getvalue();
				if (p instanceof Day) 			dia = ((Day)p).getvalue();
				if (p instanceof Season)		ses = ((Season)p).getvalue();
				if (p instanceof MedlineDate)	fecha = ((MedlineDate)p).getvalue();
			}
		}

		int year = 1;
		try { if (StringUtils.isNotBlank(anio)) year = Integer.parseInt(anio); } catch (Exception ex) {}

		int month = 1;
		try { 
			if (StringUtils.isNotBlank(mes)) month = Integer.parseInt(mes); 
		} catch (Exception ex) {
			month = getMonthFrom(mes);
		}
		
		int dayOfMonth = 1;
		try { if (StringUtils.isNotBlank(dia)) dayOfMonth = Integer.parseInt(dia); } catch (Exception ex) {}
		
		Fecha resultado = null;
		LocalDate localdate = LocalDate.of(year, month, dayOfMonth);
		if ((localdate != null) && (localdate.getYear()>=2)) {
			resultado = new Fecha(Articulo.FECHA_PUBLICACION, localdate);
		}
		
		if (StringUtils.isNotBlank(ses)) {
			if (resultado == null) {
				resultado = new Fecha(Articulo.FECHA_PUBLICACION);
				resultado.setAnio(anio);
			}
			resultado.setSesion(ses);
		}
		
		if (StringUtils.isNotBlank(fecha)) {
			if (resultado == null) {
				resultado = new Fecha(Articulo.FECHA_PUBLICACION);
			}
			if (StringUtils.isBlank(resultado.getAnio())) {
				if (anio != null) {
					resultado.setAnio(anio);
				} else {
					Matcher m = MEDLINE_DATE_PTR.matcher(fecha);
					if (m.find()) {
						try {
							year = Integer.parseInt(m.group(1));
							month = getMonthFrom(m.group(2));
							dayOfMonth = 1;
							localdate = LocalDate.of(year, month, dayOfMonth);
							if (localdate != null) {
								resultado = new Fecha(Articulo.FECHA_PUBLICACION, localdate);
							}
						} catch (Exception ex) {
							System.out.println("DEBUG- makeFecha: " + fecha);
						}
					}
				}
			}
			StringBuffer sb = new StringBuffer();
			if (StringUtils.isNotBlank(resultado.getSesion())) sb.append(resultado.getSesion());
			sb.append("(");
			sb.append(fecha);
			sb.append(")");
			resultado.setSesion(sb.toString());
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

		List<Fecha> resultado = instances.
			stream().
			filter(p -> 	(p != null) &&
							(StringUtils.isNotBlank(p.getDateType())) ).
			map(instance -> 	{
				Fecha fecha = null;
				LocalDate localdate = makeDate(instance);
				String dateType = instance.getDateType();
				if (	(StringUtils.isNotBlank(dateType)) &&
						(localdate != null)) {
					fecha = new Fecha(dateType, localdate);
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

		List<Fecha> resultado = history.getPubMedPubDate().
			stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getPubStatus())) ).
			map(instance -> {
				LocalDateTime localdate = makeDate(instance);
				String pubStatus = instance.getPubStatus();
				if (	(localdate == null)) {
					return null;
				}
				return new Fecha(pubStatus, localdate.toLocalDate());
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
		
		List<Autor> resultado = authorList.
			stream().
			filter(p -> (p != null) &&
						(YES.equals(p.getCompleteYN())) && 
						(p.getAuthor() != null) &&
						(!p.getAuthor().isEmpty())).
			flatMap(list -> makeAutores(list).
					stream()).
			filter(p -> 	(p != null)).
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
		
		List<Autor> resultado = personalNameSubjectList.getPersonalNameSubject().
			stream().
			filter(p -> (p != null) ).
			map(instance ->		{
				Autor autor = new Autor();
				autor.setTipo(Articulo.AUTOR);
				if (instance.getInitials() != null)	autor.setIniciales(	instance.getInitials().getvalue());
				if (instance.getForeName() != null)	autor.setNombre(	instance.getForeName().getvalue());
				if (instance.getLastName() != null)	autor.setApellidos(	instance.getLastName().getvalue());
				if (instance.getSuffix() != null)	autor.setSufijo(	instance.getSuffix().getvalue());
				return autor;
			}).
			filter(p -> 	(p != null)).
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
	private List<Autor> makeAutores(AuthorList authorList) {

		if (	(authorList == null)  ||
				(authorList.getAuthor() == null) ||
				(authorList.getAuthor().isEmpty())) return null; 

		List<Autor> resultado = authorList.getAuthor().
			stream().
			filter(p ->		(p !=null ) && 
							(YES.equals(p.getValidYN())) ).
			map(instance ->		{
				Autor autor = new Autor();
				if (StringUtils.isNotBlank(authorList.getType())) {
					autor.setTipo(authorList.getType());
				} else {
					autor.setTipo(Articulo.AUTOR);
				}
				instance.getEqualContrib();
				autor.addIds(makeIds(instance.getIdentifier()));
				if (instance.getLastNameOrForeNameOrInitialsOrSuffixOrCollectiveName() != null) {
					instance.getLastNameOrForeNameOrInitialsOrSuffixOrCollectiveName().forEach(d -> {
						if (d!=null) {
							if (d instanceof Initials)			autor.setIniciales(((Initials)d).getvalue());
							if (d instanceof Suffix)			autor.setSufijo(((Suffix)d).getvalue());
							if (d instanceof ForeName)			autor.setNombre(((ForeName)d).getvalue());
							if (d instanceof LastName)			autor.setApellidos(((LastName)d).getvalue());
							if (d instanceof CollectiveName)	{
								if (Articulo.AUTOR.equals(autor.getTipo())) {
									autor.setTipo(Articulo.GRUPO);
								} if (Articulo.EDITOR.equals(autor.getTipo())) {
									autor.setTipo(Articulo.EDITORIAL);
								}
								autor.setNombre(((CollectiveName)d).getvalue());
							}
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

	private List<Autor> makeAutores(InvestigatorList investigatorList) {

		if (	(investigatorList == null)  ||
				(investigatorList.getInvestigator() == null)  ||
				(investigatorList.getInvestigator().isEmpty())) return null; 

		List<Autor> resultado = investigatorList.getInvestigator().
			stream().
			filter(p ->		(p !=null ) && 
							(YES.equals(p.getValidYN())) ).
			map(instance ->		{
				Autor autor = new Autor();
				autor.setTipo(Articulo.INVESTIGADOR);
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
		
		List<Centro> resultado = affiliationInfo.
			stream().
			filter(p ->	(	(p != null) && 
							(StringUtils.isNotBlank(p.getAffiliation()))) ). 
			map(instance ->	{
				Centro centro = new Centro();
				centro.setNombre(instance.getAffiliation());
				centro.addIds(	makeIds(instance.getIdentifier()));
				return centro;
			}).
			filter(p -> 	(p != null)).
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
	private List<Localizacion> makeLocalizacion(List<Object> paginationOrELocationID) {
		
		if (	(paginationOrELocationID == null) ||
				(paginationOrELocationID.isEmpty()) ) return null;
		
		List<Localizacion> resultado = paginationOrELocationID.
			stream().
			filter(p -> 	(p != null)).		
			map(instance -> {
				Localizacion ref = null; 
				if (instance instanceof Pagination) {
					Pagination pagination = (Pagination) instance;
					ref = makeLocalizacion(pagination);
				} else if (instance instanceof ELocationID) {
					ELocationID location = (ELocationID) instance;
					ref = makeLocalizacion(location);
				}
				return ref;
			}).
			filter(p -> 	(p != null)).
			collect(Collectors.toList());

		return resultado;
		
	}

	/**
	 * Crea una referencia electronica de un documento
	 * @param pagination
	 * @return
	 */
	private Localizacion makeLocalizacion(ELocationID location) {

		if (	(location == null) ||
				(NO.equals(location.getValidYN()))) return null;

		String tipo = location.getEIdType();
		String path = location.getvalue();
		return new Localizacion(tipo, path);
				
	}

	/**
	 * Crea una localizaci�n de un documento paginado
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

		return new Localizacion(ini, fin, ref);
		
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
	 * @param date
	 * @return la fecha
	 */
	private LocalDate makeDate(ArticleDate date) {
		
		if (date == null) return null;

		int year = 1;
		try { if ((date.getYear() != null) && StringUtils.isNotBlank(date.getYear().getvalue())) year = Integer.parseInt(date.getYear().getvalue()); } catch (Exception ex) {}

		int month = 1;
		try { if ((date.getMonth() != null) && StringUtils.isNotBlank(date.getMonth().getvalue())) month = Integer.parseInt(date.getMonth().getvalue()); } catch (Exception ex) {}
		
		int dayOfMonth = 1;
		try { if ((date.getDay() != null) && StringUtils.isNotBlank(date.getDay().getvalue())) dayOfMonth = Integer.parseInt(date.getDay().getvalue()); } catch (Exception ex) {}
		
		LocalDate resultado = LocalDate.of(year, month, dayOfMonth);
	
		return resultado;

	}
	
	/**
	 * Obtiene una fecha compatible Java
	 * @param articleDate
	 * @return la fecha
	 */
	private LocalDate makeDate(String date) {
		return (StringUtils.isNotBlank(date)) ? LocalDate.parse(date, DATE_FMT) : null;
	}

	/**
	 * Obtiene una fecha compatible Java
	 * @param date
	 * @return la fecha
	 */
	private LocalDateTime makeDate(PubMedPubDate date) {
		
		if (date == null) return null;
		
		int year = 1;
		try { if ((date.getYear() != null) && StringUtils.isNotBlank(date.getYear().getvalue())) year = Integer.parseInt(date.getYear().getvalue()); } catch (Exception ex) {}

		int month = 1;
		try { if ((date.getMonth() != null) && StringUtils.isNotBlank(date.getMonth().getvalue())) month = Integer.parseInt(date.getMonth().getvalue()); } catch (Exception ex) {}
		
		int dayOfMonth = 1;
		try { if ((date.getDay() != null) && StringUtils.isNotBlank(date.getDay().getvalue())) dayOfMonth = Integer.parseInt(date.getDay().getvalue()); } catch (Exception ex) {}
		
		int hour = 0;
		try { if (date.getHour() != null) hour = Integer.parseInt(date.getHour()); } catch (Exception ex) {}

		int minute = 0;
		try { if (date.getMinute() != null) minute = Integer.parseInt(date.getMinute()); } catch (Exception ex) {}

		int second = 0;
		try { if (date.getSecond() != null) second = Integer.parseInt(date.getSecond()); } catch (Exception ex) {}

		LocalDateTime resultado = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);

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
	 * Construye los datos del fasc�culo
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
	 * Conjuntos de datos utilizados por el art�culo
	 * @param dataBankList
	 * @return
	 */
	private List<Descriptor> makeData(DataBankList dataBankList) {

		if (	(dataBankList == null) ||
				(dataBankList.getDataBank() == null) ||
				(dataBankList.getDataBank().isEmpty()) ||
				(NO.contentEquals(dataBankList.getCompleteYN()))) return null;
		
		List<Descriptor> resultado = dataBankList.getDataBank().
			stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getDataBankName())) &&
							(p.getAccessionNumberList() != null) ||
							(p.getAccessionNumberList().getAccessionNumber() != null) ||
							(!p.getAccessionNumberList().getAccessionNumber().isEmpty()) ).
			flatMap(instance -> 	{
				Vector<Descriptor> list = new Vector<>();
				for (AccessionNumber number: instance.getAccessionNumberList().getAccessionNumber()) {
					list.add(new Descriptor(
						instance.getDataBankName(),
						number.getvalue()));
				}
				return list.stream();
			}).
			filter(p -> 	(p != null)).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}

	private List<Descriptor> makeData(List<SpaceFlightMission> spaceFlightMission) {

		if (	(spaceFlightMission == null) ||
				(spaceFlightMission.isEmpty())) return null;

		List<Descriptor> resultado = spaceFlightMission.
			stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> new Descriptor(
					Descriptor.PUBMED,
					instance.getvalue())).
			filter(p -> 	(p != null)).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	/**
	 * Permisos de reproducci�n (pais, agencia y permiso
	 * @param grantList
	 * @return
	 */
	private List<Permiso> makePermisos(GrantList grantList) {

		if (	(grantList == null) ||
				(grantList.getGrant() == null) ||
				(grantList.getGrant().isEmpty()) ||
				("N".contentEquals(grantList.getCompleteYN()))) return null;
		
		List<Permiso> resultado = grantList.getGrant().
			stream().
			filter(p ->			(p != null) ).
			map(instance -> 	{
				return new Permiso(
						instance.getCountry(),
						instance.getAgency(),
						instance.getGrantID(),
						instance.getAcronym());
			}).
			filter(p -> 	(p != null)).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}

	/**
	 * Obtiene la lista de objetos que extienden el art�culo
	 * @param objects
	 * @return
	 */
	public Map<String, Map<String, String>> makePropiedades(ObjectList objects) {
		
		if (	(objects == null) || 
				(objects.getObject() == null) ||
				(objects.getObject().isEmpty())) return null;

		Map<String, Map<String, String>> resultado = objects.getObject().
			stream().
			filter(p ->		(p != null) &&
							(p.getType() != null) &&
							(p.getParam() != null) &&
							(!p.getParam().isEmpty()) ).
			map(instance -> {

				String tipo = instance.getType();
				Map<String, String> parametros = instance.getParam().
					stream().
					filter(p ->		(p != null) &&
									(StringUtils.isNotBlank(p.getName())) &&
									(StringUtils.isNotBlank(p.getvalue())) ).
					map(param ->	{
						return new SimpleEntry<String, String>(
									param.getName(),
									param.getvalue());
					}).
					filter(p -> 	(p != null)).
					collect(Collectors.toMap(
							p -> p.getKey(), 
							p -> p.getValue(),
							(o1, o2) -> o1 + ", " + o2 ));
					
				return new SimpleEntry<String, Map<String, String>>(
						tipo, 
						parametros);
				
			}).
			filter(p -> 	(p != null)).
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

	/**
	 * Obtiene la lista de citas al articulo
	 * @param referenceList
	 * @return
	 */
	private List<Referencia> makeReferencias(List<ReferenceList> referenceList) {

		if (	(referenceList == null) || 
				(referenceList.isEmpty())) return null;

		List<Referencia> resultado = referenceList.
			stream().
			filter(p ->		(p != null) &&
							(p.getReference() != null) &&
							(!p.getReference().isEmpty()) ).
			flatMap(list -> {
				if (StringUtils.isNotBlank(list.getTitle())) {
					// TODO: Siempre pone referencias O BIBLIOGRAFIA
					String str = list.getTitle().trim().toUpperCase();
					if (!(	("REFERENCE".equals(str)) ||
							("REFERENCES".equals(str)) ||
							("LITERATURE CITED".equals(str)) ||
							("BIBLIOGRAPHY".equals(str)) )) {
						System.out.println ("UN NUEVO TIPO DE REFERENCIA EN makeReferencias: " + list.getTitle());
					}
				}
				return list.getReference().stream();} ).
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getCitation())) &&
							(p.getArticleIdList() != null) &&
							(p.getArticleIdList().getArticleId() != null) &&
							(!p.getArticleIdList().getArticleId().isEmpty()) ). 
			map(instance -> {
				
				Map<String, String> refIds = instance.getArticleIdList().getArticleId().
					stream().
					filter(p -> (p != null) &&
								(StringUtils.isNotBlank(p.getIdType())) &&
								(StringUtils.isNotBlank(p.getvalue())) ).
					map(id -> {
						return new SimpleEntry<String, String>(
									id.getIdType(),
									id.getvalue());
					}).
					filter(p -> 	(p != null)).
					collect(Collectors.toMap(
							p -> p.getKey(), 
							p -> p.getValue(),
							(o1, o2) -> o1 + ", " + o2 ));
				
				Referencia ref = new Referencia();
				ref.setCita(instance.getCitation());
				ref.setIds(refIds);
				return ref;
				
			}).
			filter(p -> 	(p != null)).
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

		List<Termino> resultado = meshHeadingList.getMeshHeading().
			stream().
			filter(p ->		(p != null) &&
							(p.getDescriptorName() != null) &&
							(StringUtils.isNotBlank(p.getDescriptorName().getUI())) &&
							(StringUtils.isNotBlank(p.getDescriptorName().getvalue())) &&
							(p.getQualifierName() != null) &&
							(!p.getQualifierName().isEmpty())).
			map(instance -> {
				List<Termino> items = instance.getQualifierName().
					stream().
					filter(p ->	(p != null)  &&
								(StringUtils.isNotBlank(p.getUI())) &&
								(StringUtils.isNotBlank(p.getvalue())) ).
					map(item -> {
						return new Termino(
								TermType.QUALIFIER,
								DescType.NONE,
								item.getUI(),
								item.getvalue());
					}).
					filter(p -> 	(p != null)).
					collect(Collectors.toList());
				return new Termino(
						TermType.DESCRIPTOR,
						DescType.NONE,
						instance.getDescriptorName().getUI(),
						instance.getDescriptorName().getvalue(),
						items);			
			}).
			filter(p -> 	(p != null)).
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

		List<Termino> resultado = chemicalList.getChemical().
			stream().
			filter(p ->		(p != null) &&
							(p.getNameOfSubstance() != null) &&
							(StringUtils.isNotBlank(p.getNameOfSubstance().getUI())) &&
							(StringUtils.isNotBlank(p.getNameOfSubstance().getvalue()))).
			map(instance -> {
				return new Termino(
						TermType.SUPPLEMENTAL,
						DescType.CHEMICAL,
						instance.getNameOfSubstance().getUI(),
						instance.getNameOfSubstance().getvalue(),
						instance.getRegistryNumber());			
			}).
			filter(p -> 	(p != null)).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	/**
	 * Establece t�rminos de clase 2, 3 y 4 protocolos, dolencias y organismos
	 * @param supplMeshList
	 * @return
	 */
	private List<Termino> makeTerminos(SupplMeshList supplMeshList) {
		
		if (	(supplMeshList == null) || 
				(supplMeshList.getSupplMeshName() == null) ||
				(supplMeshList.getSupplMeshName().isEmpty())) return null;

		List<Termino> resultado = supplMeshList.getSupplMeshName().
			stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getUI())) &&
							(StringUtils.isNotBlank(p.getvalue()))).
			map(instance -> {
				return new Termino(
						TermType.SUPPLEMENTAL,
						Termino.getSupplementalType(instance.getType()),
						instance.getUI(),
						instance.getvalue());			
			}).
			filter(p -> 	(p != null)).
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
		
		List<Termino> resultado = publicationType.
			stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getUI())) &&
							(StringUtils.isNotBlank(p.getvalue()))).
			map(instance -> {
				return new Termino(
						TermType.DESCRIPTOR,
						DescType.PUBLICATION,
						instance.getUI(),
						instance.getvalue());			
			}).
			filter(p -> 	(p != null)).
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
		
		List<Descriptor> resultado = keywordList.
			stream().
			filter(p -> 	(p != null) &&
							(p.getKeyword() != null) &&
							(!p.getKeyword().isEmpty())).
			flatMap(list -> {
				String tipo = list.getOwner();
				return list.getKeyword().
					stream().
					filter(p ->		(p != null) &&
									(StringUtils.isNotBlank(p.getvalue()))).
					map(instance ->	new Descriptor(tipo, instance.getvalue())).
					filter(p -> 	(p != null));
			}).
			filter(p -> 	(p != null)).
			collect(Collectors.toList());

		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	/**
	 * Prepara una lista de genes
	 * @param geneSymbolList
	 * @return
	 */
	private List<Descriptor> makeGenes(GeneSymbolList geneSymbolList) {

		if (	(geneSymbolList == null) || 
				(geneSymbolList.getGeneSymbol() == null) || 
				(geneSymbolList.getGeneSymbol().isEmpty())) return null;
		
		List<Descriptor> resultado = geneSymbolList.getGeneSymbol().
			stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> {
				return new Descriptor(Descriptor.PUBMED, instance.getvalue());
			}).
			filter(p -> 	(p != null)).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}
	
	public List<Descriptor> makeItems(List<ItemList> itemList) {

		if (	(itemList == null) || 
				(itemList.isEmpty())) return null;

		List<Descriptor> resultado = itemList.
			stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getListType())) &&
							(p.getItem() != null) &&
							(!p.getItem().isEmpty()) ).
			flatMap(list -> {
				String tipo = list.getListType();
				return list.getItem().
					stream().
					filter(p ->		(p != null) &&
									(StringUtils.isNotBlank(p.getvalue()))).
					map(instance ->	new Descriptor(tipo, instance.getvalue())).
					filter(p -> 	(p != null));
			}).
			filter(p -> 	(p != null)).
			collect(Collectors.toList());
						
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}
	
	/**
	 * Observaciones de diversas fuentas
	 * @param generalNote
	 * @return
	 */
	// TODO	private Map<String, String> makeObservacionesMap(List<GeneralNote> generalNote) {
	public List<Descriptor> makeNotes(List<GeneralNote> generalNote) {
		
		if (	(generalNote == null) || 
				(generalNote.isEmpty())) return null;
		
		List<Descriptor> resultado = generalNote.
			stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> {
					return new Descriptor(
							instance.getOwner(),
							instance.getvalue());
			}).
			filter(p -> 	(p != null)).
			collect(Collectors.toList());
						
		if (resultado.isEmpty()) resultado = null;
		return resultado;
		
	}
	
}
