package es.rcs.tfm.srv.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Permiso {
	
	private String pais;
	private String agencia;
	private String permiso;
	private String codigo;
	
	public Permiso(String pais, String agencia, String permiso, String codigo) {
		super();
		this.pais = pais;
		this.agencia = agencia;
		this.permiso = permiso;
		this.codigo = codigo;
	}

}
