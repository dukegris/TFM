package es.rcs.tfm.db.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.crnk.core.queryspec.pagingspec.NumberSizePagingSpec;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = true)
@JsonApiResource(
		type = SecFunctionEntity.RES_TYPE,
		resourcePath = SecFunctionEntity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = SecFunctionEntity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
				name = SecFunctionEntity.DB_ID_PK, 
				columnNames = { SecFunctionEntity.DB_ID }),
			@UniqueConstraint(
					name = SecFunctionEntity.DB_UID_UK, 
					columnNames = { SecFunctionEntity.DB_UID }),
			@UniqueConstraint(
					name = SecFunctionEntity.DB_CODE_UK, 
					columnNames = { SecFunctionEntity.DB_CODE }),
			@UniqueConstraint(
					name = SecFunctionEntity.DB_FUNCTION_UK, 
					columnNames = { SecFunctionEntity.DB_FUNCTION}) })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class SecFunctionEntity extends AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(SecFunctionEntity.class);

	public static final String RES_ACTION		= "functions";
	public static final String RES_TYPE			= "Function";

	public static final String RES_CODE			= "code";
	public static final String RES_FUNCTION		= "function";
	public static final String RES_URL			= "url";

	public static final String RES_MODULE_ID	= "moduleId";
	public static final String RES_MODULE		= "module";
	
	public static final String DB_TABLE 		= "sec_function";
	
	public static final String DB_ID_PK 		= "sec_fun_pk";
	public static final String DB_UID_UK		= "sec_fun_uid_uk";
	public static final String DB_CODE_UK		= "sec_fun_cod_uk";
	public static final String DB_FUNCTION_UK	= "sec_fun_txt_uk";
	public static final String DB_MODULE_ID_FK	= "sec_fun_mod_fk";

	public static final String DB_CODE			= "fun_cod";
	public static final String DB_FUNCTION		= "fun_txt";
	public static final String DB_URL			= "fun_url";
	public static final String DB_MODULE_ID		= "mod_id";

	public static final String ATT_MODULE_ID	= "moduleId";
	public static final String ATT_MODULE		= "module";

	
	@JsonProperty(
			value = RES_CODE,
			required = true)
	@Column(
			name = DB_CODE, 
			unique = true,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "El código no puede ser nulo")
	@Size(
			max = 32, 
			message = "El códigono puede sobrepasar los {max} caracteres.")
	private String code;

	
	@JsonProperty(
			value = RES_FUNCTION,
			required = true)
	@Column(
			name = DB_FUNCTION, 
			unique = false,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "El nombre no puede ser nulo")
	@Size(
			max = 64, 
			message = "El nombre puede sobrepasar los {max} caracteres.")
	private String name;


	@JsonProperty(
			value = RES_URL,
			required = true)
	@Column(
			name = DB_URL, 
			unique = false,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "La url base no puede ser nula")
	@Size(
			max = 64, 
			message = "La url base no puede sobrepasar los {max} caracteres.")
	public String url;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_MODULE_ID)
	@Column(
			name = DB_MODULE_ID,
			unique = false,
			nullable = false)
	private Long moduleId;

	
	@JsonApiRelation(
			idField = RES_MODULE_ID,
			mappedBy = SecModuleEntity.ATT_FUNCTIONS,
			lookUp = LookupIncludeBehavior.NONE,
			serialize = SerializeType.ONLY_ID)
	@JsonProperty(
			value = RES_MODULE)
	@JoinColumn(
			name = DB_MODULE_ID,
			referencedColumnName = SecModuleEntity.DB_ID,
			unique = false,
			nullable = false,
			insertable = false,
			updatable = false,
			foreignKey = @ForeignKey(
					value = ConstraintMode.NO_CONSTRAINT,
					name = DB_MODULE_ID_FK))
	@ManyToOne(
			optional = false,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private SecModuleEntity module;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	public SecFunctionEntity() {
		super();
	}

	public SecFunctionEntity(
			SecModuleEntity module,
			@NotNull(message = "El código no puede ser nulo") @Size(max = 32, message = "El códigono puede sobrepasar los {max} caracteres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 64, message = "El nombre puede sobrepasar los {max} caracteres.") String name) {
		super();
		this.code = code;
		this.name = name;
		this.setModule(module);
	}

	public SecFunctionEntity(
			SecModuleEntity module,
			@NotNull(message = "El código no puede ser nulo") @Size(max = 32, message = "El códigono puede sobrepasar los {max} caracteres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 64, message = "El nombre puede sobrepasar los {max} caracteres.") String name,
			@NotNull(message = "La url base no puede ser nula") @Size(max = 64, message = "La url base no puede sobrepasar los {max} caracteres.") String url) {
		super();
		this.code = code;
		this.name = name;
		this.url = url;
		this.setModule(module);
	}

	public void setModule(SecModuleEntity item) {
		this.module = item;
		this.moduleId = (item != null) ? item.getId() : null;
	}
	
}
