package es.rcs.tfm.db.model;

import java.time.LocalDate;

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
public class PubDateSubentity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubDateSubentity.class);

	public static final String RES_TYPE		= "type";
	public static final String RES_DATE		= "date";

	public static final String DB_TYPE		= "dat_typ";
	public static final String DB_DATE		= "dat_txt";

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
	private String tipo;

	@JsonProperty(
			value = RES_DATE,
			required = false)
	@Column(
			name = DB_DATE, 
			unique = false,
			nullable = true)
	@NotNull(
			message = "La fecha no puede ser nula")
	private LocalDate fecha;

	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public PubDateSubentity() {
		super();
	}

	public PubDateSubentity(
			@Size(max = 32, message = "El tipo no puede sobrepasar los {max} caracteres.")
			String tipo,
			@NotNull(message = "La fecha no puede ser nula")
			LocalDate fecha) {
		super();
		this.tipo = tipo;
		this.fecha = fecha;
	}
	
}
