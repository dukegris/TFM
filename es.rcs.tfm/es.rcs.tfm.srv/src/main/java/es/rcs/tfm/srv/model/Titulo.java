package es.rcs.tfm.srv.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Titulo {
	
	private String titulo;
	private String libroId;
	private String parteId;
	private String seccionId;
	
	public Titulo(String titulo) {
		super();
		this.titulo = titulo;
	}
	
}
