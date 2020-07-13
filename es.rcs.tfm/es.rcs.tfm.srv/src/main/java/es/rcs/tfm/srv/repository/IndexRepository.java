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
		if (obj.getTitulo() != null)			result = obj.getTitulo().getTitulo();
		if (StringUtils.isBlank(result))		result = obj.getTituloOriginal();
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
		if (!result && !StringUtils.equals(getResumen(source.getResumen()),		destination.getSummary()))		result = true;
		if (!result && !StringUtils.equals(getTitle(source),					destination.getTitle()))		result = true;
		return result;
	}
	
	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// BUILD ENITIES

	private PubArticleIdx buildEntity(Articulo source, PubArticleIdx destination) {

		if (	(source == null) || 
				(destination == null)) return null;

		if (StringUtils.isNotBlank(source.getPmid())) destination.setPmid(source.getPmid());
		if (StringUtils.isNotBlank(source.getPmid())) destination.setPublicationLanguage(source.getIdioma());

		if (source.getRevista() != null) {
			destination.setPublicationType(PubArticleIdx.JOURNAL_TYPE);
			if (StringUtils.isNotBlank(source.getRevista().getAbreviatura())) destination.setPublicationAbbreviation(source.getRevista().getAbreviatura());
			if (StringUtils.isNotBlank(source.getRevista().getMedio())) destination.setPublicationMedia(source.getRevista().getMedio());
			if (StringUtils.isNotBlank(source.getRevista().getNombre())) destination.setPublicationTitle(source.getRevista().getNombre());
			if (StringUtils.isNotBlank(source.getRevista().getTipo())) destination.setPublicationType(source.getRevista().getTipo());
			if (StringUtils.isNotBlank(source.getRevista().getPais())) destination.setPublicationCountry(source.getRevista().getPais());
			if ((source.getRevista().getIds() != null) && !source.getRevista().getIds().isEmpty()) {
				List<String> list = source.getRevista().getIds().entrySet().stream().map(item -> String.format("%s: %s", item.getKey(), item.getValue())).collect(Collectors.toList());
				destination.setPublicationIdentifiers(list);
			}
		}
		if (source.getFasciculo() != null) {
			if (StringUtils.isNotBlank(source.getFasciculo().getMedio())) destination.setPublicationMedia(source.getFasciculo().getMedio());
			if (StringUtils.isNotBlank(source.getFasciculo().getTipo())) destination.setPublicationMedia(source.getFasciculo().getTipo());
			if (StringUtils.isNotBlank(source.getFasciculo().getNumero())) destination.setPublicationNumber(source.getFasciculo().getNumero());
			if (StringUtils.isNotBlank(source.getFasciculo().getVolumen())) destination.setVolumeTitle(source.getFasciculo().getVolumen());
			if (source.getFasciculo().getFecha() != null) {
				String str = null;
				if (source.getFasciculo().getFecha().getFecha() != null) {
					str = source.getFasciculo().getFecha().getFecha().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL));
				} else {
					str = String.format("%s %s", source.getFasciculo().getFecha().getSesion(), source.getFasciculo().getFecha().getAnio());
				}
				if (StringUtils.isNotBlank(str)) {
					str = String.format("%s: %s", source.getFasciculo().getFecha().getTipo(), str);
					destination.setPublicationDate(str);
				}
			}
		}
		if (source.getLibro() != null) {
			
			destination.setPublicationType(PubArticleIdx.BOOK_TYPE);
			if (StringUtils.isNotBlank(source.getLibro().getMedio())) destination.setPublicationMedia(source.getLibro().getMedio());
			if (StringUtils.isNotBlank(source.getLibro().getVolumen())) destination.setVolumeNumber(source.getLibro().getVolumen());
			if (StringUtils.isNotBlank(source.getLibro().getTituloVolumen())) destination.setVolumeTitle(source.getLibro().getTituloVolumen());
			if (StringUtils.isNotBlank(source.getLibro().getCiudad())) destination.setEditionCity(source.getLibro().getCiudad());
			if (StringUtils.isNotBlank(source.getLibro().getEditor())) destination.setEditionEditor(source.getLibro().getEditor());
			if (StringUtils.isNotBlank(source.getLibro().getEdicion())) destination.setEditionTitle(source.getLibro().getEdicion());
			if ((source.getLibro().getIds() != null) && !source.getLibro().getIds().isEmpty()) {
				List<String> list = source.getLibro().getIds().entrySet().stream().map(item -> String.format("%s: %s", item.getKey(), item.getValue())).collect(Collectors.toList());
				destination.setPublicationIdentifiers(list);
			}
			if ((source.getLibro().getAutores() != null) && !source.getLibro().getAutores().isEmpty()) {
				List<String> list = source.getLibro().getAutores().stream().map(item -> String.format("%s: %s %s %s", item.getTipo(), item.getNombre(), item.getSufijo(), item.getApellidos())).collect(Collectors.toList());
				destination.setAuhors(list);
			}
			String str = (source.getLibro().getTitulo() != null) ? source.getLibro().getTitulo().getTitulo() : "";
			if (StringUtils.isNotBlank(str)) destination.setPublicationTitle(str);

			str = (source.getLibro().getTituloColeccion() != null) ? source.getLibro().getTituloColeccion().getTitulo() : "";
			if (StringUtils.isNotBlank(str)) destination.setSerieTitle(str);

			/*
			if (StringUtils.isNotBlank(source.getPmid())) destination.setSerieNumber(source.getPmid());
			source.getLibro().getFechas();
			source.getLibro().getInforme();
			*/
			
			if (source.getLibro().getLocalizacion() != null) {
				Localizacion loc = source.getLibro().getLocalizacion();
				if (Localizacion.PAGINA.equals(loc.getTipo())) {
					StringBuffer sb = new StringBuffer();
					if (StringUtils.isNotBlank(loc.getPaginaInicial())) sb.append(loc.getPaginaInicial() + " - ");
					if (StringUtils.isNotBlank(loc.getPaginaFinal())) sb.append(loc.getPaginaFinal());
					if (StringUtils.isNotBlank(loc.getReferencia())) sb.append("(" + loc.getReferencia() + ")");
					if (StringUtils.isNotBlank(sb.toString())) destination.setPublicationPages(sb.toString());
				}
			}
		}
		

		String str = null;
		str = getResumen(source.getResumen());
		if (StringUtils.isNotBlank(str)) destination.setSummary(str);
		str = getTitle(source);
		if (StringUtils.isNotBlank(str)) destination.setTitle(str);
		
		if ((source.getAutores() != null) && !source.getAutores().isEmpty()) {
			List<String> list = source.getAutores().stream().map(item -> String.format("%s: %s %s %s", item.getTipo(), item.getNombre(), item.getSufijo(), item.getApellidos())).collect(Collectors.toList());
			destination.setAuhors(list);
		}
		if ((source.getIds() != null) && !source.getIds().isEmpty()) {
			List<String> list = source.getIds().entrySet().stream().map(item -> String.format("%s: %s", item.getKey(), item.getValue())).collect(Collectors.toList());
			destination.setIdentifiers(list);
		}
		if ((source.getPermisos() != null) && !source.getPermisos().isEmpty()) {
			List<String> list = source.getPermisos().stream().map(item -> String.format("%s: %s, %s (%s)", item.getCodigo(), item.getPermiso(), item.getAgencia(), item.getPais())).collect(Collectors.toList());
			destination.setGrants(list);
		}
		if ((source.getLocalizaciones() != null) && !source.getLocalizaciones().isEmpty()) {
			for (Localizacion loc: source.getLocalizaciones()) {
				if (loc != null) {
					if (Localizacion.PAGINA.equals(loc.getTipo())) {
						StringBuffer sb = new StringBuffer();
						if (StringUtils.isNotBlank(loc.getPaginaInicial())) sb.append(loc.getPaginaInicial() + " - ");
						if (StringUtils.isNotBlank(loc.getPaginaFinal())) sb.append(loc.getPaginaFinal());
						if (StringUtils.isNotBlank(loc.getReferencia())) sb.append("(" + loc.getReferencia() + ")");
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

		Optional<PubArticleIdx> searchedIDX = Optional.ofNullable(obj.getIndice());

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
			
			obj.setHayCambiosEnIDX(false);

		} catch (Exception ex) {
			LOG.warn("updateIdx-ARTICLE"  + 
					"\r\n\t" + obj + 
					"\r\n\t" + ex.getMessage());
			idx = null;
			obj.setHayCambiosEnIDX(true);
		}

		obj.setIndice(idx);

		return obj;
		
	}
	
	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// REPOSITORIOS

	@Autowired
	@Qualifier( value = IndexNames.IDX_ALL_ARTICLES_REP )
	PubArticleIdxRepository articleIdxRep;

}
