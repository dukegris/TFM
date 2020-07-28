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
import es.rcs.tfm.srv.model.Articulo.IdType;
import es.rcs.tfm.srv.model.Articulo.MediumType;
import es.rcs.tfm.srv.model.Articulo.OwnerType;
import es.rcs.tfm.srv.model.Articulo.PublicationType;
import es.rcs.tfm.srv.model.Articulo.StatusType;
import es.rcs.tfm.srv.model.ArticuloBloque.BlockType;
import es.rcs.tfm.srv.model.ArticuloBloque;
import es.rcs.tfm.srv.model.Autor;
import es.rcs.tfm.srv.model.Autor.AuthorType;
import es.rcs.tfm.srv.model.Centro;
import es.rcs.tfm.srv.model.Fasciculo;
import es.rcs.tfm.srv.model.Fichero;
import es.rcs.tfm.srv.model.Libro;
import es.rcs.tfm.srv.model.Localizacion;
import es.rcs.tfm.srv.model.Localizacion.LocalizationType;
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
	private static final Map<String, String> LANGUAGE = new HashMap<>();
	private static final Map<String, String> CATEGORY= new HashMap<>();

	public static final Map<ProviderType, String> TERM_PRVDR = new HashMap<>();
	public static final Map<TermType, String> TERM_TERM = new HashMap<>();
	public static final Map<DescType, String> TERM_DESC = new HashMap<>();
	static {
		
		// https://www.loc.gov/marc/languages/language_code.html
		LANGUAGE.put(	"ENG",					"en");

		// 	En abstract: NlmCategory (BACKGROUND|OBJECTIVE|METHODS|RESULTS|CONCLUSIONS|UNASSIGNED) #IMPLIED
		CATEGORY.put(	"BACKGROUND",			"BACKGROUND");
		CATEGORY.put(	"OBJECTIVE",			"OBJECTIVE");
		CATEGORY.put(	"METHODS",				"METHODS");
		CATEGORY.put(	"RESULTS",				"RESULTS");
		CATEGORY.put(	"CONCLUSIONS",			"CONCLUSIONS");
		CATEGORY.put(	"UNASSIGNED", 			"UNASSIGNED");

		/*
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
		 */
		
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
	private static final String getLanguageField(String key) {
		return get(LANGUAGE, key);
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
		if (!result && !StringUtils.equals(source.getPmid(), destination.getPmid())) result = true;
		if (!result && !StringUtils.equals(getLanguageField(source.getLanguage()),destination.getLanguage())) result = true;
		if (!result && (source.getOwner() != null) && (destination.getOwner() != null) && (!source.getOwner().toString().equals(destination.getOwner()))) result = true;
		if (!result && (source.getStatus() != null) && (destination.getStatus() != null) && (!source.getStatus().toString().equals(destination.getStatus()))) result = true;
		if (!result && (source.getMedium() != null) && (destination.getMediaType() != null) && (!source.getMedium().toString().equals(destination.getMediaType()))) result = true;
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
		if (!result && !StringUtils.equals(source.getInitials(), destination.getInitials())) result = true;
		if (!result && !StringUtils.equals(source.getGivenName(), destination.getName())) result = true;
		if (!result && !StringUtils.equals(source.getSuffix(), destination.getSuffix())) result = true;
		if (!result && !StringUtils.equals(source.getFamilyName(), destination.getLastName())) result = true;
		
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
		if (!result && !StringUtils.equals(source.getType(), destination.getCentretype())) result = true;
		if (!result && !StringUtils.equals(getCentreName(source.getName()), destination.getName())) result = true;

		return result;
		
	}

	private boolean anyDifferences(Fasciculo source, PubArticlePublicationEntity destination) {
		
		if (source == null) return false;
		if (destination == null) return true;

		boolean result = false;
		if (destination.getId() == null) result = true;
		if (!result && !StringUtils.equals(source.getNumber(), destination.getNumber())) result = true;
		if (!result && !StringUtils.equals(source.getVolume(), destination.getVolume())) result = true;
		if (!result && (source.getMedium() != null) && (destination.getMediaType() != null) && (!source.getMedium().toString().equals(destination.getMediaType()))) result = true;
		if (source.getDate() != null) {
			if (!result && (source.getDate().getDate() != null) && (!source.getDate().getDate().isEqual(destination.getDate()))) result = true;
			try {
				if (!result && (source.getDate().getYear() != null) && (Integer.parseInt(source.getDate().getYear()) != destination.getYear())) result = true;
			} catch (Exception ex) {}
		}

		return result;
		
	}

	private boolean anyDifferences(Fichero source, PubFileEntity destination) {
		
		if (source == null) return false;
		if (destination == null) return true;
		
		boolean result = false;
		if (destination.getId() == null) result = true;
		if (!result && !StringUtils.equals(source.getType(), destination.getSource())) result = true;
		if (!result && !StringUtils.equals(source.getFilename(), destination.getFileName())) result = true;
		if (!result && !StringUtils.equals(source.getGzDirectory(), destination.getGzDirectory())) result = true;
		if (!result && !StringUtils.equals(source.getGzFilename(), destination.getGzFileName())) result = true;
		if (!result && !StringUtils.equals(source.getMd5Filename(), destination.getMd5FileName())) result = true;
		if (!result && !StringUtils.equals(source.getUncompressFilename(), destination.getUncompressedFileName())) result = true;
		if (!result && (source.getGzInstant() != null) && source.getGzInstant().equals(destination.getGzTimeStamp())) result = true;
		if (!result && (source.getNumArticlesProcessed() != destination.getNumArticlesProcessed())) result = true;
		if (!result && (source.getNumArticlesTotal() != destination.getNumArticlesTotal())) result = true;
		if (!result && source.isProcessCompleted() && !PubFileEntity.PROCESSED.equals(destination.getStatus())) result = true;

		return result;
		
	}

	private boolean anyDifferences(Libro source, PubPublicationEntity destination) {
		
		if (source == null) return false;
		if (destination == null) return true;
		
		boolean result = false;
		if (destination.getId() == null) result = true;
		if (!result && !StringUtils.equals(source.getEdition(), destination.getEditionTitle())) result = true;
		if (!result && !StringUtils.equals(source.getEditor(), destination.getEditionEditor())) result = true;
		if (!result && !StringUtils.equals(source.getCity(), destination.getEditionCity())) result = true;
		if (!result && !StringUtils.equals(source.getVolume(), destination.getVolume())) result = true;
		if (!result && !StringUtils.equals(source.getVolumeTitle(), destination.getVolumeTitle())) result = true;
		if (!result && (source.getMedium() != null) && (destination.getMediaType() != null) && (!source.getMedium().toString().equals(destination.getMediaType()))) result = true;
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
		if (!result && !StringUtils.equals(source.getAbbreviation(), destination.getAbbreviation())) result = true;
		if (!result && !StringUtils.equals(source.getCountry(), destination.getCountry())) result = true;
		if (!result && !StringUtils.equals(source.getName(), destination.getTitle())) result = true;
		if (!result && (source.getType() != null) && (destination.getType() != null) && (!source.getType().toString().equals(destination.getType()))) result = true;
		if (!result && (source.getMedium() != null) && (destination.getMediaType() != null) && (!source.getMedium().toString().equals(destination.getMediaType()))) result = true;

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
		
		if (StringUtils.isNotBlank(source.getPmid())) destination.setPmid(source.getPmid());
		if (StringUtils.isNotBlank(source.getLanguage())) destination.setLanguage(getLanguageField(source.getLanguage()));
		if ((source.getOwner() != null) && !(OwnerType.NONE.equals(source.getOwner()))) destination.setOwner(source.getOwner().toString());
		if ((source.getStatus() != null) && !(StatusType.NONE.equals(source.getStatus()))) destination.setStatus(source.getStatus().toString());
		if ((source.getMedium() != null) && !(MediumType.NONE.equals(source.getMedium()))) destination.setMediaType(source.getMedium().toString());

		String str = source.generateTitle();
		if (StringUtils.isNotBlank(str)) destination.setTitle(str);
		
		return destination;
		
	}

	private PubPublicationEntity buildEntity(Libro source, PubPublicationEntity destination) {
		
		if (	(source == null) || 
				(destination == null)) return null;

		destination.setType(PubPublicationEntity.BOOK_TYPE);
		if (StringUtils.isNotBlank(source.getEdition())) destination.setEditionTitle(source.getEdition());
		if (StringUtils.isNotBlank(source.getEditor())) destination.setEditionEditor(source.getEditor());
		if (StringUtils.isNotBlank(source.getCity())) destination.setEditionCity(source.getCity());
		if (StringUtils.isNotBlank(source.getVolume())) destination.setVolume(source.getVolume());
		if (StringUtils.isNotBlank(source.getVolumeTitle())) destination.setVolumeTitle(source.getVolumeTitle());
		if ((source.getMedium() != null) && !(MediumType.NONE.equals(source.getMedium()))) destination.setMediaType(source.getMedium().toString());

		if (source.getTitle() != null) {
			//TODO
			if (StringUtils.isNotBlank(source.getTitle().getBookId())) { 
				System.out.print(" LB-" + source.getTitle().getBookId());
			}
			if (StringUtils.isNotBlank(source.getTitle().getPartId())) { 
				System.out.print(" PB-" + source.getTitle().getPartId());
			}
			if (StringUtils.isNotBlank(source.getTitle().getSectionId())) { 
				System.out.print(" SB-" + source.getTitle().getSectionId());
			}
			if (StringUtils.isNotBlank(source.getTitle().getTitle())) { 
				System.out.println(" TB-" + source.getTitle().getTitle());
			}
		}
		
		if (source.getCollectionTitle() != null) {
			//TODO
			if (StringUtils.isNotBlank(source.getCollectionTitle().getBookId())) { 
				System.out.print(" LC-" + source.getCollectionTitle().getBookId());
			}
			if (StringUtils.isNotBlank(source.getCollectionTitle().getPartId())) { 
				System.out.print(" PC-" + source.getCollectionTitle().getPartId());
			}
			if (StringUtils.isNotBlank(source.getCollectionTitle().getSectionId())) { 
				System.out.print(" SC-" + source.getCollectionTitle().getSectionId());
			}
			if (StringUtils.isNotBlank(source.getCollectionTitle().getTitle())) { 
				System.out.println(" TC-" + source.getCollectionTitle().getTitle());
			}
		}

		if (source.getLocalization() != null) {
			//TODO
			System.out.print("LO");
		}

		return destination;
		
	}

	private PubAuthorEntity buildEntity(Autor source, PubAuthorEntity destination) {
		
		if (	(source == null) || 
				(destination == null)) return null;

		if (StringUtils.isNotBlank(source.getInitials())) destination.setInitials(source.getInitials());
		if (StringUtils.isNotBlank(source.getGivenName())) destination.setName(source.getGivenName());
		if (StringUtils.isNotBlank(source.getSuffix())) destination.setSuffix(source.getSuffix());
		if (StringUtils.isNotBlank(source.getFamilyName())) destination.setLastName(source.getFamilyName());

		if (	(source.getIds() != null) && 
				(!source.getIds().isEmpty())) {
			Set<PubValuesSubentity> items = source.
					getIds().
					entrySet().
					stream().
					map(item -> new PubValuesSubentity(
							item.getKey().toString(), 
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

		if (StringUtils.isNotBlank(source.getType())) destination.setCentretype(source.getType());
		if (StringUtils.isNotBlank(source.getName())) destination.setName(getCentreName(source.getName()));

		if (	(source.getIds() != null) && 
				(!source.getIds().isEmpty())) {
			Set<PubValuesSubentity> items = source.
					getIds().
					entrySet().
					stream().
					map(item -> new PubValuesSubentity(
							item.getKey().toString(), 
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

		if (StringUtils.isNotBlank(source.getType())) destination.setSource(source.getType());
		if (StringUtils.isNotBlank(source.getFilename())) destination.setFileName(source.getFilename());
		if (StringUtils.isNotBlank(source.getGzDirectory())) destination.setGzDirectory(source.getGzDirectory());
		if (StringUtils.isNotBlank(source.getGzFilename())) destination.setGzFileName(source.getGzFilename());
		if (StringUtils.isNotBlank(source.getMd5Filename())) destination.setMd5FileName(source.getMd5Filename());
		if (StringUtils.isNotBlank(source.getUncompressFilename())) destination.setUncompressedFileName(source.getUncompressFilename());
		if (source.getGzInstant() != null) destination.setGzTimeStamp(source.getGzInstant());
		if (source.isProcessCompleted()) destination.setStatus(PubFileEntity.PROCESSED);
		destination.setGzSize(source.getGzSize());
		destination.setNumArticlesProcessed(source.getNumArticlesProcessed());
		destination.setNumArticlesTotal(source.getNumArticlesTotal());
		
		return destination;
		
	}

	private PubPublicationEntity buildEntity(Revista source, PubPublicationEntity destination) {
		
		if (	(source == null) || 
				(destination == null)) return null;
		
		destination.setType(PubPublicationEntity.JOURNAL_TYPE);
		if (StringUtils.isNotBlank(source.getAbbreviation())) destination.setAbbreviation(source.getAbbreviation());
		if (StringUtils.isNotBlank(source.getCountry())) destination.setCountry(source.getCountry());
		if (StringUtils.isNotBlank(source.getName())) destination.setTitle(source.getName());
		if ((source.getType() != null) && !(PublicationType.NONE.equals(source.getType()))) destination.setType(source.getType().toString());
		if ((source.getMedium() != null) && !(MediumType.NONE.equals(source.getMedium()))) destination.setMediaType(source.getMedium().toString());

		if (	(source.getIds() != null) && 
				(!source.getIds().isEmpty())) {
			Set<PubValuesSubentity> items = source.
					getIds().
					entrySet().
					stream().
					map(item -> new PubValuesSubentity(
							item.getKey().toString(), 
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
		if ((source.getType() != null) && !(AuthorType.NONE.equals(source.getType()))) destination.setType(source.getType().toString());

		return destination;
		
	}

	private PubBlockSubentity buildRelation(
			ArticuloBloque source, 
			PubArticleEntity sourceArticleDB, 
			PubBlockSubentity destination) {
		
		if (	(source == null) || 
				//(sourceArticleDB == null) || 
				(destination == null)) return null;
		
		if (sourceArticleDB != null) destination.setArticle(sourceArticleDB);
		if (StringUtils.isNotBlank(source.getText())) destination.setText(source.getText());
		if (source.getOffset() != null) destination.setOffset(source.getOffset());		
		if (	(source.getType() != null) && 
				(!BlockType.NONE.equals(source.getType())) && 
				!(source.getType().toString().equals(destination.getBlockType())))
				destination.setBlockType(source.getType().toString());
		
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

		if (StringUtils.isNotBlank(source.getNumber())) destination.setNumber(source.getNumber());
		if (StringUtils.isNotBlank(source.getVolume())) destination.setVolume(source.getVolume());
		if ((source.getMedium() != null) && !(MediumType.NONE.equals(source.getMedium()))) destination.setMediaType(source.getMedium().toString());
		if (source.getDate() != null) {
			destination.setDate(source.getDate().getDate());
			try {
				destination.setYear(Integer.parseInt(source.getDate().getYear()));
			} catch (Exception ex) {}
			destination.setYearSesion(source.getDate().getSession());
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
				if (LocalizationType.PAGE.equals(source.getType())) {
					if (StringUtils.isNotBlank(source.getInitialPage())) destination.setStartPage(source.getInitialPage());
					if (StringUtils.isNotBlank(source.getEndPage())) destination.setEndPage(source.getEndPage());
					if (StringUtils.isNotBlank(source.getReference())) destination.setReference(source.getReference());
				} else {
					items.add(new PubValuesSubentity(
							source.getType().toString(), 
							source.getPath()));
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
			if ((autor.getCenters() != null) && !autor.getCenters().isEmpty()) {
				for (Centro centro: autor.getCenters()) {
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
				(obj.getPubmedFile() == null) || 
				(obj.getPubmedFile().getEntity() == null) ||
				(obj.getPubmedFile().getEntity().getId() == null)) return false;
		
		// METODO 1: Hacemos la búsqueda de ficheros por ser NaN. No toca articulos por eso devuelve siempre false
		boolean result = false;
		
		Optional<PubArticleFileEntity> searchedDB = searchArticleFileInDB (
				articleDB.getId(), 
				obj.getPubmedFile().getEntity().getId());
		
		if (!searchedDB.isPresent()) {
			PubArticleFileEntity db = new PubArticleFileEntity();
			db = buildRelation(obj.getPubmedFile(), articleDB, obj.getPubmedFile().getEntity(), db);
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
				(obj.getJournal() == null) &&
				(obj.getBook() == null))) return false;
		
		Set<PubArticleAuthorEntity> articuloAutores = new HashSet<>();
		if (obj.getBook() != null) {
			PubPublicationEntity bookDB = updateDB(obj.getBook());
			updateDB(obj, articleDB, bookDB);
			Set<PubArticleAuthorEntity> items = prepareArticleAuthors(articleDB, bookDB, obj.getBook().getAuthors());
			if ((items != null) && (!items.isEmpty())) articuloAutores.addAll(items);
		}
		if (obj.getJournal() != null) {
			PubPublicationEntity journalDB = updateDB(obj.getJournal());
			updateDB(obj, articleDB, journalDB);
			Set<PubArticleAuthorEntity> items = prepareArticleAuthors(articleDB, journalDB, obj.getAuthors());
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
				(obj.getReferences() == null) || 
				(obj.getReferences().isEmpty())) return false;

		if (articleDB.getId() != null) {
			System.out.println ("DEBUG articulo encontrado. Debiera cambiar la version");
		}

		// METODO 2: Hacemos un merge por ser 1aN EAGER
		Set<PubReferenceSubentity> references =  obj.
			getReferences().
			stream().
			map(instance -> {
				PubReferenceSubentity relation = buildRelation(instance, articleDB, new PubReferenceSubentity());
				if ((instance.getIds() != null) && !instance.getIds().isEmpty()) {
					Set<PubValuesSubentity> items = instance.
							getIds().
							entrySet().
							stream().
							map(item -> new PubValuesSubentity(
									item.getKey().toString(), 
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
				(obj.getTerms() == null) || 
				(obj.getTerms().isEmpty())) return false;

		// Hacemos la búsqueda de terminos por ser NaN
		Set<PubArticleTermEntity> articuloTerminos = new HashSet<PubArticleTermEntity>();
		for (Termino termino: obj.getTerms()) {
			
			PubTermEntity termDB = updateDB(termino, null);
			if ((termDB != null) && (articleDB != null)) {
				PubArticleTermEntity artTermDB = buildRelation (termino, articleDB, termDB, new PubArticleTermEntity());
				articuloTerminos.add(artTermDB);
				if (StringUtils.isNotBlank(termino.getData())) {
					artTermDB.setData(termino.getData());
				}
				articuloTerminos.add(artTermDB);
			}
			
			if ((termino.getSubterms() != null) && !termino.getSubterms().isEmpty()) {
				for (Termino cualificador: termino.getSubterms()){
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
				(obj.getEntity() == null) &&
				(StringUtils.isBlank(obj.getPmid()))) ) return Optional.empty();

		Optional<PubArticleEntity> searchedDB = Optional.ofNullable(obj.getEntity());

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
				for (Entry<IdType, String> id: obj.getIds().entrySet()) {
					// TODO Hay articulos con mismos identificadores en otras BBDD. Por ejemplo un articulo con un doi
					// PubMed lo ha convertido en dos, generalmente ocurre en conferencias. En la carga igual hay que quitarlo y solo
					// buscar por el pmid de la linea anterior
					List<PubArticleEntity> find = articleRep.findByIdentifier(id.getKey().toString(), id.getValue());
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
			for (Entry<IdType, String> id: obj.getIds().entrySet()) {
				List<PubAuthorEntity> find = authorRep.findByIdentifier(id.getKey().toString(), id.getValue());
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
		
		if ("Molecular Biology Division, Bhabha Atomic Research Centre, Mumbai 400".indexOf(obj.getName())>-1 ) {
			System.out.println("DEBUG");
		}
		if (	(obj == null) || ((
				(obj.getIds() == null) ||
				(obj.getIds().isEmpty())) &&
				(StringUtils.isBlank(obj.getName()))) ) return Optional.empty();

		Optional<PubCenterEntity> searchedDB = null;

		try {
			
			if (StringUtils.isNotBlank(obj.getName())) {
				searchedDB = centreRep.findByName(getCentreName(obj.getName()));
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
				for (Entry<IdType, String> id: obj.getIds().entrySet()) {
					List<PubCenterEntity> find = centreRep.findByIdentifier(id.getKey().toString(), id.getValue());
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
			
			if ((obj.getTitle() != null) && StringUtils.isNotBlank(obj.getTitle().getTitle())) {
				searchedDB = publicationRep.findByTypeAndTitle(PubPublicationEntity.BOOK_TYPE, obj.getTitle().getTitle());
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
				for (Entry<IdType, String> id: obj.getIds().entrySet()) {
					List<PubPublicationEntity> find = publicationRep.findByIdentifier(id.getKey().toString(), id.getValue());
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
				(obj.getEntity() == null) && (
				(StringUtils.isBlank(obj.getType())) ||
				(StringUtils.isBlank(obj.getFilename())))) ) return Optional.empty();

		Optional<PubFileEntity> searchedDB = Optional.ofNullable(obj.getEntity());
		
		try {
			
			if (!searchedDB.isPresent()) {

				searchedDB = fileRep.findBySourceAndFileName(
					obj.getType(),
					obj.getFilename());

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
				(StringUtils.isBlank(obj.getName()))) ) return Optional.empty();

		Optional<PubPublicationEntity> searchedDB = null;

		try {
			
			if (StringUtils.isNotBlank(obj.getName())) {
				searchedDB = publicationRep.findByTypeAndTitle(PubPublicationEntity.JOURNAL_TYPE, obj.getName());
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
				for (Entry<IdType, String> id: obj.getIds().entrySet()) {
					List<PubPublicationEntity> find = publicationRep.findByIdentifier(id.getKey().toString(), id.getValue());
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
				(obj.getEntity() != null) &&
				(obj.getEntity().getId() == 755))) {
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
		if (StringUtils.isNotBlank(obj.getVersionId())) {
			try {
				Integer versionId = Integer.parseInt(obj.getVersionId());
				if ((articleDB.getVersionId() != null) && (versionId < articleDB.getVersionId())) {
					obj.setEntity(articleDB);
					return obj;
				} else {
					articleDB.setVersionId(versionId);
				}
			} catch (Exception ex) {
			}
		}
		if (obj.getVersionDate() != null) {
			LocalDate versionDate = obj.getVersionDate();
			if ((articleDB.getVersionDate() != null) && (versionDate.isBefore(articleDB.getVersionDate()))) {
				obj.setEntity(articleDB);
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
								item.getKey().toString(), 
								item.getValue())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeIdentifiers(items);
			}

			// DATOS ADICIONALES embedded
			if ((obj.getData() != null) && !obj.getData().isEmpty()) {
				Set<PubKeywordSubentity> items = obj.
						getData().
						stream().
						map(item -> new PubKeywordSubentity(
								DATA,
								item.getOwner().toString(),
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
								item.getOwner().toString(),
								item.getDescriptor())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeKeywords(items);
			}
			
			// TEXTOS
			if ((obj.getTexts() != null) && !obj.getTexts().isEmpty()) {
				Set<PubTextSubentity> items = obj.
						getTexts().
						stream().
						map(item -> new PubTextSubentity(
								item.getType().toString(),
								item.getSubtype(),
								item.getLanguage(),
								item.getCopyright(),
								item.getOrder(),
								item.getLabel(),
								item.getCategory(),
								item.getText())).
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
								item.getOwner(),
								item.getDescriptor())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeKeywords(items);
			}
			*/
			
			// NOTAS embedded
			if ((obj.getNotes() != null) && !obj.getNotes().isEmpty()) {
				Set<PubKeywordSubentity> items = obj.
						getNotes().
						stream().
						map(item -> new PubKeywordSubentity(
								NOTE,
								item.getOwner().toString(),
								item.getDescriptor())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeKeywords(items);
			}
			
			// NASA FLIGHTS embedded
			if ((obj.getFlights() != null) && !obj.getFlights().isEmpty()) {
				Set<PubKeywordSubentity> items = obj.
						getFlights().
						stream().
						map(item -> new PubKeywordSubentity(
								NASA_FLIGHT,
								item.getOwner().toString(),
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
								item.getOwner().toString(),
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
			if ((obj.getDates() != null) && !obj.getDates().isEmpty()) {
				Set<PubDateSubentity> items = obj.
						getDates().
						stream().
						filter(item -> item.getDate() != null).
						map(item -> new PubDateSubentity(
								item.getType().toString(),
								item.getDate())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeDates(items);
			}

			// TODO ¿Pertenecen al artículo o son del libro?
			// FECHAS
			if ((obj.getBook()!= null) && (obj.getBook().getDates() != null) && !obj.getBook().getDates().isEmpty()) {
				Set<PubDateSubentity> items = obj.
						getDates().
						stream().
						filter(item -> item.getDate() != null).
						map(item -> new PubDateSubentity(
								item.getType().toString(),
								item.getDate())).
						distinct().
						collect(Collectors.toSet());
				update |= articleDB.mergeDates(items);
			}

			// PERMISOS embedded
			if ((obj.getGrants() != null) && !obj.getGrants().isEmpty()) {
				Set<PubPermissionSubentity> items = obj.
						getGrants().
						stream().
						map(item -> new PubPermissionSubentity(
								item.getCountry(),
								item.getAgency(),
								item.getGrant(),
								item.getCode())).
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

			obj.setChangesInDb(false);

		} catch (Exception ex) {
			LOG.warn("updateDB-ARTICLE " + 
						"\r\n\t" + obj.getPmid() + 
						"\r\n\t" + obj.toString() + 
						"\r\n\t" + articleDB.toString() + 
						"\r\n\t" + ex.getMessage());
			obj.setChangesInDb(true);
		}

		obj.setEntity(articleDB);
		
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
				update = anyDifferences(obj.getIssue(), db);
			} else {
				db = new PubArticlePublicationEntity();
				update = true;
			}

			if (update) {
				db = buildRelation(obj.getIssue(), articleDB, publicationDB, db);
				db = buildRelation(obj.getLocalizations(), db);
				db = articlePublicationRep.save(db);
			}
			
		} catch (Exception ex) {
			LOG.warn("updateDB-ARTICLE-PUBLICATION "  + 
					"\r\n\t" + articleDB + 
					"\r\n\t" + publicationDB + 
					"\r\n\t" + obj.getJournal() + 
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
								item.getKey().toString(), 
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
	protected PubBlockSubentity updateDB(ArticuloBloque obj, PubArticleEntity articleDB) {

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
								item.getKey().toString(), 
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
			obj.setChangesInDb(true);
		}
		
		obj.setEntity(db);
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
								item.getKey().toString(), 
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
								item.getKey().toString(), 
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
								item.getKey().toString(), 
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

	public ArticuloBloque updateDB(ArticuloBloque obj) {
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
