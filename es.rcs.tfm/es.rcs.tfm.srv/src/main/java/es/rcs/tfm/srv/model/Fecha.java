package es.rcs.tfm.srv.model;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Fecha {
	
	private String tipo;
	private String sesion;
	private String anio;

	@Setter(
			value = AccessLevel.NONE)
	private LocalDate fecha = null;

	public Fecha(String tipo, LocalDate fecha) {
		super();
		this.tipo = tipo;
		this.fecha = fecha;
	}
	
	public Fecha(String tipo) {
		super();
		this.tipo = tipo;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
		if (fecha != null) this.anio = Integer.toString(fecha.getYear());
	}
	
	
}
