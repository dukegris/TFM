package es.rcs.tfm.srv.repository;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.PubArticleAuthorEntity;
import es.rcs.tfm.db.model.PubArticleEntity;
import es.rcs.tfm.db.model.PubArticleFileEntity;
import es.rcs.tfm.db.model.PubArticleFileKey;
import es.rcs.tfm.db.model.PubArticlePublicationEntity;
import es.rcs.tfm.db.model.PubArticlePublicationKey;
import es.rcs.tfm.db.model.PubArticleTermEntity;
import es.rcs.tfm.db.model.PubArticleTermKey;
import es.rcs.tfm.db.model.PubAuthorEntity;
import es.rcs.tfm.db.model.PubBlockSubentity;
import es.rcs.tfm.db.model.PubCenterEntity;
import es.rcs.tfm.db.model.PubDateSubentity;
import es.rcs.tfm.db.model.PubFileEntity;
import es.rcs.tfm.db.model.PubKeywordSubentity;
import es.rcs.tfm.db.model.PubPermissionSubentity;
import es.rcs.tfm.db.model.PubPropertySubentity;
import es.rcs.tfm.db.model.PubPublicationEntity;
import es.rcs.tfm.db.model.PubReferenceSubentity;
import es.rcs.tfm.db.model.PubTermEntity;
import es.rcs.tfm.db.model.PubTextSubentity;
import es.rcs.tfm.db.model.PubValuesSubentity;
import es.rcs.tfm.db.repository.PubArticleAuthorRepository;
import es.rcs.tfm.db.repository.PubArticleFileRepository;
import es.rcs.tfm.db.repository.PubArticlePublicationRepository;
import es.rcs.tfm.db.repository.PubArticleRepository;
import es.rcs.tfm.db.repository.PubArticleTermRepository;
import es.rcs.tfm.db.repository.PubAuthorRepository;
import es.rcs.tfm.db.repository.PubBlockRepository;
import es.rcs.tfm.db.repository.PubCentreRepository;
import es.rcs.tfm.db.repository.PubFileRepository;
import es.rcs.tfm.db.repository.PubPublicationRepository;
import es.rcs.tfm.db.repository.PubReferenceRepository;
import es.rcs.tfm.db.repository.PubTermRepository;
import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.model.Autor;
import es.rcs.tfm.srv.model.BloqueAnotado;
import es.rcs.tfm.srv.model.Centro;
import es.rcs.tfm.srv.model.Fasciculo;
import es.rcs.tfm.srv.model.Fichero;
import es.rcs.tfm.srv.model.Libro;
import es.rcs.tfm.srv.model.Localizacion;
import es.rcs.tfm.srv.model.Referencia;
import es.rcs.tfm.srv.model.Revista;
import es.rcs.tfm.srv.model.Termino;
import es.rcs.tfm.srv.model.Termino.DescType;
import es.rcs.tfm.srv.model.Termino.ProviderType;
import es.rcs.tfm.srv.model.Termino.TermType;

@Repository(
		value = SrvNames.DATABASE_REP)
public class DatabaseRepository {

	private static final Logger LOG = LoggerFactory.getLogger(DatabaseRepository.class);

	private static final String KEYWORD			= "KEYWORD";
	private static final String GEN				= "GEN";
	private static final String DATA			= "DATA";
	private static final String NOTE 			= "NOTE";
	private static final String NASA_FLIGHT		= "NASA_FLIGHT";

	private static int NUMBER = 0;
	private static LocalTime START_DATETIME = LocalTime.now();
	private static Vector<Duration> STATS = new Vector<>();
	private static final Map<String, String> STATUS = new HashMap<>();
	private static final Map<String, String> MEDIA = new HashMap<>();
	private static final Map<String, String> LANGUAGE = new HashMap<>();
	private static final Map<String, String> DATE = new HashMap<>();
	private static final Map<String, String> CATEGORY= new HashMap<>();

	public static final Map<ProviderType, String> TERM_PRVDR = new HashMap<>();
	public static final Map<TermType, String> TERM_TERM = new HashMap<>();
	public static final Map<DescType, String> TERM_DESC = new HashMap<>();
	static {
		/*
	private String estado; // Completed | In-Process | PubMed-not-MEDLINE | In-Data-Review | Publisher | MEDLINE | OLDMEDLINE
	private Map<String, String> ids = new HashMap<String, String>(); // NASA | KIE | PIP | POP | ARPL | CPC | IND | CPFH | CLML | NRCBL | NLM | QCIM
	private List<Autor> autores = new ArrayList<Autor>(); // 	investigators | authors | editors
		 */
		// https://dtd.nlm.nih.gov/ncbi/pubmed/att-PubStatus.html
		STATUS.put(		"RECEIVED",				"RECIBIDO");
		STATUS.put(		"ACCEPTED", 			"ACEPTADO");
		STATUS.put(		"REVISED", 				"REVISADO");
		STATUS.put(		"RETRACTED", 			"RETIRADO");

		STATUS.put(		"PPUBLISH",				"EN ELECTRONICO");
		STATUS.put(		"AHEADOFPRINT", 		"DISPONIBLE");

		STATUS.put(		"ECOLLECTION", 			"ECOLLECTION");
		STATUS.put(		"EPUBLISH", 			"EN PAPEL");
		
		STATUS.put(		"PMC", 					"PMC");
		STATUS.put(		"PMCR", 				"PMCR");
		STATUS.put(		"PMC-RELEASE", 			"PMC-RELEASE");
			
		STATUS.put(		"PUBMED", 				"PUBMED");
		STATUS.put(		"PUBMEDR", 				"PUBMEDR");
		
		STATUS.put(		"PREMEDLINE", 			"PREMEDLINE");
		STATUS.put(		"MEDLINE", 				"MEDLINE");
		STATUS.put(		"MEDLINER", 			"MEDLINER");
		
		STATUS.put(		"ENTREZ", 				"ENTREZ");
		
		MEDIA.put(		"INTERNET",				"INTERNET");
		MEDIA.put(		"PRINT",				"PAPEL");
		MEDIA.put(		"PRINT-ELECTRONIC",		"PAPEL Y ELECTRONICO");
		MEDIA.put(		"ELECTRONIC",			"ELECTRONICO");
		MEDIA.put(		"ELECTRONIC-PRINT",		"IMPRESO DE ELECTRONICO");
		MEDIA.put(		"ELECTRONIC-ECOLLECTION",	"COLECCION ELECTRONICA");
		
		// https://www.loc.gov/marc/languages/language_code.html
		LANGUAGE.put(	"ENG",					"en");
		
		DATE.put(		"ACCEPTED",				"ACEPTADO");
		DATE.put(		"REVISED",				"REVISADO");
		DATE.put(		"RECEIVED",				"RECIBIDO");
		DATE.put(		"PUBMED",				"EN PUBMED");
		DATE.put(		"MEDLINE",				"EN MEDLINE");
		DATE.put(		"ENTREZ",				"EN ENTREZ");

		// 	En abstract: NlmCategory (BACKGROUND|OBJECTIVE|METHODS|RESULTS|CONCLUSIONS|UNASSIGNED) #IMPLIED
		CATEGORY.put(	"BACKGROUND",			"BACKGROUND");
		CATEGORY.put(	"OBJECTIVE",			"OBJECTIVE");
		CATEGORY.put(	"METHODS",				"METHODS");
		CATEGORY.put(	"RESULTS",				"RESULTS");
		CATEGORY.put(	"CONCLUSIONS",			"CONCLUSIONS");
		CATEGORY.put(	"UNASSIGNED", 			"UNASSIGNED");

		TERM_PRVDR.put(	ProviderType.MESH, 		"MESH");

		TERM_TERM.put(	TermType.DESCRIPTOR, 	"DESCRIPTOR");
		TERM_TERM.put(	TermType.QUALIFIER, 	"QUALIFIER");
		TERM_TERM.put(	TermType.SUPPLEMENTAL, 	"SUPPLEMENTAL");
		TERM_TERM.put(	TermType.PHARMALOGICAL, "PHARMALOGICAL");

		TERM_DESC.put(	DescType.NONE,			"NONE");
		TERM_DESC.put(	DescType.PROTOCOL,		"PROTOCOL");
		TERM_DESC.put(	DescType.ORGANISM,		"ORGANISM");
		TERM_DESC.put(	DescType.DISEASE,		"DISEASE");
		TERM_DESC.put(	DescType.CHEMICAL,		"CHEMICAL");
		TERM_DESC.put(	DescType.PUBLICATION,	"PUBLICATION");
		TERM_DESC.put(	DescType.ECIN,			"ECIN");
		TERM_DESC.put(	DescType.ECOUT,			"ECOUT");
		
	}
	private static final String get(Map<String, String> map, String key) {
		if (StringUtils.isBlank(key)) return null;
		String value = map.get(key.toUpperCase());
		if (StringUtils.isBlank(value)) value = key;
		return value;
	}
	
	/**
	 * Debido al tamaño desproporcionado de algunos centros se debe recortar su nombre
	 * @param str Cadena a recortar
	 * @return Cadena recortada
	 */
	private static String getCentreName(String str) {
		if (StringUtils.isBlank(str)) return str;
		ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(str);
		String result = StringUtils.abbreviate(byteBuffer.toString(), 2046);
		return result;
	}
	private static final String getDateTypeField(String key) {
		return get(DATE, key);
	}
	private static final String getLanguageField(String key) {
		return get(LANGUAGE, key);
	}
	private static final String getMediaField(String key) {
		return get(MEDIA, key);
	}

	private static final String getStatusField(String key) {
		return get(STATUS, key);
	}
	
