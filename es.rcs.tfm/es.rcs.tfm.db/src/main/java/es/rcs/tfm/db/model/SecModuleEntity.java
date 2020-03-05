package es.rcs.tfm.db.model;

import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
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
		type = SecModuleEntity.RES_TYPE,
		resourcePath = SecModuleEntity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = SecModuleEntity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
					name = SecModuleEntity.DB_ID_PK, 
					columnNames = { SecModuleEntity.DB_ID }),
			@UniqueConstraint(
					name = SecModuleEntity.DB_UID_UK, 
					columnNames = { SecModuleEntity.DB_UID }),
			@UniqueConstraint(
					name = SecModuleEntity.DB_CODE_UK, 
					columnNames = { SecModuleEntity.DB_CODE }),
			@UniqueConstraint(
					name = SecModuleEntity.DB_NAME_UK, 
					columnNames = { SecModuleEntity.DB_NAME}) })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class SecModuleEntity extends AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(SecModuleEntity.class);

	public static final String RES_ACTION			= "modules";
	public static final String RES_TYPE				= "Module";

	public static final String RES_CODE				= "code";
	public static final String RES_NAME				= "name";
	public static final String RES_URL				= "url";
	public static final String RES_APPLICATION_ID	= "applicationId";
	public static final String RES_APPLICATION		= "application";
	public static final String RES_FUNCTION_IDS		= "functionIds";
	public static final String RES_FUNCTIONS		= "functions";

	public static final String DB_TABLE 			= "sec_module";
	public static final String DB_ID_PK 			= "sec_mod_pk";
	public static final String DB_UID_UK			= "sec_mod_uid_uk";
	public static final String DB_CODE_UK			= "sec_mod_cod_uk";
	public static final String DB_NAME_UK			= "sec_mod_txt_uk";
	public static final String DB_APPLICATION_ID_FK	= "sec_mod_app_fk";

	public static final String DB_CODE				= "mod_cod";
	public static final String DB_NAME				= "mod_txt";
	public static final String DB_URL				= "mod_url";
	public static final String DB_APPLICATION_ID	= "app_id";

	public static final String ATT_APPLICATION		= "application";
	public static final String ATT_APPLICATION_ID	= "applicationId";
	public static final String ATT_FUNCTIONS		= "functions";
	public static final String ATT_FUNCTION_IDS		= "functionIds";

	
	@JsonProperty(
			value = RES_CODE,
			required = true)
	@Column(
			name = DB_CODE, 
			unique = true,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "El código del módulo o puede ser nulo")
	@Size(
			max = 32, 
			message = "El código del módulo no puede sobrepasar los {max} caracteres.")
	private String code;

	
	@JsonProperty(
			value = RES_NAME,
			required = true)
	@Column(
			name = DB_NAME, 
			unique = true,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "El nombre del modulo no puede ser nulo")
	@Size(
			max = 64, 
			message = "El nombre del modulo no puede sobrepasar los {max} caracteres.")
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
			value = RES_APPLICATION_ID)
	@Column(
			name = DB_APPLICATION_ID,
			unique = false,
			nullable = true)
	@NotNull(
			message = "La aplicacion no puede ser nula")
	private Long applicationId;

	
	@JsonApiRelation(
			idField = ATT_APPLICATION_ID,
			mappedBy = SecApplicationEntity.ATT_MODULES,
			lookUp = LookupIncludeBehavior.NONE,
			serialize = SerializeType.ONLY_ID)
	@JsonProperty(
			value = RES_APPLICATION)
	@JoinColumn(
			name = DB_APPLICATION_ID,
			referencedColumnName = SecApplicationEntity.DB_ID,
			unique = false,
			nullable = false,
			insertable = false,
			updatable = false,
			foreignKey = @ForeignKey(
					value = ConstraintMode.NO_CONSTRAINT,
					name = DB_APPLICATION_ID_FK))
	@ManyToOne(
			optional = false,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private SecApplicationEntity application;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_FUNCTION_IDS)
	@Transient
	private Set<Long> functionIds = null;

	
	@JsonApiRelation(
			idField = ATT_FUNCTION_IDS, 
			mappedBy = SecFunctionEntity.ATT_MODULE,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_FUNCTIONS,
			required = false)
	@OneToMany(
			mappedBy =  SecFunctionEntity.ATT_MODULE,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL } )
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecFunctionEntity> functions;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	public SecModuleEntity() {
		super();
	}

	public SecModuleEntity(
			SecApplicationEntity application,
			@NotNull(message = "El codigo no puede ser nulo") @Size(max = 32, message = "El codigono puede sobrepasar los {max} caracteres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 64, message = "El nombre puede sobrepasar los {max} caracteres.") String name) {
		super();
		this.code = code;
		this.name = name;
		this.setApplication(application);
	}

	public SecModuleEntity(
			SecApplicationEntity application,
			@NotNull(message = "El codigo no puede ser nulo") @Size(max = 32, message = "El codigono no puede sobrepasar los {max} caracteres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 64, message = "El nombre no puede sobrepasar los {max} caracteres.") String name,
			@NotNull(message = "La url base no puede ser nula") @Size(max = 64, message = "La url base no puede sobrepasar los {max} caracteres.") String url) {
		super();
		this.code = code;
		this.name = name;
		this.url = url;
		this.setApplication(application);
	}

	public void setApplication(SecApplicationEntity item) {
		if (item != null) {
			this.application = item;
			if (item.getId() != null) {
				this.applicationId = item.getId();
			}
		}
	}

	public void setFunctions(Set<SecFunctionEntity> items) {
		if ((items != null) && !items.isEmpty()) {
			this.functions = items;
			Set<Long> itemIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
			if ((itemIds != null) && !itemIds.isEmpty()) {
				this.functionIds = itemIds;
			}
		}
	}

}
