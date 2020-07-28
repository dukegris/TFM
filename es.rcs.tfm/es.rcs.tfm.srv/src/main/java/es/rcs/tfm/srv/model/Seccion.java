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
public class Seccion {

	String title;
	String label;

	public Seccion(
			final String title,
			final String label) {
		super();
		this.title = title;
		this.label = label;
	}

}
