package es.rcs.tfm.srv.model;

import es.rcs.tfm.srv.model.Articulo.MediumType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class Fasciculo {

	private MediumType medium;
	private String volume;
	private String number;
	private Fecha date;

}
