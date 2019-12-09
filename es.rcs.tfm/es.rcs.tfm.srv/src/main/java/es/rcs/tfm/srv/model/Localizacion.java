package es.rcs.tfm.srv.model;

import org.apache.commons.lang.StringUtils;

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

	public void setPagina(
			String paginaReferencia,
			String paginaInicial,
			String paginaFinal) {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isNotBlank(paginaReferencia)) {
			sb.append(paginaReferencia);
			if (StringUtils.isNotBlank(paginaInicial)) {
				sb.append("; ");
			}
		}
		sb.append(paginaInicial);
		if (StringUtils.isNotBlank(paginaFinal)) {
			sb.append(" - ");
		}
		sb.append(paginaFinal);

		this.tipo = PAGINA;
		this.path = sb.toString();
	}

	public Localizacion() {
		super();
	}
	public Localizacion(String tipo, String valor) {
		super();
		this.tipo = tipo;
		this.path = valor;
	}
	
	
}