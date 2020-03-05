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
public class PubValuesSubentity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubValuesSubentity.class);

	public static final String RES_TYPE		= "type";
	public static final String RES_VALUE	= "value";

	public static final String DB_TYPE		= "ids_typ";
	public static final String DB_VALUE		= "ids_val";

	
	@JsonProperty(
			value = RES_TYPE,
			required = false)
	@Column(
			name = DB_TYPE, 
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
			nullable = true, 
			length = 128)
	@NotNull(
			message = "El valor no puede ser nulo")
	@Size(
			max = 128, 
			message = "El valor no puede sobrepasar los {max} caracteres.")
	private String value;

	
	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public PubValuesSubentity() {
		super();
	}

	public PubValuesSubentity(
			@Size(max = 32, message = "El tipo no puede sobrepasar los {max} caracteres.")
			String type,
			@NotNull(message = "El valor no puede ser nulo")
			@Size(max = 128, message = "El valor no puede sobrepasar los {max} caracteres.")
			String value) {
		super();
		this.type = type;
		this.value = value;
	}

}
