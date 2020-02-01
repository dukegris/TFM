package es.rcs.tfm.srv.mapper;

import org.apache.commons.lang.StringUtils;

import es.rcs.tfm.db.model.PubArticleEntity;
import es.rcs.tfm.solr.model.PubArticleIdx;
import es.rcs.tfm.srv.model.Articulo;

public class ArticuloMapper {

	public static PubArticleEntity getEntity(Articulo obj) {
		
		PubArticleEntity db = new PubArticleEntity();

		if (StringUtils.isNotBlank(obj.getPmid()))		db.setPmid(obj.getPmid());
		if (StringUtils.isNotBlank(obj.getResumen()))	db.setSummary(obj.getResumen());
		if (	(obj.getTitulo()) != null &&
				StringUtils.isNotBlank(obj.getTitulo().getTitulo()))
														db.setTitle(obj.getTitulo().getTitulo());

		obj.getAutores();
		obj.getBlocks();
		//obj.getBlocksOfType(type);
		obj.getDatos();
		obj.getDescriptores();
		obj.getEntidad();
		obj.getEstado();
		obj.getFarmacos();
		obj.getFasciculo();
		obj.getFechas();
		obj.getFicheroPmc();
		obj.getFicheroPubmed();
		obj.getGenes();
		obj.getIdioma();
		obj.getIds();
		obj.getKeywords();
		obj.getLibro();
		obj.getLocalizacion();
		obj.getMedio();
		obj.getObservaciones();
		obj.getPermisos();
		obj.getPmid();
		obj.getReferencias();
		obj.getResumen();
		obj.getResumenAlternativo();
		obj.getRevista();
		obj.getSecciones();
		obj.getTerminos();
		obj.getTitulo();
		obj.getTituloOriginal();
		obj.getVersion();
		obj.getVersionFecha();

		return db;
		
	}

	public static PubArticleEntity mergeEntity(PubArticleEntity db, Articulo obj) {

		db = getEntity(obj);
		
		
		return db;
		
	}
	
	public static PubArticleIdx getIndex(Articulo obj) {
		
		PubArticleIdx idx = new PubArticleIdx();

		if (StringUtils.isNotBlank(obj.getPmid()))		idx.setPmid(obj.getPmid());
		if (StringUtils.isNotBlank(obj.getResumen()))	idx.setSummary(obj.getResumen());
		if (	(obj.getTitulo()) != null &&
				StringUtils.isNotBlank(obj.getTitulo().getTitulo()))
														idx.setTitle(obj.getTitulo().getTitulo());

		return idx;
		
	}

	public static Articulo getObject(PubArticleEntity db) {

		if (db == null) return null;
		
		Articulo obj = new Articulo();
		
		obj.setPmid(db.getPmid());
		obj.setResumen(db.getSummary());
		obj.getTitulo().setTitulo(db.getTitle());
		
		return obj;
		
	}
}
