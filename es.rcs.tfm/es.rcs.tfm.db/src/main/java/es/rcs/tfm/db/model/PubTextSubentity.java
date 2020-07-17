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
public class PubTextSubentity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubTextSubentity.class);

	public static final String RES_TYPE		= "type";
	public static final String RES_SUBTYPE	= "subtype";
	public static final String RES_LANGUAGE	= "language";
	public static final String RES_COPYRIGHT= "copyright";
	public static final String RES_ORDER	= "order";
	public static final String RES_LABEL	= "label";
	public static final String RES_CATEGORY	= "category";
	public static final String RES_TEXT		= "text";

	public static final String DB_TYPE		= "txt_typ";
	public static final String DB_SUBTYPE	= "txt_sub";
	public static final String DB_LANGUAGE	= "txt_lan";
	public static final String DB_COPYRIGHT	= "txt_cop";
	public static final String DB_ORDER		= "txt_ord";
	public static final String DB_LABEL		= "txt_lab";
	public static final String DB_CATEGORY	= "txt_cat";
	public static final String DB_TEXT		= "txt_txt";

	public static final String ATT_TYPE		= "type";
	public static final String ATT_TEXT		= "text";

	
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
			value = RES_SUBTYPE,
			required = false)
	@Column(
			name = DB_SUBTYPE, 
			unique = false,
			nullable = true, 
			length = 32)
	@Size(
			max = 32, 
			message = "El subtipo no puede sobrepasar los {max} caracteres.")
	private String subtype;

	
	@JsonProperty(
			value = RES_LANGUAGE,
			required = false)
	@Column(
			name = DB_LANGUAGE, 
			unique = false,
			nullable = true, 
			length = 128)
	@Size(
			max = 128, 
			message = "El idioma no puede sobrepasar los {max} caracteres.")
	private String language;

	
	@JsonProperty(
			value = RES_COPYRIGHT,
			required = false)
	@Column(
			name = DB_COPYRIGHT, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "El copyright no puede sobrepasar los {max} caracteres.")
	private String copyright;

	
	@JsonProperty(
			value = RES_ORDER,
			required = false)
	@Column(
			name = DB_ORDER, 
			unique = false,
			nullable = true)
	private Integer order;

	
	@JsonProperty(
			value = RES_LABEL,
			required = false)
	@Column(
			name = DB_LABEL, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "La etiqueta no puede sobrepasar los {max} caracteres.")
	private String label;

	
	@JsonProperty(
			value = RES_CATEGORY,
			required = false)
	@Column(
			name = DB_CATEGORY, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "La categoría no puede sobrepasar los {max} caracteres.")
	private String category;

	
	@JsonProperty(
			value = RES_TEXT,
			required = false)
	@Column(
			name = DB_TEXT, 
			unique = false,
			nullable = true, 
			length = 8000)
	@NotNull(
			message = "El texto no puede ser nulo")
	@Size(
			max = 8000, 
			message = "El texto no puede sobrepasar los {max} caracteres.")
	private String text;

	
	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public PubTextSubentity() {
		super();
	}

	public PubTextSubentity(
			@Size(max = 32, message = "El tipo no puede sobrepasar los {max} caracteres.")
			String type,
			@NotNull(message = "El texto no puede ser nulo")
			@Size(max = 8000, message = "El texto no puede sobrepasar los {max} caracteres.")
			String text) {
		super();
		this.type = type;
		this.text = text;
	}

	public PubTextSubentity(
			@Size(max = 32, message = "El tipo no puede sobrepasar los {max} caracteres.")
			String type,
			@Size(max = 32, message = "El subtipo no puede sobrepasar los {max} caracteres.")
			String subtype,
			@Size(max = 128, message = "El idioma no puede sobrepasar los {max} caracteres.")
			String language,
			@Size(max = 256, message = "El copyright no puede sobrepasar los {max} caracteres.")
			String copyright,
			Integer order,
			@Size(max = 256, message = "La etiqueta no puede sobrepasar los {max} caracteres.")
			String label,
			@Size(max = 256, message = "La categoría no puede sobrepasar los {max} caracteres.")
			String category,
			@NotNull(message = "El texto no puede ser nulo")
			@Size(max = 8000, message = "El texto no puede sobrepasar los {max} caracteres.")
			String text) {
		super();
		this.type = type;
		this.subtype = subtype;
		this.language = language;
		this.copyright = copyright;
		this.order = order;
		this.label = label;
		this.category = category;
		this.text = text;
	}

}
