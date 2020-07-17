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
public class Texto {
	private String tipo;
	private String subtipo;
	private String idioma;
	private String copyright;
	private Integer order;
	private String etiqueta;
	private String categoria;
	private String texto;
	public Texto(
			String tipo, 
			String subtipo, 
			String idioma, 
			String copyright, 
			Integer order, 
			String etiqueta,
			String categoria, 
			String texto) {
		super();
		this.tipo = tipo;
		this.subtipo = subtipo;
		this.idioma = idioma;
		this.copyright = copyright;
		this.order = order;
		this.etiqueta = etiqueta;
		this.categoria = categoria;
		this.texto = texto;
	}

}
