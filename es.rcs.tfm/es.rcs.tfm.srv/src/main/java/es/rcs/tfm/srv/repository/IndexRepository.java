package es.rcs.tfm.srv.repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
		if (StringUtils.isEmpty(str)) return null;
		return str;
	}

	private static final String getTitle(Articulo obj) {
		String result = "";
		if (obj.getTitulo() != null)			result = obj.getTitulo().getTitulo();
		if (StringUtils.isEmpty(result))		result = obj.getTituloOriginal();
		if (StringUtils.isEmpty(result))		result = "";
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

		String str = null;
		str = getResumen(source.getResumen());
		if (StringUtils.isNotBlank(str)) destination.setSummary(str);
		str = getTitle(source);
		if (StringUtils.isNotBlank(str)) destination.setTitle(str);

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
					(StringUtils.isNotEmpty(obj.getPmid()))) {

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
