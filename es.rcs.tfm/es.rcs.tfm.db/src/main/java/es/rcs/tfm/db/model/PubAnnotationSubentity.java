package es.rcs.tfm.db.model;

import java.util.AbstractMap.SimpleEntry;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
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
public class PubAnnotationSubentity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubAnnotationSubentity.class);
	
	public static final String RES_IDENTIFIER		= "id";
	public static final String RES_INFONTYPE		= "type";
	public static final String RES_VALUE			= "offset";
	public static final String RES_TEXT				= "text";
	public static final String RES_POSITIONS		= "positions";

	public static final String DB_IDENTIFIER		= "ann_id";
	public static final String DB_TYPE				= "ann_typ";
	public static final String DB_TEXT				= "ann_txt";
	public static final String DB_VALUE				= "ann_val";
	public static final String DB_POSITIONS			= "ann_pos";


	@JsonProperty(
			value = RES_IDENTIFIER,
			required = false)
	@Column(
			name = DB_IDENTIFIER, 
			unique = false,
			nullable = true, 
			length = 128)
	@NotNull(
			message = "El identificador de la anotacion no puede ser nulo")
	@Size(
			max = 128, 
			message = "El identificador de la anotacion no puede sobrepasar los {max} caracteres.")
	private String identificador;

	
	@JsonProperty(
			value = RES_INFONTYPE,
			required = false)
	@Column(
			name = DB_TYPE, 
			unique = false,
			nullable = true, 
			length = 32)
	@Size(
			max = 32, 
			message = "El tipo no puede sobrepasar los {max} caracteres.")
	private String tipo;

	
	@JsonProperty(
			value = RES_TEXT,
			required = true)
	@Column(
			name = DB_TEXT, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "El texto no puede sobrepasar los {max} caracteres.")
	private String text;

	
	@JsonProperty(
			value = RES_VALUE,
			required = true)
	@Column(
			name = DB_VALUE,
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "El valor representado no puede sobrepasar los {max} caracteres.")
	private String value;

	
	@JsonProperty(
			value = RES_POSITIONS,
			required = true)
	@Column(
			name = DB_POSITIONS, 
			unique = false,
			nullable = true, 
			length = 1024)
	@Size(
			max = 1024, 
			message = "El texto no puede sobrepasar los {max} caracteres.")
	@Setter(
			value = AccessLevel.NONE)
	@Getter(
			value = AccessLevel.NONE)
	private String positions;
	
	// Almacen offset - length
	@Transient
	private Vector<SimpleEntry<Integer, Integer>> postionsVector = new Vector<>();

	
	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public PubAnnotationSubentity() {
		super();
	}

	/*
	public PubAnnotationSubentity(
			@Size(max = 32, message = "El tipo no puede sobrepasar los {max} caracteres.")
			String tipo,
			@NotNull(message = "El identificador no puede ser nulo")
			@Size(max = 32, message = "El identificador no puede sobrepasar los {max} caracteres.")
			String identificador) {
		super();
		this.tipo = tipo;
		this.identificador = identificador;
	}
	 */

	public String getPositions() {
		return positions;
	}


	public void setPositions(String positions) {
		this.positions = positions;
	}

}
