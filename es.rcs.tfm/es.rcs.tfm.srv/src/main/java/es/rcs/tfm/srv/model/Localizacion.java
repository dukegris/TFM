package es.rcs.tfm.srv.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Localizacion {
	
	public static final String PAGINA = "PAGINA";
	
	private String tipo = new String();
	private String path = new String();
	private String paginaInicial = new String();
	private String paginaFinal = new String();
	private String referencia = new String();

	public Localizacion() {
		super();
	}
	public Localizacion(String tipo, String valor) {
		super();
		this.tipo = tipo;
		this.path = valor;
	}
	
	public Localizacion(String paginaInicial, String paginaFinal, String referencia) {
		super();
		this.tipo = PAGINA;
		this.paginaInicial = paginaInicial;
		this.paginaFinal = paginaFinal;
		this.referencia = referencia;
	}
	
}