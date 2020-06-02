package es.rcs.tfm.db.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
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
public class PubPermissionSubentity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(PubValuesSubentity.class);

	public static final String RES_COUNTRY			= "country";
	public static final String RES_AGENCY			= "agency";
	public static final String RES_CODE				= "code";
	public static final String RES_PERMISSION		= "permission";

	public static final String DB_COUNTRY			= "per_cou";
	public static final String DB_AGENCY			= "per_age";
	public static final String DB_CODE				= "per_cod";
	public static final String DB_PERMISSION		= "per_txt";

	
	@JsonProperty(
			value = RES_COUNTRY,
			required = false)
	@Column(
			name = DB_COUNTRY, 
			unique = false,
			nullable = true, 
			length = 64)
	@Size(
			max = 64, 
			message = "El pais no puede sobrepasar los {max} caracteres.")
	private String country;

	
	@JsonProperty(
			value = RES_AGENCY,
			required = false)
	@Column(
			name = DB_AGENCY, 
			unique = false,
			nullable = true, 
			length = 1024)
	@Size(
			max = 1024, 
			message = "La agencia no puede sobrepasar los {max} caracteres.")
	private String agency;

	
	@JsonProperty(
			value = RES_CODE,
			required = false)
	@Column(
			name = DB_CODE, 
			unique = false,
			nullable = true, 
			length = 32)
	@Size(
			max = 32, 
			message = "El codigo no puede sobrepasar los {max} caracteres.")
	private String code;

	
	@JsonProperty(
			value = RES_PERMISSION,
			required = true)
	@Column(
			name = DB_PERMISSION, 
			unique = false,
			nullable = true, 
			length = 256)
	@Size(
			max = 256, 
			message = "El nombre del permiso no puede sobrepasar los {max} caracteres.")
	private String permission;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public PubPermissionSubentity() {
		super();
	}

	public PubPermissionSubentity(
			@Size(max = 64, message = "El pais no puede sobrepasar los {max} caracteres.")
			String country,
			@Size(max = 256, message = "La agencia no puede sobrepasar los {max} caracteres.")
			String agency,
			@Size(max = 256, message = "El nombre del permiso no puede sobrepasar los {max} caracteres.")
			String permission,
			@Size(max = 32, message = "El codigo no puede sobrepasar los {max} caracteres.")
			String code) {
		super();
		this.country = country;
		this.agency = agency;
		this.permission = permission;
		this.code = code;
	}

	
}
