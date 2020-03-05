package es.rcs.tfm.db.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
@Embeddable
public class PubKeywordSubentity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubKeywordSubentity.class);

	public static final String RES_PROVIDER		= "provider";
	public static final String RES_KEY			= "type";
	public static final String RES_VALUE		= "keyword";

	public static final String DB_PROVIDER		= "key_prv";
	public static final String DB_KEY			= "key_key";
	public static final String DB_VALUE			= "key_txt";

	// 	 * NLM | NLM-AUTO | NASA | PIP | KIE | NOTNLM | HHS) "NLM"

	
	@JsonProperty(
			value = RES_PROVIDER,
			required = false)
	@Column(
			name = DB_PROVIDER, 
			unique = false,
			nullable = true, 
			length = 32)
	@Size(
			max = 32, 
			message = "El proveedor no puede sobrepasar los {max} caracteres.")
	private String provider;

	
	@JsonProperty(
			value = RES_KEY,
			required = false)
	@Column(
			name = DB_KEY, 
			unique = false,
			nullable = true, 
			length = 32)
	@Size(
			max = 32, 
			message = "El tipo no puede sobrepasar los {max} caracteres.")
	private String type;

	
	@JsonProperty(
			value = RES_VALUE,
			required = false)
	@Column(
			name = DB_VALUE, 
			unique = false,
			nullable = false, 
			length = 256)
	@NotNull(
			message = "La materia no puede ser nula")
	@Size(
			max = 256, 
			message = "La materia no puede sobrepasar los {max} caracteres.")
	private String value;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public PubKeywordSubentity() {
		super();
	}

	public PubKeywordSubentity(
			@Size(max = 32, message = "El tipo no puede sobrepasar los {max} caracteres.")
			String tipo,
			@Size(max = 32, message = "El proveedor no puede sobrepasar los {max} caracteres.")
			String provider,
			@NotNull(message = "La materia no puede ser nula")
			@Size(max = 256, message = "La materia no puede sobrepasar los {max} caracteres.")
			String materia) {
		super();
		this.provider = provider;
		this.type = tipo;
		this.value = materia;
	}

}