	public static void saveStats(int FILE_ID) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter("./stats.txt", true));
			for (int index = 0; index < STATS.size(); index ++) {
				out.println(String.format("%d\t%d\t%d\t%s", FILE_ID, index, STATS.get(index).toNanos(), STATS.get(index).toString()));
			};
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) out.close();
		}
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// DIFERENCIAS

	public boolean anyDifferences(Articulo source, PubArticleEntity destination) {
		
		if (source == null) return false;
		if (destination == null) return true;
		
		boolean result = false;
		if (destination.getId() == null) result = true;
		if (!result && !StringUtils.equals(source.getPropietario(), destination.getOwner())) result = true;
		if (!result && !StringUtils.equals(source.getPmid(), destination.getPmid())) result = true;
		if (!result && !StringUtils.equals(getLanguageField(source.getIdioma()),destination.getLanguage())) result = true;
		if (!result && !StringUtils.equals(getStatusField(source.getEstado()), destination.getStatus())) result = true;
		if (!result && !StringUtils.equals(getMediaField(source.getMedio()), destination.getMediaType())) result = true;
		if (!result && !StringUtils.equals(source.generateTitle(), destination.getTitle())) result = true;

		return result;
		
	}

	private boolean anyDifferences(PubArticleAuthorEntity source, PubArticleAuthorEntity destination) {

		if (source == null) return false;
		if (destination == null) return true;
		
		boolean result = false;
		if (destination.getId() == null) result = true;
		if (!result && !StringUtils.equals(source.getType(), destination.getType())) result = true;
		
		return result;
		
	}

	private boolean anyDifferences(Autor source, PubAuthorEntity destination) {

		if (source == null) return false;
		if (destination == null) return true;
		
		boolean result = false;
		if (destination.getId() == null) result = true;
		if (!result && !StringUtils.equals(source.getIniciales(), destination.getInitials())) result = true;
		if (!result && !StringUtils.equals(source.getNombre(), destination.getName())) result = true;
		if (!result && !StringUtils.equals(source.getSufijo(), destination.getSuffix())) result = true;
		if (!result && !StringUtils.equals(source.getApellidos(), destination.getLastName())) result = true;
		
		return result;
		
	}

	private boolean anyDifferences(PubBlockSubentity source, PubBlockSubentity destination) {
		
		if (source == null) return false;
		if (destination == null) return true;
		
		boolean result = false;
		if (destination.getId() == null) result = true;
		if (!result && (Integer.compare(source.getOffset(), destination.getOffset()) != 0)) result = true;
		if (!result && !StringUtils.equals(source.getBlockType(), destination.getBlockType())) result = true;
		if (!result && !StringUtils.equals(source.getText(), destination.getText())) result = true;
		
		return result;
		
	}
	
	private boolean anyDifferences(Centro source, PubCenterEntity destination) {
		
		if (source == null) return false;
		if (destination == null) return true;
		
		boolean result = false;
		if (destination.getId() == null) result = true;
		if (!result && !StringUtils.equals(source.getTipo(), destination.getCentretype())) result = true;
		if (!result && !StringUtils.equals(getCentreName(source.getNombre()), destination.getName())) result = true;

		return result;
		
	}

	private boolean anyDifferences(Fasciculo source, PubArticlePublicationEntity destination) {
		
		if (source == null) return false;
		if (destination == null) return true;

		boolean result = false;
		if (destination.getId() == null) result = true;
		if (!result && !StringUtils.equals(source.getMedio(), destination.getMedia())) result = true;
		if (!result && !StringUtils.equals(source.getNumero(), destination.getNumber())) result = true;
		if (!result && !StringUtils.equals(source.getTipo(), destination.getType())) result = true;
		if (!result && !StringUtils.equals(source.getVolumen(), destination.getVolume())) result = true;
		if (source.getFecha() != null) {
			if (!result && (source.getFecha().getFecha() != null) && (!source.getFecha().getFecha().isEqual(destination.getDate()))) result = true;
			try {
				if (!result && (source.getFecha().getAnio() != null) && (Integer.parseInt(source.getFecha().getAnio()) != destination.getYear())) result = true;
			} catch (Exception ex) {}
		}

		return result;
		
	}

	private boolean anyDifferences(Fichero source, PubFileEntity destination) {
		
		if (source == null) return false;
		if (destination == null) return true;
		
		boolean result = false;
		if (destination.getId() == null) result = true;
		if (!result && !StringUtils.equals(source.getTipo(), destination.getSource())) result = true;
		if (!result && !StringUtils.equals(source.getNombre(), destination.getFileName())) result = true;
		if (!result && !StringUtils.equals(source.getGzDirectorio(), destination.getGzDirectory())) result = true;
		if (!result && !StringUtils.equals(source.getGzFichero(), destination.getGzFileName())) result = true;
		if (!result && !StringUtils.equals(source.getMd5Fichero(), destination.getMd5FileName())) result = true;
		if (!result && !StringUtils.equals(source.getUncompressFichero(), destination.getUncompressedFileName())) result = true;
		if (!result && (source.getGzInstante() != null) && source.getGzInstante().equals(destination.getGzTimeStamp())) result = true;
		if (!result && (source.getNumArticlesProcessed() != destination.getNumArticlesProcessed())) result = true;
		if (!result && (source.getNumArticlesTotal() != destination.getNumArticlesTotal())) result = true;
		if (!result && source.isProcesoArticulosCompletado() && !PubFileEntity.PROCESSED.equals(destination.getStatus())) result = true;

		return result;
		
	}

	private boolean anyDifferences(Libro source, PubPublicationEntity destination) {
		
		if (source == null) return false;
		if (destination == null) return true;
		
		boolean result = false;
		if (destination.getId() == null) result = true;
		if (!result && !StringUtils.equals(getMediaField(source.getMedio()),destination.getMedia())) result = true;
		if (!result && !StringUtils.equals(source.getEdicion(), destination.getEditionTitle())) result = true;
		if (!result && !StringUtils.equals(source.getEditor(), destination.getEditionEditor())) result = true;
		if (!result && !StringUtils.equals(source.getCiudad(), destination.getEditionCity())) result = true;
		if (!result && !StringUtils.equals(source.getVolumen(), destination.getVolume())) result = true;
		if (!result && !StringUtils.equals(source.getTituloVolumen(), destination.getVolumeTitle())) result = true;
		// TODO Title

		return result;
		
	}
	
	// No requerido
	protected boolean anyDifferences(PubReferenceSubentity source, PubReferenceSubentity destination) {

		if (source == null) return false;
		if (destination == null) return true;
	
		boolean result = false;
		if (destination.getId() == null) result = true;
		if (!result && !StringUtils.equals(source.getReference(), destination.getReference())) result = true;

		return result;
		
	}

	private boolean anyDifferences(Revista source, PubPublicationEntity destination) {
		
		if (source == null) return false;
		if (destination == null) return true;
		
		boolean result = false;
		if (destination.getId() == null) result = true;
		if (!result && !StringUtils.equals(source.getAbreviatura(), destination.getAbbreviation())) result = true;
		if (!result && !StringUtils.equals(getMediaField(source.getMedio()), destination.getMedia())) result = true;
		if (!result && !StringUtils.equals(source.getPais(), destination.getCountry())) result = true;
		if (!result && !StringUtils.equals(source.getTipo(), destination.getType())) result = true;
		if (!result && !StringUtils.equals(source.getNombre(), destination.getTitle())) result = true;

		return result;

	}

	private boolean anyDifferences(Termino source, PubTermEntity destination) {
		
		if (source == null) return false;
		if (destination == null) return true;
		
		boolean result = false;
		if (destination.getId() == null) result = true;
		if (!result && (source.getProvider() != null) && (!TERM_PRVDR.get(source.getProvider()).equals(destination.getProvider()))) result = true;
		if (!result && (source.getTermtype() != null) && (!TERM_TERM.get(source.getTermtype()).equals(destination.getTermtype()))) result = true;
		if (!result && (source.getDesctype() != null) && (!TERM_DESC.get(source.getDesctype()).equals(destination.getDesctype()))) result = true;
		if (!result && !StringUtils.equals(source.getCode(), destination.getCode())) result = true;
		if (!result && !StringUtils.equals(source.getValue(), destination.getValue())) result = true;
		
		return result;
		
	}
	
	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// BUILD ENITIES
	private PubArticleEntity buildEntity(Articulo source, PubArticleEntity destination) {
		
		if (	(source == null) || 
				(destination == null)) return null;
		
		if (StringUtils.isNotBlank(source.getPropietario())) destination.setOwner(source.getPropietario());
		if (StringUtils.isNotBlank(source.getPmid())) destination.setPmid(source.getPmid());
		if (StringUtils.isNotBlank(source.getEstado())) destination.setStatus(getStatusField(source.getEstado()));
		if (StringUtils.isNotBlank(source.getIdioma())) destination.setLanguage(getLanguageField(source.getIdioma()));
		if (StringUtils.isNotBlank(source.getMedio())) destination.setMediaType(getMediaField(source.getMedio()));

		String str = source.generateTitle();
		if (StringUtils.isNotBlank(str)) destination.setTitle(str);
		
		return destination;
		
	}

	private PubPublicationEntity buildEntity(Libro source, PubPublicationEntity destination) {
		
		if (	(source == null) || 
				(destination == null)) return null;

		destination.setType(PubPublicationEntity.BOOK_TYPE);
		if (StringUtils.isNotBlank(source.getMedio())) destination.setMedia(getMediaField(source.getMedio()));
		if (StringUtils.isNotBlank(source.getEdicion())) destination.setEditionTitle(source.getEdicion());
		if (StringUtils.isNotBlank(source.getEditor())) destination.setEditionEditor(source.getEditor());
		if (StringUtils.isNotBlank(source.getCiudad())) destination.setEditionCity(source.getCiudad());
		if (StringUtils.isNotBlank(source.getVolumen())) destination.setVolume(source.getVolumen());
		if (StringUtils.isNotBlank(source.getTituloVolumen())) destination.setVolumeTitle(source.getTituloVolumen());

		if (source.getTitulo() != null) {
			//TODO
			if (StringUtils.isNotBlank(source.getTitulo().getLibroId())) { 
				System.out.print(" LB-" + source.getTitulo().getLibroId());
			}
			if (StringUtils.isNotBlank(source.getTitulo().getParteId())) { 
				System.out.print(" PB-" + source.getTitulo().getParteId());
			}
			if (StringUtils.isNotBlank(source.getTitulo().getSeccionId())) { 
				System.out.print(" SB-" + source.getTitulo().getSeccionId());
			}
			if (StringUtils.isNotBlank(source.getTitulo().getTitulo())) { 
				System.out.println(" TB-" + source.getTitulo().getTitulo());
			}
		}
		
		if (source.getTituloColeccion() != null) {
			//TODO
			if (StringUtils.isNotBlank(source.getTituloColeccion().getLibroId())) { 
				System.out.print(" LC-" + source.getTituloColeccion().getLibroId());
			}
			if (StringUtils.isNotBlank(source.getTituloColeccion().getParteId())) { 
				System.out.print(" PC-" + source.getTituloColeccion().getParteId());
			}
			if (StringUtils.isNotBlank(source.getTituloColeccion().getSeccionId())) { 
				System.out.print(" SC-" + source.getTituloColeccion().getSeccionId());
			}
			if (StringUtils.isNotBlank(source.getTituloColeccion().getTitulo())) { 
				System.out.println(" TC-" + source.getTituloColeccion().getTitulo());
			}
		}

		if (source.getLocalizacion() != null) {
			//TODO
			System.out.print("LO");
		}

		return destination;
		
	}

	private PubAuthorEntity buildEntity(Autor source, PubAuthorEntity destination) {
		
		if (	(source == null) || 
				(destination == null)) return null;

		if (StringUtils.isNotBlank(source.getIniciales())) destination.setInitials(source.getIniciales());
		if (StringUtils.isNotBlank(source.getNombre())) destination.setName(source.getNombre());
		if (StringUtils.isNotBlank(source.getSufijo())) destination.setSuffix(source.getSufijo());
		if (StringUtils.isNotBlank(source.getApellidos())) destination.setLastName(source.getApellidos());

		if (	(source.getIds() != null) && 
				(!source.getIds().isEmpty())) {
			Set<PubValuesSubentity> items = source.
					getIds().
					entrySet().
					stream().
					map(item -> new PubValuesSubentity(
							item.getKey(), 
							item.getValue())).
					distinct().
					collect(Collectors.toSet());
			destination.mergeIdentifiers(items);
		}

		return destination;
		
	}

	private PubCenterEntity buildEntity(Centro source, PubCenterEntity destination) {
		
		if (	(source == null) || 
				(destination == null)) return null;

		if (StringUtils.isNotBlank(source.getTipo())) destination.setCentretype(source.getTipo());
		if (StringUtils.isNotBlank(source.getNombre())) destination.setName(getCentreName(source.getNombre()));

		if (	(source.getIds() != null) && 
				(!source.getIds().isEmpty())) {
			Set<PubValuesSubentity> items = source.
					getIds().
					entrySet().
					stream().
					map(item -> new PubValuesSubentity(
							item.getKey(), 
							item.getValue())).
					distinct().
					collect(Collectors.toSet());
			destination.mergeIdentifiers(items);
		}

		return destination;
		
	}

	private PubFileEntity buildEntity(Fichero source, PubFileEntity destination) {
		
		if (	(source == null) || 
				(destination == null)) return null;

		if (StringUtils.isNotBlank(source.getTipo())) destination.setSource(source.getTipo());
		if (StringUtils.isNotBlank(source.getNombre())) destination.setFileName(source.getNombre());
		if (StringUtils.isNotBlank(source.getGzDirectorio())) destination.setGzDirectory(source.getGzDirectorio());
		if (StringUtils.isNotBlank(source.getGzFichero())) destination.setGzFileName(source.getGzFichero());
		if (StringUtils.isNotBlank(source.getMd5Fichero())) destination.setMd5FileName(source.getMd5Fichero());
		if (StringUtils.isNotBlank(source.getUncompressFichero())) destination.setUncompressedFileName(source.getUncompressFichero());
		if (source.getGzInstante() != null) destination.setGzTimeStamp(source.getGzInstante());
		if (source.isProcesoArticulosCompletado()) destination.setStatus(PubFileEntity.PROCESSED);
		destination.setGzSize(source.getGzTamanio());
		destination.setNumArticlesProcessed(source.getNumArticlesProcessed());
		destination.setNumArticlesTotal(source.getNumArticlesTotal());
		
		return destination;
		
	}

	private PubPublicationEntity buildEntity(Revista source, PubPublicationEntity destination) {
		
		if (	(source == null) || 
				(destination == null)) return null;
		
		destination.setType(PubPublicationEntity.JOURNAL_TYPE);
		if (StringUtils.isNotBlank(source.getMedio())) destination.setMedia(getMediaField(source.getMedio()));
		if (StringUtils.isNotBlank(source.getAbreviatura())) destination.setAbbreviation(source.getAbreviatura());
		if (StringUtils.isNotBlank(source.getPais())) destination.setCountry(source.getPais());
		if (StringUtils.isNotBlank(source.getTipo())) destination.setType(source.getTipo());
		if (StringUtils.isNotBlank(source.getNombre())) destination.setTitle(source.getNombre());

		if (	(source.getIds() != null) && 
				(!source.getIds().isEmpty())) {
			Set<PubValuesSubentity> items = source.
					getIds().
					entrySet().
					stream().
					map(item -> new PubValuesSubentity(
							item.getKey(), 
							item.getValue())).
					distinct().
					collect(Collectors.toSet());
			destination.mergeIdentifiers(items);
		}

		return destination;
		
	}

	private PubTermEntity buildEntity(Termino source, PubTermEntity destination) {
		
		if (	(source == null) || 
				(destination == null)) return null;
		
		if (source.getProvider() != null) destination.setProvider(TERM_PRVDR.get(source.getProvider()));
		if (source.getTermtype() != null) destination.setTermtype(TERM_TERM.get(source.getTermtype()));
		if (source.getDesctype() != null) destination.setDesctype(TERM_DESC.get(source.getDesctype()));
		if (StringUtils.isNotBlank(source.getCode())) destination.setCode(source.getCode());
		if (StringUtils.isNotBlank(source.getValue())) destination.setValue(source.getValue());
		
		return destination;
		
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// RELACIONES
	private PubArticleAuthorEntity buildRelation(
			Autor source, 
			PubArticleEntity sourceArticleDB,
			PubAuthorEntity sourceAuthorDB,
			PubCenterEntity sourceCentreDB,
			PubPublicationEntity sourcePublicationDB, 
			PubArticleAuthorEntity destination) {

		if (	(source == null) || 
				(sourceArticleDB == null) || 
				(sourceAuthorDB == null) || 
				//(sourceCentreDB == null) || 
				//(sourcePublicationDB == null) || 
				(destination == null)) return null;
		
		destination.setArticle(sourceArticleDB);
		destination.setAuthor(sourceAuthorDB);
		destination.setCentre(sourceCentreDB);
		destination.setPublication(sourcePublicationDB);
		if (StringUtils.isNotBlank(source.getTipo())) destination.setType(source.getTipo());

		return destination;
		
	}

	private PubBlockSubentity buildRelation(
			BloqueAnotado source, 
			PubArticleEntity sourceArticleDB, 
			PubBlockSubentity destination) {
		
		if (	(source == null) || 
				//(sourceArticleDB == null) || 
				(destination == null)) return null;
		
		if (sourceArticleDB != null) destination.setArticle(sourceArticleDB);
		if (StringUtils.isNotBlank(source.getType())) destination.setBlockType(source.getType());
		if (StringUtils.isNotBlank(source.getText())) destination.setText(source.getText());
		if (source.getOffset() != null) destination.setOffset(source.getOffset());
		
		return destination;
	}

	private PubArticlePublicationEntity buildRelation(
			Fasciculo source,
			PubArticleEntity sourceArticleDB, 
			PubPublicationEntity sourcePublicationDB, 
			PubArticlePublicationEntity destination) {

		if (	(source == null) || 
				(sourceArticleDB == null) || 
				(sourcePublicationDB == null) || 
				(destination == null)) return null;
		
		destination.setArticle(sourceArticleDB);
		destination.setPublication(sourcePublicationDB);

		if (StringUtils.isNotBlank(source.getMedio())) destination.setMedia(source.getMedio());
		if (StringUtils.isNotBlank(source.getNumero())) destination.setNumber(source.getNumero());
		if (StringUtils.isNotBlank(source.getTipo())) destination.setType(source.getTipo());
		if (StringUtils.isNotBlank(source.getVolumen())) destination.setVolume(source.getVolumen());
		if (source.getFecha() != null) {
			destination.setDate(source.getFecha().getFecha());
			try {
				destination.setYear(Integer.parseInt(source.getFecha().getAnio()));
			} catch (Exception ex) {}
			destination.setYearSesion(source.getFecha().getSesion());
		}
		
		return destination;
		
	}

	private PubArticleFileEntity buildRelation(
			Fichero source,
			PubArticleEntity sourceArticleDB, 
			PubFileEntity sourceFileDB, 
			PubArticleFileEntity destination) {

		if (	(source == null) || 
				(sourceArticleDB == null) || 
				(sourceFileDB == null) || 
				(destination == null)) return null;
		
		destination.setArticle(sourceArticleDB);
		destination.setFile(sourceFileDB);
		
		return destination;
		
	}
	
	private PubArticlePublicationEntity buildRelation(
			List<Localizacion> sources,
			PubArticlePublicationEntity destination) {

		if (	(sources == null) || 
				(sources.isEmpty()) || 
				(destination == null)) return null;
	
		Set<PubValuesSubentity> items = new HashSet<>();
		for (Localizacion source: sources) {
			if (source != null) {
				if (Localizacion.PAGINA.equals(source.getTipo())) {
					if (StringUtils.isNotBlank(source.getPaginaInicial())) destination.setStartPage(source.getPaginaInicial());
					if (StringUtils.isNotBlank(source.getPaginaFinal())) destination.setEndPage(source.getPaginaFinal());
					if (StringUtils.isNotBlank(source.getReferencia())) destination.setReference(source.getReferencia());
				} else {
					items.add(new PubValuesSubentity(source.getTipo(), source.getPath()));
				}
			}
		}
		if (!items.isEmpty()) {
			destination.setLocations(items);
		}
		
		return destination;
		
	}

	private PubReferenceSubentity buildRelation(
			Referencia source, 
			PubArticleEntity sourceArticleDB, 
			PubReferenceSubentity destination) {
		
		if (	(source == null) || 
				//(sourceArticleDB == null) || 
				(destination == null)) return null;
		
		if (sourceArticleDB != null) destination.setArticle(sourceArticleDB);
		if (StringUtils.isNotBlank(source.getCita())) destination.setReference(source.getCita());
		
		return destination;
		
	}

	private PubArticleTermEntity buildRelation(
			Termino source,
			PubArticleEntity sourceArticleDB, 
			PubTermEntity sourceTermDB, 
			PubArticleTermEntity destination) {

		if (	(source == null) || 
				(sourceArticleDB == null) || 
				(sourceTermDB == null) || 
				(destination == null)) return null;
		
		destination.setArticle(sourceArticleDB);
		destination.setTerm(sourceTermDB);
		
		return destination;
		
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// PREPARACION DE DATOS
	private Set<PubArticleAuthorEntity> prepareArticleAuthors(PubArticleEntity articleDB, PubPublicationEntity pubDB, List<Autor> autores) {

		if (	(autores == null) || 
				(autores.isEmpty())) return null;

		Set<PubArticleAuthorEntity> articuloAutores = new HashSet<>();
		for (Autor autor: autores) {
			PubAuthorEntity authorDB = updateDB(autor);
			if ((autor.getCentros() != null) && !autor.getCentros().isEmpty()) {
				for (Centro centro: autor.getCentros()) {
					PubCenterEntity centreDB = updateDB(centro);
					if ((centreDB != null) && (articleDB != null)) {
						PubArticleAuthorEntity articuloAutor = buildRelation(
								autor, articleDB, authorDB, centreDB, pubDB,
								new PubArticleAuthorEntity());
						articuloAutores.add(articuloAutor);
					}
				}
			} else {
				PubArticleAuthorEntity articuloAutor = buildRelation(
						autor, articleDB, authorDB, null, pubDB,
						new PubArticleAuthorEntity());
				articuloAutores.add(articuloAutor);
			}
		}

		return articuloAutores;

	}

	private boolean prepareArticleBlocks(PubArticleEntity articleDB, Articulo obj) {
		
		if (	(obj == null) ||
				(articleDB.getId() == null) ||
				(obj.getBlocks() == null) || 
				(obj.getBlocks().isEmpty())) return false;

		if (articleDB.getId() != null) {
			System.out.println ("DEBUG articulo encontrado. Debiera cambiar la version");
		}
		// METODO 1: Al ser 1aN en LAZY debemos actualizar aquí
		Set<PubBlockSubentity> items =  obj.
				getBlocks().
				stream().
				map(item -> buildRelation(item, articleDB, new PubBlockSubentity())).
				distinct().
				collect(Collectors.toSet());

		// Actualizamos solo los enlaces no existentes
		boolean result = false;
		if (!items.isEmpty()) items.forEach(db -> {
			
			Optional<PubBlockSubentity> searchedDB = searchBlockInDB(
					db.getArticle().getId(), 
					db.getId());
			
			boolean update = false;
			try {

				// TODO MAL
				if (searchedDB.isPresent()) {
					PubBlockSubentity olddb = searchedDB.get();
					if (db.getId() == null) db.setId(olddb.getId());
					update = anyDifferences(olddb, db);
				} else {
					update = true;
				}

				if (update) {
					db = blockRep.save(db);
				}
			} catch (Exception ex) {
				LOG.warn("updateDB-ARTICLE_BLOCK" + 
							"\r\n\t" + db.toString() + 
							"\r\n\t" + ex.getMessage());
			}
		});

		return result;
		
	}

	private boolean prepareArticleFiles(PubArticleEntity articleDB, Articulo obj) {
		
		if (	(obj == null) ||
				(articleDB.getId() == null) ||
				(obj.getFicheroPubmed() == null) || 
				(obj.getFicheroPubmed().getEntidad() == null) ||
				(obj.getFicheroPubmed().getEntidad().getId() == null)) return false;
		
		// METODO 1: Hacemos la búsqueda de ficheros por ser NaN. No toca articulos por eso devuelve siempre false
		boolean result = false;
		
		Optional<PubArticleFileEntity> searchedDB = searchArticleFileInDB (
				articleDB.getId(), 
				obj.getFicheroPubmed().getEntidad().getId());
		
		if (!searchedDB.isPresent()) {
			PubArticleFileEntity db = new PubArticleFileEntity();
			db = buildRelation(obj.getFicheroPubmed(), articleDB, obj.getFicheroPubmed().getEntidad(), db);
			try {
				db = articleFileRep.save(db);
			} catch (Exception ex) {
				LOG.warn("updateDB-ARTICLE_FILES" + 
							"\r\n\t" + db.toString() + 
							"\r\n\t" + ex.getMessage());
				db = null;
			}
		}

		return result;

	}
	
	private boolean prepareArticlePublications(PubArticleEntity articleDB, Articulo obj) {
		
		if (	(obj == null) || (
				(obj.getRevista() == null) &&
				(obj.getLibro() == null))) return false;
		
		Set<PubArticleAuthorEntity> articuloAutores = new HashSet<>();
		if (obj.getLibro() != null) {
			PubPublicationEntity bookDB = updateDB(obj.getLibro());
			updateDB(obj, articleDB, bookDB);
			Set<PubArticleAuthorEntity> items = prepareArticleAuthors(articleDB, bookDB, obj.getLibro().getAutores());
			if ((items != null) && (!items.isEmpty())) articuloAutores.addAll(items);
		}
		if (obj.getRevista() != null) {
			PubPublicationEntity journalDB = updateDB(obj.getRevista());
			updateDB(obj, articleDB, journalDB);
			Set<PubArticleAuthorEntity> items = prepareArticleAuthors(articleDB, journalDB, obj.getAutores());
			if ((items != null) && (!items.isEmpty())) articuloAutores.addAll(items);
		}
		
		// Actualizamos solo los enlaces no existentes
		boolean result = false;
		if (!articuloAutores.isEmpty()) articuloAutores.forEach(db -> {
			
			Optional<PubArticleAuthorEntity> searchedDB = searchArticleAuthorInDB(
					db.getKey().getArticleId(), 
					db.getKey().getAuthorId(), 
					db.getKey().getCentreId(), 
					db.getKey().getPublicationId());
			
			boolean update = false;
			try {

				// TODO MAL
				if (searchedDB.isPresent()) {
					PubArticleAuthorEntity olddb = searchedDB.get();
					if (db.getId() == null) db.setId(olddb.getId());
					update = anyDifferences(olddb, db);
				} else {
					update = true;
				}
			
				if (update) {
					db = articleAuthorRep.save(db);
				}
			} catch (Exception ex) {
				LOG.warn("updateDB-ARTICLE_AUTHORS" + 
							"\r\n\t" + db.toString() + 
							"\r\n\t" + ex.getMessage());
			}
				
		});

		// No se requiere actualizar el articulo al ser una relación NaN
		return result;
		
	}
	
	private boolean prepareArticleReferences(PubArticleEntity articleDB, Articulo obj) {
		
		if (	(obj == null) ||
				//(articleDB.getId() == null) ||
				(obj.getReferencias() == null) || 
				(obj.getReferencias().isEmpty())) return false;

		if (articleDB.getId() != null) {
			System.out.println ("DEBUG articulo encontrado. Debiera cambiar la version");
		}

		// METODO 2: Hacemos un merge por ser 1aN EAGER
		Set<PubReferenceSubentity> references =  obj.
			getReferencias().
			stream().
			map(instance -> {
				PubReferenceSubentity relation = buildRelation(instance, articleDB, new PubReferenceSubentity());
				if ((instance.getIds() != null) && !instance.getIds().isEmpty()) {
					Set<PubValuesSubentity> items = instance.
							getIds().
							entrySet().
							stream().
							map(item -> new PubValuesSubentity(
									item.getKey(), 
									item.getValue())).
							distinct().
							collect(Collectors.toSet());
					relation.mergeIdentifiers(items);
				}
				
				return relation;
			}).
			distinct().
			collect(Collectors.toSet());

		boolean result = articleDB.mergeReferences(references);
		return result;
		
	}
	
	private boolean prepareArticleTerms(PubArticleEntity articleDB, Articulo obj) {
		
		if (	(obj == null) ||
				(articleDB == null) || 
				//(articleDB.getId() == null) ||
				(obj.getTerminos() == null) || 
				(obj.getTerminos().isEmpty())) return false;

		// Hacemos la búsqueda de terminos por ser NaN
		Set<PubArticleTermEntity> articuloTerminos = new HashSet<PubArticleTermEntity>();
		for (Termino termino: obj.getTerminos()) {
			
			PubTermEntity termDB = updateDB(termino, null);
			if ((termDB != null) && (articleDB != null)) {
				PubArticleTermEntity artTermDB = buildRelation (termino, articleDB, termDB, new PubArticleTermEntity());
				articuloTerminos.add(artTermDB);
				if (StringUtils.isNotBlank(termino.getData())) {
					artTermDB.setData(termino.getData());
				}
				articuloTerminos.add(artTermDB);
			}
			
			if ((termino.getSubterminos() != null) && !termino.getSubterminos().isEmpty()) {
				for (Termino cualificador: termino.getSubterminos()){
					PubTermEntity cualificadorDB = updateDB(cualificador, termDB);
					if ((cualificadorDB != null) && (articleDB != null)) {
						PubArticleTermEntity artCualDB = buildRelation (cualificador, articleDB, cualificadorDB, new PubArticleTermEntity());
						articuloTerminos.add(artCualDB);
					}
				}
			}

		}
		
		// Actualizamos solo los enlaces no existentes
		boolean result = false;
		if (!articuloTerminos.isEmpty()) articuloTerminos.forEach(db -> {
			
			Optional<PubArticleTermEntity> searchedDB = searchArticleTermInDB(
					db.getId().getArticleId(), 
					db.getId().getTermId());
			
			if (!searchedDB.isPresent()) {
				try {
					db = articleTermRep.save(db);
				} catch (Exception ex) {
					LOG.warn("updateDB-ARTICLE_TERM" + 
								"\r\n\t" + db.toString() + 
								"\r\n\t" + ex.getMessage());
				}
			}
			
		});

		// No se requiere actualizar el articulo al ser una relación NaN
		return result;
		
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// SEARCH
	/**
	 * Busca un articulo en la base de datos. siempre devuelve un objeto
	 * @param obj Datos del articulo
	 * @return Articulo en la base de datos
	 */
	public Optional<PubArticleEntity> searchArticleInDB(Articulo obj) {
		
		if (	(obj == null) || ((
				(obj.getIds() == null) ||
				(obj.getIds().isEmpty())) &&
				(obj.getEntidad() == null) &&
				(StringUtils.isBlank(obj.getPmid()))) ) return Optional.empty();

		Optional<PubArticleEntity> searchedDB = Optional.ofNullable(obj.getEntidad());

		try {
			
			if (	(!searchedDB.isPresent()) && 
					(StringUtils.isNotBlank(obj.getPmid()))) {
				searchedDB = articleRep.findByPmid(obj.getPmid());
			}
	
			if (	(!searchedDB.isPresent()) && 
					(obj.getIds() != null) && 
					(!obj.getIds().isEmpty())) {

				/*
				Session session = entityManager.unwrap(Session.class);
				Query<PubArticleEntity> query = session.createNamedQuery(PubArticleEntity.DB_SEARCH_IDENTIFIERS, PubArticleEntity.class);
				query.setParameterList("ids", obj.getIds());
				searchedDB = query.getSingleResult();
				List<PubArticleEntity> find = articleRep.findByIdentifiers(obj.getIds());
				if ((find != null) && (!find.isEmpty())) {
					searchedDB = Optional.ofNullable(find.get(0));
				}
				*/
				for (Entry<String, String> id: obj.getIds().entrySet()) {
					// TODO Hay articulos con mismos identificadores en otras BBDD. Por ejemplo un articulo con un doi
					// PubMed lo ha convertido en dos, generalmente ocurre en conferencias. En la carga igual hay que quitarlo y solo
					// buscar por el pmid de la linea anterior
					List<PubArticleEntity> find = articleRep.findByIdentifier(id.getKey(), id.getValue());
					if ((find != null) && (!find.isEmpty())) {
						searchedDB = Optional.ofNullable(find.get(0));
						break;
					}
				}
				
			}
			
		} catch (Exception ex) {
			LOG.warn("searchArticleInDB" + 
						"\r\n\t" + obj + 
						"\r\n\t" + ex.getMessage());
		}

		return searchedDB;		
		
	}

	private Optional<PubArticleAuthorEntity> searchArticleAuthorInDB(
			Long articleId, Long authorId, Long centreId, Long publicationId) {

		if (	(articleId == null) || 
				//(centreId == null) || 
				//(publicationId == null)  || 
				(authorId == null)) return Optional.empty();
		
		Optional<PubArticleAuthorEntity> searchedDB = null;

		try {
		
			searchedDB = articleAuthorRep.findByKeyWithNulls(
					articleId, 
					authorId,
					centreId,
					publicationId);

		} catch (Exception ex) {
			LOG.warn("searchArticleAuthorInDB" + 
						"\r\n\t ART: " + articleId + 
						"\r\n\t AUTHOR: " + authorId + 
						"\r\n\t CENTRE: " + centreId + 
						"\r\n\t PUBLICATION: " + publicationId + 
						"\r\n\t" + ex.getMessage());
		}

		return searchedDB;
		
	}
	
	private Optional<PubArticleFileEntity> searchArticleFileInDB(
			Long articleId, Long fileId) {
		
		if (	(articleId == null) || 
				(fileId == null)) return Optional.empty();
		
		Optional<PubArticleFileEntity> searchedDB = Optional.empty();

		try {
		
			PubArticleFileKey id = new PubArticleFileKey(
					articleId, 
					fileId);
			searchedDB = articleFileRep.findById(id);

		} catch (Exception ex) {
			LOG.warn("searchArticleFileInDB" + 
						"\r\n\t ART: " + articleId + 
						"\r\n\t FILE: " + fileId + 
						"\r\n\t" + ex.getMessage());
		}

		return searchedDB;
		
	}

	private Optional<PubArticlePublicationEntity> searchArticlePublicationInDB(
			Long articleId, Long publicationId) {

		if (	(articleId == null) || 
				(publicationId == null)) return Optional.empty();
		
		Optional<PubArticlePublicationEntity> searchedDB = null;

		try {
		
			PubArticlePublicationKey id = new PubArticlePublicationKey(
					articleId, 
					publicationId);
		
			searchedDB = articlePublicationRep.findById(id);

		} catch (Exception ex) {
			LOG.warn("searchArticlePublicationInDB" + 
						"\r\n\t ART: " + articleId + 
						"\r\n\t PUB: " + publicationId + 
						"\r\n\t" + ex.getMessage());
		}

		return searchedDB;
		
	}

	private Optional<PubArticleTermEntity> searchArticleTermInDB(
			Long articleId, Long termId) {

		if (	(articleId == null) || 
				(termId == null)) return Optional.empty();
		
		Optional<PubArticleTermEntity> searchedDB = null;

		try {
		
			PubArticleTermKey id = new PubArticleTermKey(
					articleId, 
					termId);
		
			searchedDB = articleTermRep.findById(id);

		} catch (Exception ex) {
			LOG.warn("searchArticleTermInInDb" + 
						"\r\n\t ART: " + articleId + 
						"\r\n\t TERM: " + termId + 
						"\r\n\t" + ex.getMessage());
		}

		return searchedDB;
		
	}
		
	private Optional<PubAuthorEntity> searchAuthorInDB(Autor obj) {
		
		if (	(obj == null) || 
				(obj.getIds() == null) ||
				(obj.getIds().isEmpty())) return Optional.empty();

		Optional<PubAuthorEntity> searchedDB = Optional.empty();

		try {
	
			/*
			Session session = entityManager.unwrap(Session.class);
			Query<PubAuthorEntity> query = session.getNamedQuery(PubAuthorEntity.DB_SEARCH_IDENTIFIERS);
			query.setParameterList("ids", obj.getIds());
			searchedDB = query.getSingleResult();
			List<PubAuthorEntity> find = authorRep.findByIdentifiers(obj.getIds());
			if ((find != null) && (!find.isEmpty())) {
				searchedDB = Optional.ofNullable(find.get(0));
			}
			*/
			for (Entry<String, String> id: obj.getIds().entrySet()) {
				List<PubAuthorEntity> find = authorRep.findByIdentifier(id.getKey(), id.getValue());
				if ((find != null) && (!find.isEmpty())) {
					searchedDB = Optional.ofNullable(find.get(0));
					break;
				}
			}
			
		} catch (Exception ex) {
			LOG.warn("searchAuthorInDB" + 
						"\r\n\t" + obj + 
						"\r\n\t" + ex.getMessage());
			searchedDB = Optional.empty();
		}

		return searchedDB;		
			
	}
	
	/**
	 * Busca un centro en la base de datos. siempre devuelve un objeto
	 * @param obj Datos del centro
	 * @return Centro en la base de datos
	 */
	private Optional<PubCenterEntity> searchCentreInDB(Centro obj) {
		
		if ("Molecular Biology Division, Bhabha Atomic Research Centre, Mumbai 400".indexOf(obj.getNombre())>-1 ) {
			System.out.println("DEBUG");
		}
		if (	(obj == null) || ((
				(obj.getIds() == null) ||
				(obj.getIds().isEmpty())) &&
				(StringUtils.isBlank(obj.getNombre()))) ) return Optional.empty();

		Optional<PubCenterEntity> searchedDB = null;

		try {
			
			if (StringUtils.isNotBlank(obj.getNombre())) {
				searchedDB = centreRep.findByName(getCentreName(obj.getNombre()));
			}
	
			if (	(!searchedDB.isPresent()) && 
					(obj.getIds() != null) && 
					(!obj.getIds().isEmpty())) {
				
				/*
				Session session = entityManager.unwrap(Session.class);
				Query<PubCenterEntity> query = session.getNamedQuery(PubCenterEntity.DB_SEARCH_IDENTIFIERS);
				query.setParameterList("ids", obj.getIds());
				searchedDB = query.getSingleResult();
				List<PubCenterEntity> find = centreRep.findByIdentifiers(obj.getIds());
				if ((find != null) && (!find.isEmpty())) {
					searchedDB = Optional.ofNullable(find.get(0));
				}
				*/
				for (Entry<String, String> id: obj.getIds().entrySet()) {
					List<PubCenterEntity> find = centreRep.findByIdentifier(id.getKey(), id.getValue());
					if ((find != null) && (!find.isEmpty())) {
						searchedDB = Optional.ofNullable(find.get(0));
						break;
					}
				}
			}
			
		} catch (Exception ex) {
			LOG.warn("searchCentreInDB" + 
					"\r\n\t" + obj + 
					"\r\n\t" + ex.getMessage());
			searchedDB = Optional.empty();
		}

		return searchedDB;		

	}

	private Optional<PubBlockSubentity> searchBlockInDB(Long articleId, Long blockId) {

		if (	(articleId == null) || 
				(blockId == null)) return Optional.empty();
		
		Optional<PubBlockSubentity> searchedDB = null;

		try {
		
			searchedDB = blockRep.findById(blockId);
			if (	searchedDB.isPresent() && 
					(searchedDB.get().getArticle() != null) && 
					(searchedDB.get().getArticle().getId() != articleId)) searchedDB = Optional.empty();

		} catch (Exception ex) {
			LOG.warn("searchArticleTermInInDb" + 
						"\r\n\t ART: " + articleId + 
						"\r\n\t BLOCK: " + blockId + 
						"\r\n\t" + ex.getMessage());
		}

		return searchedDB;
		
	}
	
	/**
	 * Busca un libro en la base de datos. Siempre devuelve un objeto
	 * @param obj Datos del libro
	 * @return Libro en la base de datos
	 */
	private Optional<PubPublicationEntity> searchBookInDB(Libro obj) {
		
		if (	(obj == null) || (
//TODO				(StringUtils.isBlank(obj.getTitle()))  &&
				(
				(obj.getIds() == null) ||
				(obj.getIds().isEmpty()))
				)) return Optional.empty();

		Optional<PubPublicationEntity> searchedDB = Optional.empty();

		try {
			
//			if (StringUtils.isNotBlank(obj.getTitle())) {
//				searchedDB = publicationRep.findByName(obj.getTitle());
//			}
			
			if ((obj.getTitulo() != null) && StringUtils.isNotBlank(obj.getTitulo().getTitulo())) {
				searchedDB = publicationRep.findByTypeAndTitle(PubPublicationEntity.BOOK_TYPE, obj.getTitulo().getTitulo());
			}
	
			if (	(!searchedDB.isPresent()) && 
					(obj.getIds() != null) && 
					(!obj.getIds().isEmpty())) {

				/*
				Session session = entityManager.unwrap(Session.class);
				Query<PubPublicationEntity> query = session.getNamedQuery(PubPublicationEntity.DB_SEARCH_IDENTIFIERS);
				query.setParameterList("ids", obj.getIds());
				searchedDB = query.getSingleResult();
				List<PubPublicationEntity> find = publicationRep.findByIdentifiers(obj.getIds());
				if ((find != null) && (!find.isEmpty())) {
					searchedDB = Optional.ofNullable(find.get(0));
				}
				 */
				for (Entry<String, String> id: obj.getIds().entrySet()) {
					List<PubPublicationEntity> find = publicationRep.findByIdentifier(id.getKey(), id.getValue());
					if ((find != null) && (!find.isEmpty())) {
						searchedDB = Optional.ofNullable(find.get(0));
						break;
					}
				}

			}
			
		} catch (Exception ex) {
			LOG.warn("searchBookInDB" + 
					"\r\n\t" + obj + 
					"\r\n\t" + ex.getMessage());
			searchedDB = Optional.empty();
		}

		return searchedDB;		

	}

	// TODO
	/**
	 * Busca un fichero en la base de datos. siempre devuelve un objeto
	 * @param obj Datos del fichero
	 * @return Fichero actualizado si se requiere la descarag 
	 */
	public Optional<PubFileEntity> searchFileInDB(Fichero obj) {
		
		if (	(obj == null) || (
				(obj.getEntidad() == null) && (
				(StringUtils.isBlank(obj.getTipo())) ||
				(StringUtils.isBlank(obj.getNombre())))) ) return Optional.empty();

		Optional<PubFileEntity> searchedDB = Optional.ofNullable(obj.getEntidad());
		
		try {
			
			if (!searchedDB.isPresent()) {

				searchedDB = fileRep.findBySourceAndFileName(
					obj.getTipo(),
					obj.getNombre());

			}
			
		} catch (Exception ex) {
			LOG.warn("searchFicheroInDB" + 
						"\r\n\t" + obj + 
						"\r\n\t" + ex.getMessage());
			searchedDB = Optional.empty();
		}

		return searchedDB;		
		
	}

	/**
	 * Busca una revista en la base de datos. siempre devuelve un objeto
	 * @param obj Datos de la revista
	 * @return Revista en la base de datos
	 */
	private Optional<PubPublicationEntity> searchJournalInDB(Revista obj) {
		
		if (	(obj == null) || ((
				(obj.getIds() == null) ||
				(obj.getIds().isEmpty())) &&
				(StringUtils.isBlank(obj.getNombre()))) ) return Optional.empty();

		Optional<PubPublicationEntity> searchedDB = null;

		try {
			
			if (StringUtils.isNotBlank(obj.getNombre())) {
				searchedDB = publicationRep.findByTypeAndTitle(PubPublicationEntity.JOURNAL_TYPE, obj.getNombre());
			}
	
			if (	(!searchedDB.isPresent()) && 
					(obj.getIds() != null) && 
					(!obj.getIds().isEmpty())) {

				/*
				Session session = entityManager.unwrap(Session.class);
				Query<PubPublicationEntity> query = session.getNamedQuery(PubPublicationEntity.DB_SEARCH_IDENTIFIERS);
				query.setParameterList("ids", obj.getIds());
				searchedDB = query.getSingleResult();
				List<PubPublicationEntity> find = publicationRep.findByIdentifiers(obj.getIds());
				if ((find != null) && (!find.isEmpty())) {
					searchedDB = Optional.ofNullable(find.get(0));
				}
				*/
				for (Entry<String, String> id: obj.getIds().entrySet()) {
					List<PubPublicationEntity> find = publicationRep.findByIdentifier(id.getKey(), id.getValue());
					if ((find != null) && (!find.isEmpty())) {
						searchedDB = Optional.ofNullable(find.get(0));
						break;
					}
				}
				
			}
			
		} catch (Exception ex) {
			LOG.warn("searchJournalInDB" + 
					"\r\n\t" + obj + 
					"\r\n\t" + ex.getMessage());
			searchedDB = Optional.empty();
		}

		return searchedDB;		

	}

	/**
	 * Busca un descriptor en la base de datos. Siempre devuelve un objeto
	 * @param obj Datos del descriptor
	 * @return Descriptor en la base de datos
	 */
	private Optional<PubTermEntity> searchTermInDB(Termino obj) {
		
		if (	(obj == null) ||
				(StringUtils.isBlank(obj.getCode()))) return Optional.empty();
	
		Optional<PubTermEntity> searchedDB = Optional.empty();

		try {
			
			searchedDB = termRep.findByProviderAndTermtypeAndCode(
					TERM_PRVDR.get(ProviderType.MESH), 
					TERM_TERM.get(obj.getTermtype()), 
					obj.getCode());
			
		} catch (Exception ex) {
			LOG.warn("searchTermInDB" + 
					"\r\n\t" + obj + 
					"\r\n\t" + ex.getMessage());
			searchedDB = Optional.empty();
		}

		return searchedDB;	

	}
	
	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// UPDATES
	/**
	 * Actualiza la informacion de bases de datos de un articulo
	 * @param obj Articulo con los datos 
	 * @return Articulo actualizado con las operaciones de actualizacion de base de datos corregidas
	 */
	public Articulo updateDB(Articulo obj) {
		
		if (obj == null) return null;

		NUMBER++;
		if (NUMBER % 100 == 0) {
			Duration duration = Duration.between(LocalTime.now(), START_DATETIME);
			STATS.add(duration);
			START_DATETIME = LocalTime.now();
		}
		
		if (	("31902960".equals(obj.getPmid())) || (
				(obj.getEntidad() != null) &&
				(obj.getEntidad().getId() == 755))) {
			System.out.println("DEBUG Revisar ENCODING Chino");
		}
		PubArticleEntity articleDB = null;
		Optional<PubArticleEntity> searchedDB = searchArticleInDB(obj);
		boolean update = false;
		if (searchedDB.isPresent()) {
			articleDB = searchedDB.get();
		} else {
			update = true;
			articleDB = new PubArticleEntity();
		}

		// VERSION
		if (StringUtils.isNotBlank(obj.getVersion())) {
			try {
				Integer versionId = Integer.parseInt(obj.getVersion());
				if ((articleDB.getVersionId() != null) && (versionId < articleDB.getVersionId())) {
					obj.setEntidad(articleDB);
					return obj;
				} else {
					articleDB.setVersionId(versionId);
				}
			} catch (Exception ex) {
			}
		}
		if (obj.getVersionFecha() != null) {
			LocalDate versionDate = obj.getVersionFecha();
			if ((articleDB.getVersionDate() != null) && (versionDate.isBefore(articleDB.getVersionDate()))) {
				obj.setEntidad(articleDB);
				return obj;
			} else {
				articleDB.setVersionDate(versionDate);
			}
		}
		
		// UPDATE
		try {
			update |= anyDifferences(obj, articleDB);

			// IDENTIFICADORES embedded
			if ((obj.getIds() != null) && !obj.getIds().isEmpty()) {
				Set<PubValuesSubentity> items = obj.
						getIds().
						entrySet().
						stream().
						map(item -> new PubValuesSubentity(
								item.getKey(), 
								item.getValue())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeIdentifiers(items);
			}

			// DATOS ADICIONALES embedded
			if ((obj.getDatos() != null) && !obj.getDatos().isEmpty()) {
				Set<PubKeywordSubentity> items = obj.
						getDatos().
						stream().
						map(item -> new PubKeywordSubentity(
								DATA,
								item.getPropietario(),
								item.getDescriptor())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeKeywords(items);
			}
			
			// GENES embedded
			if ((obj.getGenes() != null) && !obj.getGenes().isEmpty()) {
				Set<PubKeywordSubentity> items = obj.
						getGenes().
						stream().
						map(item -> new PubKeywordSubentity(
								GEN,
								item.getPropietario(),
								item.getDescriptor())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeKeywords(items);
			}
			
			// TEXTOS
			if ((obj.getTextos() != null) && !obj.getTextos().isEmpty()) {
				Set<PubTextSubentity> items = obj.
						getTextos().
						stream().
						map(item -> new PubTextSubentity(
								item.getTipo(),
								item.getSubtipo(),
								item.getIdioma(),
								item.getCopyright(),
								item.getOrder(),
								item.getEtiqueta(),
								item.getCategoria(),
								item.getTexto())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeTextos(items);
			}
		
			/*
			Articulo.CONTENT_TYPE = Articulo.ABSTRACT_TYPE
			Articulo.COPYRIGHT
			Articulo.CONTENT

			Articulo.CONTENT_TYPE = Articulo.OTHER_TYPE
			Articulo.TYPE
			Articulo.LANGUAGE
			Articulo.COPYRIGHT
			Articulo.CONTENT
				Articulo.LABEL
				Articulo.CATEGORY
				Articulo.TEXT
			if ((obj.getNotas() != null) && !obj.getNotas().isEmpty()) {
				Set<PubKeywordSubentity> items = obj.
						getNotas().
						stream().
						map(item -> new PubKeywordSubentity(
								NOTE,
								item.getPropietario(),
								item.getDescriptor())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeKeywords(items);
			}
			*/
			
			// NOTAS embedded
			if ((obj.getNotas() != null) && !obj.getNotas().isEmpty()) {
				Set<PubKeywordSubentity> items = obj.
						getNotas().
						stream().
						map(item -> new PubKeywordSubentity(
								NOTE,
								item.getPropietario(),
								item.getDescriptor())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeKeywords(items);
			}
			
			// NASA FLIGHTS embedded
			if ((obj.getVuelos() != null) && !obj.getVuelos().isEmpty()) {
				Set<PubKeywordSubentity> items = obj.
						getVuelos().
						stream().
						map(item -> new PubKeywordSubentity(
								NASA_FLIGHT,
								item.getPropietario(),
								item.getDescriptor())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeKeywords(items);
			}

			// PALABRAS CLAVES embedded
			if ((obj.getKeywords() != null) && !obj.getKeywords().isEmpty()) {

				Set<PubKeywordSubentity> items = obj.
						getKeywords().
						stream().
						map(item -> new PubKeywordSubentity(
								KEYWORD,
								item.getPropietario(),
								item.getDescriptor())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeKeywords(items);
			}

			// PROPIEDADES
			if ((obj.getProperties() != null) && !obj.getProperties().isEmpty()) {
				Set<PubPropertySubentity> items = obj.
						getProperties().
						entrySet().
						stream().
						flatMap(list -> {
							String object = list.getKey();
							return list.
								getValue().
								entrySet().
								stream().
								map(instance -> new PubPropertySubentity(object, instance.getKey(), instance.getValue()));
						}).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeProperties(items);
			}

			// FECHAS embedded
			if ((obj.getFechas() != null) && !obj.getFechas().isEmpty()) {
				Set<PubDateSubentity> items = obj.
						getFechas().
						stream().
						filter(item -> item.getFecha() != null).
						map(item -> new PubDateSubentity(
								getDateTypeField(item.getTipo()),
								item.getFecha())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeDates(items);
			}

			// TODO ¿Pertenecen al artículo o son del libro?
			// FECHAS
			if ((obj.getLibro()!= null) && (obj.getLibro().getFechas() != null) && !obj.getLibro().getFechas().isEmpty()) {
				Set<PubDateSubentity> items = obj.
						getFechas().
						stream().
						filter(item -> item.getFecha() != null).
						map(item -> new PubDateSubentity(
								getDateTypeField(item.getTipo()),
								item.getFecha())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeDates(items);
			}

			// PERMISOS embedded
			if ((obj.getPermisos() != null) && !obj.getPermisos().isEmpty()) {
				Set<PubPermissionSubentity> items = obj.
						getPermisos().
						stream().
						map(item -> new PubPermissionSubentity(
								item.getPais(),
								item.getAgencia(),
								item.getPermiso(),
								item.getCodigo())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergePermissions(items);
			}

			// REFERENCIAS 1aN
			update |= prepareArticleReferences(articleDB, obj);

			// BLOQUES
			update |= prepareArticleBlocks(articleDB, obj);

			// DATOS
			if (update) {
				articleDB = buildEntity(obj, articleDB);
				articleDB = articleRep.saveAndFlush(articleDB);
				update = false;
			}

			// FICHEROS NaN
			update |= prepareArticleFiles(articleDB, obj);

			// TERMINOS NaN
			update |= prepareArticleTerms(articleDB, obj);

			// AUTORES, CENTROS, REVISTAS Y LIBROS
			update |= prepareArticlePublications(articleDB, obj);

/*				// SET DATA 
				update |= prepareSecciones(articleDB, obj);
	
				// SECCIONES
				update |= prepareSecciones(articleDB, obj);

*/			
			if (update) {
				articleDB = articleRep.saveAndFlush(articleDB);
				update = false;
			}
			
			if (articleDB.getTitle().length()<10) {
				System.out.println("DEBUG titulo corto");
			}

			obj.setHayCambiosEnBD(false);

		} catch (Exception ex) {
			LOG.warn("updateDB-ARTICLE " + 
						"\r\n\t" + obj.getPmid() + 
						"\r\n\t" + obj.toString() + 
						"\r\n\t" + articleDB.toString() + 
						"\r\n\t" + ex.getMessage());
			obj.setHayCambiosEnBD(true);
		}

		obj.setEntidad(articleDB);
		
		return obj;
		
	}
	
	/*
	private PubArticleAuthorEntity updateDB(PubArticleAuthorEntity db) {

		if (db == null) return null;
	
		Optional<PubArticleAuthorEntity> searchedDB = searchArticleAuthorInDB(
				db.getIds().getArticleId(), 
				db.getIds().getAuthorId(), 
				db.getIds().getCentreId(), 
				db.getIds().getPublicationId());
		boolean update = false;
		try {

			if (!searchedDB.isPresent()) {
				update = true;
			}

			if (update) {
				db = articleAuthorRep.save(db);
			}
			
		} catch (Exception ex) {
			LOG.warn("updateDB-ARTICLE_AUTHORS" + 
						"\r\n\t" + db.toString() + 
						"\r\n\t" + ex.getMessage());
			db = null;
		}
		// No se requiere actualizar el articulo al ser una relación NaN
		// result = true;
		
		return db;

	}
	*/
	
	private PubArticlePublicationEntity updateDB(Articulo obj, PubArticleEntity articleDB, PubPublicationEntity publicationDB) {

		if (	(obj == null) ||
				(articleDB.getId() == null) ||
				(publicationDB.getId() == null) ) return null;
		
		Optional<PubArticlePublicationEntity> searchedDB = searchArticlePublicationInDB (
				articleDB.getId(), 
				publicationDB.getId());
		PubArticlePublicationEntity db = null;
		boolean update = false;
		try {

			if (searchedDB.isPresent()) {
				db = searchedDB.get();
				update = anyDifferences(obj.getFasciculo(), db);
			} else {
				db = new PubArticlePublicationEntity();
				update = true;
			}

			if (update) {
				db = buildRelation(obj.getFasciculo(), articleDB, publicationDB, db);
				db = buildRelation(obj.getLocalizaciones(), db);
				db = articlePublicationRep.save(db);
			}
			
		} catch (Exception ex) {
			LOG.warn("updateDB-ARTICLE-PUBLICATION "  + 
					"\r\n\t" + articleDB + 
					"\r\n\t" + publicationDB + 
					"\r\n\t" + obj.getRevista() + 
					"\r\n\t" + ex.getMessage());
			db = null;
		}
		
		return db;
		
		
	}
	
	private PubAuthorEntity updateDB(Autor obj) {

		if (obj == null) return null;

		Optional<PubAuthorEntity> searchedDB = searchAuthorInDB(obj);
		PubAuthorEntity db = null;
		boolean update = false;
		try {

			if (searchedDB.isPresent()) {
				db = searchedDB.get();
				update = anyDifferences(obj, db);
			} else {
				db = new PubAuthorEntity();
				update = true;
			}

			// IDENTIFICADORES
			if ((obj.getIds() != null) && !obj.getIds().isEmpty()) {
				Set<PubValuesSubentity> items = obj.
						getIds().
						entrySet().
						stream().
						map(item -> new PubValuesSubentity(
								item.getKey(), 
								item.getValue())).
						distinct().
						collect(Collectors.toSet());
				update |= db.mergeIdentifiers(items);
			}

			if (update) {
				db = buildEntity(obj, db);
				db = authorRep.save(db);
			}
			
		} catch (Exception ex) {
			LOG.warn("updateDB-AUTHORS"  + 
					"\r\n\t" + obj + 
					"\r\n\t" + ex.getMessage());
			db = null;
		}

		return db;
		
	}

	// No requerido
	protected PubBlockSubentity updateDB(BloqueAnotado obj, PubArticleEntity articleDB) {

		if (obj == null) return null;

		// Falta Search
		// Falta anyDifference
		PubBlockSubentity db = null;
		boolean update = false;
		
		try {
			
			db = new PubBlockSubentity();
			update = true;

			if (update) {
				db = buildRelation(obj, articleDB, db);
				db = blockRep.save(db);
			}
			
		} catch (Exception ex) {
			LOG.warn("updateDB-BLOCK" + 
					"\r\n\t" + obj + 
					"\r\n\t" + db.toString() + 
					"\r\n\t" + ex.getMessage());
			db = null;
		}

		return db;
		
	}

	private PubCenterEntity updateDB(Centro obj) {

		if (obj == null) return null;

		Optional<PubCenterEntity> searchedDB = searchCentreInDB(obj);
		PubCenterEntity db = null;
		boolean update = false;
		try {

			if (searchedDB.isPresent()) {
				db = searchedDB.get();
				update = anyDifferences(obj, db);
			} else {
				db = new PubCenterEntity();
				update = true;
			}

			// IDENTIFICADORES
			if ((obj.getIds() != null) && !obj.getIds().isEmpty()) {
				Set<PubValuesSubentity> items = obj.
						getIds().
						entrySet().
						stream().
						map(item -> new PubValuesSubentity(
								item.getKey(), 
								item.getValue())).
						distinct().
						collect(Collectors.toSet());
				update |= db.mergeIdentifiers(items);
			}

			if (update) {
				db = buildEntity(obj, db);
				db = centreRep.save(db);
			}
			
		} catch (Exception ex) {
			LOG.warn("updateDB-CENTRE"  + 
					"\r\n\t" + obj + 
					"\r\n\t" + ex.getMessage());
			db = null;
		}

		return db;
		
	}
	
	/**
	 * Actualiza la informacion de bases de datos de un fichero
	 * @param obj Fichero con los datos 
	 * @return Fichero actualizado con las operaciones de actualizacion de base de datos corregidas
	 */
	public Fichero updateDB(Fichero obj) {

		if (obj == null) return null;

		Optional<PubFileEntity> searchedDB = searchFileInDB(obj);
		PubFileEntity db = null;
		boolean update = false;
		try {

			if (searchedDB.isPresent()) {
				db = searchedDB.get();
				update = anyDifferences(obj, db);
			} else {
				db = new PubFileEntity();
				update = true;
			}

			if (update) {
				db = buildEntity(obj, db);
				db = fileRep.save(db);
			}
			
		} catch (Exception ex) {
			LOG.warn("updateDB-FILE "  + 
					"\r\n\t" + obj + 
					"\r\n\t" + ex.getMessage());
			db = null;
			obj.setHayCambiosEnBD(true);
		}
		
		obj.setEntidad(db);
		return obj;
		
	}

	private PubPublicationEntity updateDB(Libro obj) {

		if (obj == null) return null;

		Optional<PubPublicationEntity> searchedDB = searchBookInDB(obj);
		PubPublicationEntity db = null;
		boolean update = false;
		try {

			if (searchedDB.isPresent()) {
				db = searchedDB.get();
				update = anyDifferences(obj, db);
			} else {
				db = new PubPublicationEntity();
				update = true;
			}

			// IDENTIFICADORES
			if ((obj.getIds() != null) && !obj.getIds().isEmpty()) {
				Set<PubValuesSubentity> items = obj.
						getIds().
						entrySet().
						stream().
						map(item -> new PubValuesSubentity(
								item.getKey(), 
								item.getValue())).
						distinct().
						collect(Collectors.toSet());
				update |= db.mergeIdentifiers(items);
			}

			if (update) {
				db = buildEntity(obj, db);
				db = publicationRep.save(db);
			}
			
		} catch (Exception ex) {
			LOG.warn("updateDB-BOOK"  + 
					"\r\n\t" + obj + 
					"\r\n\t" + ex.getMessage());
			db = null;
		}

		return db;
		
	}
	
	// No requerido
	// 
	protected PubReferenceSubentity updateDB(Referencia obj, PubArticleEntity articleDB) {

		if (obj == null) return null;

		PubReferenceSubentity db = null;
		boolean update = false;
		
		try {
			
			db = new PubReferenceSubentity();
			update = true;

			// IDENTIFICADORES
			if ((obj.getIds() != null) && !obj.getIds().isEmpty()) {
				Set<PubValuesSubentity> items = obj.
						getIds().
						entrySet().
						stream().
						map(item -> new PubValuesSubentity(
								item.getKey(), 
								item.getValue())).
						distinct().
						collect(Collectors.toSet());
				update |= db.mergeIdentifiers(items);
			}

			if (update) {
				db = buildRelation(obj, articleDB, db);
				db = referenceRep.save(db);
			}
			
		} catch (Exception ex) {
			LOG.warn("updateDB-REFERENCE" + 
					"\r\n\t" + obj + 
					"\r\n\t" + db.toString() + 
					"\r\n\t" + ex.getMessage());
			db = null;
		}

		return db;
		
	}

	private PubPublicationEntity updateDB(Revista obj) {

		if (obj == null) return null;

		Optional<PubPublicationEntity> searchedDB = searchJournalInDB(obj);
		PubPublicationEntity db = null;
		boolean update = false;
		try {

			if (searchedDB.isPresent()) {
				db = searchedDB.get();
				update |= anyDifferences(obj, db);
			} else {
				db = new PubPublicationEntity();
				update = true;
			}

			// IDENTIFICADORES
			if ((obj.getIds() != null) && !obj.getIds().isEmpty()) {
				Set<PubValuesSubentity> items = obj.
						getIds().
						entrySet().
						stream().
						map(item -> new PubValuesSubentity(
								item.getKey(), 
								item.getValue())).
						distinct().
						collect(Collectors.toSet());
				update |= db.mergeIdentifiers(items);
			}

			if (update) {
				db = buildEntity(obj, db);
				db = publicationRep.save(db);
			}
			
		} catch (Exception ex) {
			LOG.warn("updateDB-JOURNAL"  + 
					"\r\n\t" + obj + 
					"\r\n\t" + ex.getMessage());
			db = null;
		}

		return db;
		
	}

	/**
	 * Actualiza la informacion de bases de datos de un autor
	 * @param obj Autor con los datos 
	 * @return Autor actualizado con las operaciones de actualizacion de base de datos corregidas
	 */
	@Transactional(
			transactionManager = DbNames.DB_TX,
			propagation = Propagation.REQUIRED)
	public PubTermEntity updateDB(Termino obj, PubTermEntity parent) {
		
		if (obj == null) return null;

		Optional<PubTermEntity> searchedDB = searchTermInDB(obj);
		PubTermEntity db = null;
		boolean update = false;
		try {

			if (searchedDB.isPresent()) {
				db = searchedDB.get();
				update |= anyDifferences(obj, db);
			} else {
				db = new PubTermEntity();
				update = true;
			}
			
			if (update) {
				db = buildEntity(obj, db);
				if (parent != null) db.setParent(parent);
				db = termRep.save(db);
			}
						
		} catch (Exception ex) {
			LOG.warn("updateDB-TERMS" + 
					"\r\n\t" + obj + 
					"\r\n\t" + db.toString() + 
					"\r\n\t" + ex.getMessage());
			db = null;
		}

		return db;
		
	}

	public BloqueAnotado updateDB(BloqueAnotado obj) {
		// TODO Auto-generated method stub
		return null;
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// REPOSITORIOS

	@Autowired
	@Qualifier( value = DbNames.DB_FILE_REP )
	PubFileRepository fileRep;

	@Autowired
	@Qualifier( value = DbNames.DB_AUTHOR_REP )
	PubAuthorRepository authorRep;

	@Autowired
	@Qualifier( value = DbNames.DB_CENTRE_REP )
	PubCentreRepository centreRep;
	
	@Autowired
	@Qualifier( value = DbNames.DB_PUBLICATION_REP )
	PubPublicationRepository publicationRep;

	@Autowired
	@Qualifier( value = DbNames.DB_TERM_REP )
	PubTermRepository termRep;
	
	@Autowired
	@Qualifier( value = DbNames.DB_ARTICLE_REP )
	PubArticleRepository articleRep;

	@Autowired
	@Qualifier( value = DbNames.DB_REFERENCE_REP )
	PubReferenceRepository referenceRep;

	@Autowired
	@Qualifier( value = DbNames.DB_BLOCK_REP )
	PubBlockRepository blockRep;

	@Autowired
	@Qualifier( value = DbNames.DB_ARTICLE_AUTHOR_REP )
	PubArticleAuthorRepository articleAuthorRep;

	@Autowired
	@Qualifier( value = DbNames.DB_ARTICLE_FILE_REP )
	PubArticleFileRepository articleFileRep;

	@Autowired
	@Qualifier( value = DbNames.DB_ARTICLE_TERM_REP )
	PubArticleTermRepository articleTermRep;

	@Autowired
	@Qualifier( value = DbNames.DB_ARTICLE_PUBLI_REP )
	PubArticlePublicationRepository articlePublicationRep;

	@Autowired
	EntityManager entityManager;

}
