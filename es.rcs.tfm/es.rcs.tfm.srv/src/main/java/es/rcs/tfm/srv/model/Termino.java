package es.rcs.tfm.srv.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Termino {
	
	public static final String FECHA_CREACION = "PUBLICATION_DATE";
	public static final String FECHA_REVISION = "REVISION_DATE";
	public static final String FECHA_ESTABLECIMIENTO = "EDITION_DATE";

	public static final String NOTA_GENERAL = "NOTA";
	public static final String NOTA_HISTORIA = "HISTORIA";
	public static final String NOTA_ONLINE = "ONLINE";
	public static final String NOTA_MESH = "MESH";
	public static final String NOTA_VERSION = "VERSION";
	
	public enum ProviderType {
		MESH
	}

	public enum TermType {
		DESCRIPTOR,
		QUALIFIER,
		SUPPLEMENTAL,
		PHARMALOGICAL
	}

	// SUPPLEMENTAL
	// Class 1 (Chemical and drug)
	// Class 2 (Protocol)
	// Class 3 (Disease)
	// Class 4 (Organism )
	public enum DescType {
		NONE,
		
		CHEMICAL,
		PROTOCOL,
		DISEASE,
		ORGANISM,

		PUBLICATION,
		ECIN,
		ECOUT
	}
	private static final Map<String, DescType> SUPPLEMENTAL = new HashMap<>();
	static {
		SUPPLEMENTAL.put("PROTOCOL", DescType.PROTOCOL);
		SUPPLEMENTAL.put("DISEASE", DescType.DISEASE);
		SUPPLEMENTAL.put("ORGANISM", DescType.ORGANISM);
	}
	public static final DescType getSupplementalType(String key) {
		if (StringUtils.isBlank(key)) return DescType.NONE;
		DescType value = SUPPLEMENTAL.get(key.toUpperCase());
		if (value == null) value = DescType.NONE;
		return value;
	}

	private ProviderType provider;
	private TermType termtype;
	private DescType desctype;
	private String code;
	private String value;
	private String data;

	private List<Termino> subterminos = new ArrayList<>();

	private List<Fecha> fechas = new ArrayList<>(); // pubmed | medline | entrez
	private List<Descriptor> notas = new ArrayList<>();

	public Termino(TermType termtype, DescType desctype) {
		this.provider = ProviderType.MESH;
		this.termtype = termtype;
		this.desctype = desctype;
	}

	public Termino(TermType termtype, DescType desctype, String code, String value) {
		this.provider = ProviderType.MESH;
		this.termtype = termtype;
		this.desctype = desctype;
		this.code = code;
		this.value = value;
	}

	public Termino(TermType termtype, DescType desctype, String code, String value, String data) {
		this.provider = ProviderType.MESH;
		this.termtype = termtype;
		this.desctype = desctype;
		this.code = code;
		this.value = value;
		this.data = data;
	}

	public Termino(TermType termtype, DescType desctype, String code, String value, List<Termino> subterminos) {
		super();
		this.provider = ProviderType.MESH;
		this.termtype = termtype;
		this.desctype = desctype;
		this.code = code;
		this.value = value;
		addSubterminos(subterminos);
	}
	
	public void addSubterminos(List<Termino>  list) {
		if ((list != null) && (!list.isEmpty())) this.subterminos.addAll(list);
	}

	public void addFecha(Fecha item) {
		if ((item!= null)) this.fechas.add(item);
	}

	public void addNota(Descriptor item) {
		if ((item!= null)) this.notas.add(item);
	}

	public void addNotas(List<Descriptor> items) {
		if ((items!= null) && (!items.isEmpty())) this.notas.addAll(items);
	}


}
