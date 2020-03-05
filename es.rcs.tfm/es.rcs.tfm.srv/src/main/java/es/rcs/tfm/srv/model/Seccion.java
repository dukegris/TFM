package es.rcs.tfm.srv.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Seccion {
	String titulo;
	String etiqueta;
	public Seccion(String titulo, String etiqueta) {
		super();
		this.titulo = titulo;
		this.etiqueta = etiqueta;
	}
}
