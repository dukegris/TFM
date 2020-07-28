package es.rcs.tfm.srv.repository;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import es.rcs.tfm.solr.IndexNames;
import es.rcs.tfm.solr.model.PubArticleIdx;
import es.rcs.tfm.solr.repository.PubArticleIdxRepository;
import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.model.Localizacion;
import es.rcs.tfm.srv.model.Localizacion.LocalizationType;
import es.rcs.tfm.srv.model.Articulo.MediumType;
import es.rcs.tfm.srv.model.Articulo.PublicationType;

@Repository(
		value = SrvNames.INDEX_REP)
public class IndexRepository {

	private static final Logger LOG = LoggerFactory.getLogger(IndexRepository.class);

	private static ObjectMapper mapper = new ObjectMapper();
	public String getResumen(List<Map<String, Object>> list) {
		String str = "";
		try {
			str = mapper.
//					writerWithDefaultPrettyPrinter().
					writeValueAsString(list);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (StringUtils.isBlank(str)) return null;
		return str;
	}

	private static final String getTitle(Articulo obj) {
		String result = "";
		if (obj.getTitle() != null)			result = obj.getTitle().getTitle();
		if (StringUtils.isBlank(result))		result = "";
		return result;
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// DIFERENCIAS

	public boolean anyDifferences(Articulo source, PubArticleIdx destination) {

		if (source == null) return false;
		if (destination == null) return true;

		boolean result = false;
		if (destination.getId() == null)																		result = true;
		if (!result && !StringUtils.equals(source.getPmid(),					destination.getPmid()))			result = true;
		if (!result && !StringUtils.equals(getTitle(source),					destination.getTitle()))		result = true;
		return result;
	}
	
	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// BUILD ENITIES

	private PubArticleIdx buildEntity(Articulo source, PubArticleIdx destination) {

		if (	(source == null) || 
				(destination == null)) return null;

		if (StringUtils.isNotBlank(source.getPmid())) destination.setPmid(source.getPmid());
		if (StringUtils.isNotBlank(source.getPmid())) destination.setPublicationLanguage(source.getLanguage());

		if (source.getJournal() != null) {
			destination.setPublicationType(PubArticleIdx.JOURNAL_TYPE);
			if (StringUtils.isNotBlank(source.getJournal().getAbbreviation())) destination.setPublicationAbbreviation(source.getJournal().getAbbreviation());
			if (StringUtils.isNotBlank(source.getJournal().getName())) destination.setPublicationTitle(source.getJournal().getName());
			if (StringUtils.isNotBlank(source.getJournal().getCountry())) destination.setPublicationCountry(source.getJournal().getCountry());
			if ((source.getJournal().getType() != null) && !(PublicationType.NONE.equals(source.getJournal().getType()))) destination.setPublicationType(source.getJournal().getType().toString());
			if ((source.getJournal().getMedium() != null) && !(MediumType.NONE.equals(source.getJournal().getMedium()))) destination.setPublicationMedia(source.getJournal().getMedium().toString());
			if ((source.getJournal().getIds() != null) && !source.getJournal().getIds().isEmpty()) {
				List<String> list = source.getJournal().getIds().entrySet().stream().map(item -> String.format("%s: %s", item.getKey(), item.getValue())).collect(Collectors.toList());
				destination.setPublicationIdentifiers(list);
			}
		}
		
		if (source.getIssue() != null) {
			if (StringUtils.isNotBlank(source.getIssue().getNumber())) destination.setPublicationNumber(source.getIssue().getNumber());
			if (StringUtils.isNotBlank(source.getIssue().getVolume())) destination.setVolumeTitle(source.getIssue().getVolume());
			if ((source.getIssue().getMedium() != null) && !(MediumType.NONE.equals(source.getIssue().getMedium()))) destination.setPublicationMedia(source.getIssue().getMedium().toString());
			if (source.getIssue().getDate() != null) {
				String str = null;
				if (source.getIssue().getDate().getDate() != null) {
					str = source.getIssue().getDate().getDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL));
				} else {
					str = String.format("%s %s", source.getIssue().getDate().getSession(), source.getIssue().getDate().getYear());
				}
				if (StringUtils.isNotBlank(str)) {
					str = String.format("%s: %s", source.getIssue().getDate().getType(), str);
					destination.setPublicationDate(str);
				}
			}
		}
		if (source.getBook() != null) {
			
			destination.setPublicationType(PubArticleIdx.BOOK_TYPE);
			if (StringUtils.isNotBlank(source.getBook().getVolume())) destination.setVolumeNumber(source.getBook().getVolume());
			if (StringUtils.isNotBlank(source.getBook().getVolumeTitle())) destination.setVolumeTitle(source.getBook().getVolumeTitle());
			if (StringUtils.isNotBlank(source.getBook().getCity())) destination.setEditionCity(source.getBook().getCity());
			if (StringUtils.isNotBlank(source.getBook().getEditor())) destination.setEditionEditor(source.getBook().getEditor());
			if (StringUtils.isNotBlank(source.getBook().getEdition())) destination.setEditionTitle(source.getBook().getEdition());
			if ((source.getBook().getMedium() != null) && !(MediumType.NONE.equals(source.getBook().getMedium()))) destination.setPublicationMedia(source.getBook().getMedium().toString());
			if ((source.getBook().getIds() != null) && !source.getBook().getIds().isEmpty()) {
				List<String> list = source.getBook().getIds().entrySet().stream().map(item -> String.format("%s: %s", item.getKey(), item.getValue())).collect(Collectors.toList());
				destination.setPublicationIdentifiers(list);
			}
			if ((source.getBook().getAuthors() != null) && !source.getBook().getAuthors().isEmpty()) {
				List<String> list = source.getBook().getAuthors().stream().map(item -> String.format("%s: %s %s %s", item.getType(), item.getGivenName(), item.getSuffix(), item.getFamilyName())).collect(Collectors.toList());
				destination.setAuhors(list);
			}
			String str = (source.getBook().getTitle() != null) ? source.getBook().getTitle().getTitle() : "";
			if (StringUtils.isNotBlank(str)) destination.setPublicationTitle(str);

			str = (source.getBook().getCollectionTitle() != null) ? source.getBook().getCollectionTitle().getTitle() : "";
			if (StringUtils.isNotBlank(str)) destination.setSerieTitle(str);

			/*
			if (StringUtils.isNotBlank(source.getPmid())) destination.setSerieNumber(source.getPmid());
			source.getBook().getFechas();
			source.getBook().getInforme();
			*/
			
			if (source.getBook().getLocalization() != null) {
				Localizacion loc = source.getBook().getLocalization();
				if (LocalizationType.PAGE.equals(loc.getType())) {
					StringBuffer sb = new StringBuffer();
					if (StringUtils.isNotBlank(loc.getInitialPage())) sb.append(loc.getInitialPage() + " - ");
					if (StringUtils.isNotBlank(loc.getEndPage())) sb.append(loc.getEndPage());
					if (StringUtils.isNotBlank(loc.getReference())) sb.append("(" + loc.getReference() + ")");
					if (StringUtils.isNotBlank(sb.toString())) destination.setPublicationPages(sb.toString());
				}
			}
		}
		
		String str = getTitle(source);
		if (StringUtils.isNotBlank(str)) destination.setTitle(str);
		
		if ((source.getAuthors() != null) && !source.getAuthors().isEmpty()) {
			List<String> list = source.getAuthors().stream().map(item -> String.format("%s: %s %s %s", item.getType(), item.getGivenName(), item.getSuffix(), item.getFamilyName())).collect(Collectors.toList());
			destination.setAuhors(list);
		}
		if ((source.getTexts() != null) && !source.getTexts().isEmpty()) {
			List<String> list = source.getTexts().stream().map(item -> String.format("%s: %s %s %s", item.getType(), item.getLabel(), item.getCategory(), item.getText() )).collect(Collectors.toList());
			destination.setTexts(list);
		}
		if ((source.getIds() != null) && !source.getIds().isEmpty()) {
			List<String> list = source.getIds().entrySet().stream().map(item -> String.format("%s: %s", item.getKey(), item.getValue())).collect(Collectors.toList());
			destination.setIdentifiers(list);
		}
		if ((source.getGrants() != null) && !source.getGrants().isEmpty()) {
			List<String> list = source.getGrants().stream().map(item -> String.format("%s: %s, %s (%s)", item.getCode(), item.getGrant(), item.getAgency(), item.getCountry())).collect(Collectors.toList());
			destination.setGrants(list);
		}
		if ((source.getLocalizations() != null) && !source.getLocalizations().isEmpty()) {
			for (Localizacion loc: source.getLocalizations()) {
				if (loc != null) {
					if (LocalizationType.PAGE.equals(loc.getType())) {
						StringBuffer sb = new StringBuffer();
						if (StringUtils.isNotBlank(loc.getInitialPage())) sb.append(loc.getInitialPage() + " - ");
						if (StringUtils.isNotBlank(loc.getEndPage())) sb.append(loc.getEndPage());
						if (StringUtils.isNotBlank(loc.getReference())) sb.append("(" + loc.getReference() + ")");
						if (StringUtils.isNotBlank(sb.toString())) destination.setPublicationPages(sb.toString());
					}
				}
			}
		}
		
		return destination;
		
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// SEARCH

	/**
	 * Busca un articulo en el indice. siempre devuelve un objeto
	 * @param obj Datos del articulo
	 * @return Articulo en la base de datos
	 */
	public Optional<PubArticleIdx> searchArticuloInIdx(
			Articulo obj) {

		if (	(obj == null) ) return Optional.empty();

		Optional<PubArticleIdx> searchedIDX = Optional.ofNullable(obj.getIndex());

		try {
			
			if (	(!searchedIDX.isPresent()) && 
					(StringUtils.isNotBlank(obj.getPmid()))) {

				List<PubArticleIdx> idxs = articleIdxRep.findByPmid(obj.getPmid());
				if ((idxs != null) && (!idxs.isEmpty())) {
					searchedIDX = Optional.ofNullable(idxs.get(0));
				}

			}
			
		} catch (Exception ex) {
			LOG.warn("searchArticuloInIdx" + 
					"\r\n\t" + obj + 
					"\r\n\t" + ex.getMessage());
		}

		return searchedIDX;		
		
	}

	/**
	 * Actualiza la informacion del indice de un articulo
	 * @param obj Articulo con los datos 
	 * @return Articulo actualizado con las operaciones de actualizacion del indice
	 */
	public Articulo updateIdx(Articulo obj) {

		if (obj == null) return null;

		Optional<PubArticleIdx> searchedIDX = searchArticuloInIdx(obj);
		PubArticleIdx idx = null;
		boolean update = false;
		try {

			if (searchedIDX.isPresent()) {
				idx = searchedIDX.get();
				update = anyDifferences(obj, idx);
			} else {
				idx = new PubArticleIdx();
				update = true;
			}

			if (update) {
				idx = buildEntity(obj, idx);
				idx = articleIdxRep.save(idx);
			}
			
			obj.setChangesInIdx(false);

		} catch (Exception ex) {
			LOG.warn("updateIdx-ARTICLE"  + 
					"\r\n\t" + obj + 
					"\r\n\t" + ex.getMessage());
			idx = null;
			obj.setChangesInIdx(true);
		}

		obj.setIndex(idx);

		return obj;
		
	}
	
	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// REPOSITORIOS

	@Autowired
	@Qualifier( value = IndexNames.IDX_ALL_ARTICLES_REP )
	PubArticleIdxRepository articleIdxRep;

}
