package es.rcs.tfm.srv.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class Permiso {

	private String country;
	private String agency;
	private String grant;
	private String code;

	public Permiso(
			final String country,
			final String agency,
			final String grant,
			final String code) {
		super();
		this.country = country;
		this.agency = agency;
		this.grant = grant;
		this.code = code;
	}

}
