package es.rcs.tfm.srv.model;

import java.util.HashMap;
import java.util.Map;

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

	private String type;
	private String name;
	private String value;
	private Map<String, String> cualificadores = new HashMap<String, String>();

	public Termino(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public Termino(String type, String name, String value) {
		super();
		this.type = type;
		this.name = name;
		this.value = value;
	}

	public Termino(String name, String value, Map<String, String> cualificadores) {
		super();
		this.name = name;
		this.value = value;
		addTerms(cualificadores);
	}

	public void addTerms(Map<String, String>  items) {
		if ((items!= null) && (!items.isEmpty())) this.cualificadores.putAll(items);
	}

}
