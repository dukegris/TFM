package es.rcs.tfm.srv.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
@EqualsAndHashCode(
		callSuper = false)
public class Articulo {

	public static final String SEPARATOR = ";";
	
	public static final String PUBMED_ID_NAME = "pmid";
	public static final String PMC_ID_NAME = "pmc";
	public static final String DOI_ID_NAME = "doi";
	public static final String PII_ID_NAME = "pii";
	public static final String ISBN_ID_NAME = "isbn";
	public static final String ISSN_ID_NAME = "issn";
	public static final String NLM_ID_NAME = "nlm";
	
	public static final String FECHA_REVISION = "REVISION_DATE";
	public static final String FECHA_PUBLICACION = "PUBLICATION_DATE";
	public static final String FECHA_COMPLETA = "COMPLETE_DATE";
	public static final String FECHA_INICIO = "BEGIN_DATE";
	public static final String FECHA_CONTRIBUCION = "CONTRIBUTION_DATE";
	public static final String FECHA_EDICION = "EDITION_DATE";

	public static final String INVESTIGADOR = "investigators";
	public static final String AUTOR = "authors";
	public static final String EDITOR = "editors";

	public static final String DESCRIPTORES_MEDLINE = "MEDLINE_DESC";
	public static final String REVISTA = "REVISTA";
	public static final String LIBRO = "LIBRO";

	public static final String COPYRIGHT = "Copyright";

	private Titulo titulo;
	private String tituloOriginal;
	private String resumen;
	private String resumenAlternativo;
	private String idioma;
	private String version;
	private ZonedDateTime versionFecha;
	private String observaciones;
	private String medio; // Print | Print-Electronic | Electronic | Electronic-Print | Electronic-eCollection
	private String estado; // Completed | In-Process | PubMed-not-MEDLINE | In-Data-Review | Publisher | MEDLINE | OLDMEDLINE
	private Map<String, String> ids = new HashMap<String, String>(); // NASA | KIE | PIP | POP | ARPL | CPC | IND | CPFH | CLML | NRCBL | NLM | QCIM
	private List<Fecha> fechas = new ArrayList<Fecha>(); // pubmed | medline | entrez
	private List<Autor> autores = new ArrayList<Autor>(); // 	investigators | authors | editors
	private Revista revista;
	private Fasciculo fasciculo;
	private Libro libro;
	private List<Seccion> secciones;
	private Localizacion localizacion;

	private List<Permiso> permisos = new ArrayList<Permiso>();
	private List<Referencia> referencias = new ArrayList<Referencia>();
	private List<Termino> terminos = new ArrayList<Termino>();
	private List<Descriptor> descriptores = new ArrayList<Descriptor>();
	private List<String> keywords = new ArrayList<String>();

	private Map<String, String> datos = new HashMap<String, String>();
	
	private List<String> genes = new ArrayList<String>();
	private List<String> farmacos = new ArrayList<String>();
	
	private String pmid = new String();
	private List<BloqueAnotado> blocks = new ArrayList<BloqueAnotado>();
	//private HashMap<String, String> tipos = new HashMap<String, String>(); // NLM's controlled list: MeSH Pubtypes
	//private HashMap<String, String> farmacos = new HashMap<String, String>();	
	//private String cita = new String();

	private boolean hayCambiosEnBD = false;
	private boolean hayCambiosEnIDX = false;
	
	private Fichero ficheroPmc;
	private Fichero ficheroPubmed;
	
	private PubArticleEntity entidad;
	private IdxArticleSolr indice;

	public Articulo() {
		super();
	}

	public void addBlocks(List<BloqueAnotado> items) {
		if ((items!= null) && (!items.isEmpty())) this.blocks.addAll(items);
	}

	public void addBlock(BloqueAnotado value) {
		if (value != null) blocks.add(value);
	}

	public boolean containsBlockOfType(String type) {
		if (StringUtils.isBlank(type)) return false;
		boolean result = false;
		for (BloqueAnotado block: this.blocks) {
			if (type.equals(block.getType())) {
				result = true;
				break;
			}
		}
		return result;
	}

