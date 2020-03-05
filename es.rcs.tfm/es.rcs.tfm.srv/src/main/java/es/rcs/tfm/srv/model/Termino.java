package es.rcs.tfm.srv.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Termino {

	public static final String FARMACO = "Chemical and drug";
	public static final String PROTOCOLO = "Protocol";
	public static final String ORGANISMO = "Organism";
	public static final String DOLENCIA = "Disease";
	public static final String PUBLICACION = "PUBLICACION";

	public static final String MESH = "MESH";

	private String provider;
	private String termtype;
	private String code;
	private String value;
	private String data;
	private Map<String, String> cualificadores = new HashMap<String, String>();

	public Termino(String code, String value) {
		this.provider = MESH;
		this.code = code;
		this.value = value;
	}

	public Termino(String termtype, String code, String value) {
		this.provider = MESH;
		this.termtype = termtype;
		this.code = code;
		this.value = value;
	}

	public Termino(String termtype, String code, String value, String data) {
		super();
		this.provider = MESH;
		this.termtype = termtype;
		this.code = code;
		this.value = value;
		this.data = data;
	}

	public Termino(String code, String value, Map<String, String> cualificadores) {
		super();
		this.provider = MESH;
		this.code = code;
		this.value = value;
		addTerms(cualificadores);
	}

	public void addTerms(Map<String, String>  items) {
		if ((items!= null) && (!items.isEmpty())) this.cualificadores.putAll(items);
	}

	public static Termino getTerminoFromData(String data) {
		return new Termino(data, data);
	}

	public static Termino getTerminoFromData(Entry<String, String> data) {
		return new Termino(data.getKey(), data.getValue());
	}

}
