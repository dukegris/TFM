package es.rcs.tfm.srv.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import es.rcs.tfm.db.model.PubArticleEntity;
import es.rcs.tfm.solr.model.IdxArticleSolr;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Articulo {
	
	public static final String PUBMED_ID_NAME = "pubmed";
	
	private ArrayList<Centro> centros = new ArrayList<Centro>();
	private ArrayList<Autor> autores = new ArrayList<Autor>();
	private ArrayList<String> keywords = new ArrayList<String>();

	private HashMap<String, String> ids = new HashMap<String, String>();
	private HashMap<String, ZonedDateTime> fechasPublicacion = new HashMap<String, ZonedDateTime>();
	private HashMap<String, String> mesh = new HashMap<String, String>();
	private HashMap<String, String> quimicos = new HashMap<String, String>();
	private ArrayList<String> genes = new ArrayList<String>();
	
	private String pmid = new String();
	private String titulo = new String();
	private String resumen = new String();
	private String medio = new String();
	private String idioma = new String();
	private Fasciculo fasciculo = new Fasciculo();;

	private boolean hayCambiosEnBD = false;
	private boolean hayCambiosEnIDX = false;
	private boolean procesarRequerido = false;
	private boolean actualizarIdxRequerido = false;
	private boolean actualizarDbRequerido = false;
	
	private Fichero fichero;
	
	private PubArticleEntity entidad;
	private IdxArticleSolr indice;

	public Articulo(Fichero fichero) {
		super();
		this.fichero = fichero;
	}

	public void addIds(Map<String, String> items) {
		if ((items!= null) && (!items.isEmpty())) this.ids.putAll(items);
		String str = this.ids.get(PUBMED_ID_NAME);
		if (StringUtils.isNotBlank(str)) this.pmid = str;
	}

	public void addAutores(List<Autor> items) {
		if ((items!= null) && (!items.isEmpty())) this.autores.addAll(items);
	}
	
	public void addCentros(List<Centro> items) {
		if ((items!= null) && (!items.isEmpty())) this.centros.addAll(items);
	}
	
}
