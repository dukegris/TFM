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
public class PubPropertySubentity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubPropertySubentity.class);

	public static final String RES_OBJECT	= "object";
	public static final String RES_KEY		= "key";
	public static final String RES_VALUE	= "value";

	public static final String DB_OBJECT	= "pro_obj";
	public static final String DB_KEY		= "pro_key";
	public static final String DB_VALUE		= "pro_txt";
	
	@JsonProperty(
			value = RES_OBJECT,
			required = false)
	@Column(
			name = DB_OBJECT, 
			unique = false,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "El identificador del objeto no puede ser nula")
	@Size(
			max = 32, 
			message = "El identificador del objeto no puede sobrepasar los {max} caracteres.")
	private String object;

	
	@JsonProperty(
			value = RES_KEY,
			required = false)
	@Column(
			name = DB_KEY, 
			unique = false,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "La propiedad no puede ser nula")
	@Size(
			max = 32, 
			message = "La propiedad no puede sobrepasar los {max} caracteres.")
	private String key;

	
	@JsonProperty(
			value = RES_VALUE,
			required = false)
	@Column(
			name = DB_VALUE, 
			unique = false,
			nullable = false, 
			length = 256)
	@NotNull(
			message = "El valor no puede ser nulo")
	@Size(
			max = 256, 
			message = "El valor no puede sobrepasar los {max} caracteres.")
	private String value;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public PubPropertySubentity() {
		super();
	}

	public PubPropertySubentity(
			@NotNull(message = "El identificador del objeto no puede ser nula")
			@Size(max = 32, message = "El identificador del objeto no puede sobrepasar los {max} caracteres.")
			String object,
			@NotNull(message = "La propiedad no puede ser nula")
			@Size(max = 32, message = "La propiedad no puede sobrepasar los {max} caracteres.")
			String key,
			@NotNull(message = "El valor no puede ser nulo")
			@Size(max = 256, message = "El valor no puede sobrepasar los {max} caracteres.")
			String value) {
		super();
		this.object = object;
		this.key = key;
		this.value = value;
	}
	

}