	public BloqueAnotado getBlocksOfType(String type) {
		if (StringUtils.isBlank(type)) return null;
		BloqueAnotado result = null;
		for (BloqueAnotado block: this.blocks) {
			if (type.equals(block.getType())) {
				result = block;
				break;
			}
		}
		return result;
	}

	public void addIds(Map<String, String> items) {
		if ((items!= null) && (!items.isEmpty())) this.ids.putAll(items);
		String str = this.ids.get(PUBMED_ID_NAME);
		if (StringUtils.isNotBlank(str)) this.pmid = str;
	}

	public void addIds(Entry<String, String> item) {
		if (item != null) this.ids.put(item.getKey(), item.getValue());
	}

	public void addIds(List<Entry<String, String>> items) {
		if ((items!= null) && (!items.isEmpty())) {
			items.forEach(entry -> {
				this.ids.put(entry.getKey(), entry.getValue());
			});
		}
		String str = this.ids.get(PUBMED_ID_NAME);
		if (StringUtils.isNotBlank(str)) this.pmid = str;
	}

	public void addAutores(List<Autor> items) {
		if ((items!= null) && (!items.isEmpty())) this.autores.addAll(items);
	}
	
	public void addFechas(List<Fecha> items) {
		if ((items!= null) && (!items.isEmpty())) this.fechas.addAll(items);
	}

	public void addFecha(Fecha item) {
		if ((item!= null)) this.fechas.add(item);
	}

	public void addReferencias(List<Referencia> items) {
		if ((items!= null) && (!items.isEmpty())) this.referencias.addAll(items);
	}

	public void addSecciones(List<Seccion> items) {
		if ((items!= null) && (!items.isEmpty())) this.secciones.addAll(items);
	}

	public void addPermisos(List<Permiso> items) {
		if ((items!= null) && (!items.isEmpty())) this.permisos.addAll(items);
	}

	public void addDatos(Map<String, String> items) {
		if ((items!= null) && (!items.isEmpty())) this.datos.putAll(items);
	}

	/*
	public void addTipos(Map<String, String> items) {
		if ((items!= null) && (!items.isEmpty())) this.tipos.putAll(items);
	}
	 */

	public void addTerminos(List<Termino> list) {
		if ((list!= null) && (!list.isEmpty())) this.terminos.addAll(list);
	}

	public void addFarmacos(List<String> items) {
		if ((items!= null) && (!items.isEmpty())) this.farmacos.addAll(items);
	}

	public void addGenes(List<String> list) {
		if ((list!= null) && (!list.isEmpty())) this.genes.addAll(list);
	}

	public void addDescriptores(List<Descriptor> list) {
		if ((list!= null) && (!list.isEmpty())) this.descriptores.addAll(list);
	}

	public void addObservaciones(String instance) {
		
		if (StringUtils.isNotBlank(instance)) {
			if (this.observaciones == null) {
				this.observaciones = instance;
			} else {
				StringBuffer sb = new StringBuffer();
				sb.append(this.observaciones);
				sb.append(instance);
				this.observaciones = sb.toString();
			}
		}
		
	}
	
	public String makeDescriptor(List<Descriptor> list) {
		
		StringBuffer sb = new StringBuffer();
		for (Descriptor s: list) {
			if (sb.length()>0) sb.append(Articulo.SEPARATOR);
			sb.append(s.getDescriptor());
		}
		String resultado = sb.toString();
		if (StringUtils.isBlank(resultado)) resultado = null;
		return resultado;

	}

	public void addResumen(String instance) {
		
		if (StringUtils.isNotBlank(instance)) {
			if (this.resumen == null) {
				this.resumen = instance;
			} else {
				StringBuffer sb = new StringBuffer();
				sb.append(this.resumen);
				sb.append(instance);
				this.resumen = sb.toString();
			}
		}
		
	}
	
}
