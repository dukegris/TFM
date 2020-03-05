package es.rcs.tfm.srv.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Descriptor {
	
	public static final String PUBMED = "PUBMED";

	// se incorporana ademas PUBMED Y NASA_FLIGHT
	private String propietario; // NLM | NLM-AUTO | NASA | PIP | KIE | NOTNLM | HHS
	private String descriptor;

	public Descriptor(String propietario, String descriptor) {
		super();
		this.propietario = propietario;
		this.descriptor = descriptor;
	}
	
}
