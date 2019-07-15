package es.rcs.tfm.srv.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Fasciculo {

	Revista revista = new Revista();
	private String medio;
	private String volumen;
	private String numero;
	private String fecha;
	private String paginaInicial;
	private String paginaFinal;

}
