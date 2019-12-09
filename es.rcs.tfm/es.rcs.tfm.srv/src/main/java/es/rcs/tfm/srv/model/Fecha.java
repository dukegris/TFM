package es.rcs.tfm.srv.model;

import java.time.ZonedDateTime;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Fecha {
	
	private String tipo = new String();
	private String sesion = new String();
	private String anio = new String();
	private ZonedDateTime fecha = null;

	public Fecha(String tipo, ZonedDateTime fecha) {
		super();
		this.tipo = tipo;
		this.fecha = fecha;
	}
	
	public Fecha(String tipo) {
		super();
		this.tipo = tipo;
	}
	
}
