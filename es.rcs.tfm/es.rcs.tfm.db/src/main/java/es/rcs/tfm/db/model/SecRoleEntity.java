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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
		type = SecRoleEntity.RES_TYPE,
		resourcePath = SecRoleEntity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = SecRoleEntity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
					name = SecRoleEntity.DB_ID_PK, 
					columnNames = { SecRoleEntity.DB_ID }),
			@UniqueConstraint(
					name = SecRoleEntity.DB_UID_UK, 
					columnNames = { SecRoleEntity.DB_UID }),
			@UniqueConstraint(
					name = SecRoleEntity.DB_CODE_UK, 
					columnNames = { SecRoleEntity.DB_CODE }),
			@UniqueConstraint(
					name = SecRoleEntity.DB_NAME_UK, 
					columnNames = { SecRoleEntity.DB_NAME })})
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class SecRoleEntity extends AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(SecRoleEntity.class);

	public static final String RES_ACTION			= "roles";
	public static final String RES_TYPE				= "Role";

	public static final String RES_CODE				= "code";
	public static final String RES_NAME				= "name";

	public static final String RES_APPLICATION_ID	= "applicationId";
	public static final String RES_APPLICATION		= "application";
	public static final String RES_USERS_IDS		= "userIds";
	public static final String RES_USERS			= "users";
	public static final String RES_AUTHORITY_IDS	= "authorityIds";
	public static final String RES_AUTHORITIES		= "authorities";

	public static final String DB_TABLE 			= "sec_role";
	public static final String DB_ID_PK 			= "sec_rol_pk";
	public static final String DB_UID_UK			= "sec_rol_uid_uk";
	public static final String DB_CODE_UK			= "sec_rol_cod_uk";
	public static final String DB_NAME_UK			= "sec_rol_rol_uk";
	
	public static final String DB_CODE				= "rol_cod";
	public static final String DB_NAME				= "rol_txt";

	public static final String DB_TABLA_ROLE_AUTH	= "sec_role_authorities";
	public static final String DB_ROLEAUTHS_FK		= "sec_rol_aut_fk";
	public static final String DB_AUTHROLES_FK		= "sec_aut_rol_fk";
	public static final String DB_APPLICATION_ID_FK	= "sec_rol_app_fk";

	public static final String DB_ROLE_ID			= "rol_id";
	public static final String DB_AUTH_ID			= "aut_id";
	public static final String DB_APPLICATION_ID	= "app_id";

	public static final String ATT_APPLICATION		= "application";
	public static final String ATT_APPLICATION_ID	= "applicationId";
	public static final String ATT_AUTHORITIES		= "authorities";
	public static final String ATT_AUTHORITY_IDS	= "authorityIds";
	public static final String ATT_USERS			= "users";
	public static final String ATT_USERS_IDS		= "userIds";

	
	@JsonProperty(
			value = RES_CODE,
			required = true)
	@Column(
			name = DB_CODE, 
			unique = true,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "El c�digo no puede ser nulo")
	@Size(
			max = 32, 
			message = "El c�digo no puede sobrepasar los {max} caracteres.")
	public String code;


	@JsonProperty(
			value = RES_NAME,
			required = true)
	@Column(
			name = DB_NAME, 
			unique = true,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "El nombre no puede ser nulo")
	@Size(
			max = 64, 
			message = "El nombre puede sobrepasar los {max} caracteres.")
	public String name;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_APPLICATION_ID)
	@Column(
			name = DB_APPLICATION_ID,
			unique = false,
			nullable = false)
	private Long applicationId;

	
	@JsonApiRelation(
			idField = ATT_APPLICATION_ID,
			mappedBy = SecApplicationEntity.ATT_ROLES,
			lookUp = LookupIncludeBehavior.NONE,
			serialize = SerializeType.ONLY_ID)
	@JsonProperty(
			value = RES_APPLICATION)
	@JoinColumn(
			name = DB_APPLICATION_ID, 
			unique = false,
			nullable = false, 
			insertable = false,
			updatable = false,
			referencedColumnName = SecApplicationEntity.DB_ID,
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
	public SecApplicationEntity application;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_USERS_IDS)
	@Transient
	private Set<Long> userIds = null;
	
	
	@JsonApiRelation(
			idField = ATT_USERS_IDS, 
			mappedBy = SecUserEntity.ATT_ROLES)
	@JsonProperty(
			value = RES_USERS)
	@ManyToMany(
			mappedBy = SecUserEntity.ATT_ROLES,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecUserEntity> users = null;


	@JsonApiRelationId
	@JsonProperty(
			value = RES_AUTHORITY_IDS)
	@Transient
	private Set<Long> authorityIds = null;


	@JsonApiRelation(
			idField = ATT_AUTHORITY_IDS, 
			mappedBy = SecAuthorityEntity.ATT_ROLES)
	@JsonProperty(
			value = RES_AUTHORITIES)
	@JoinTable (
			name = DB_TABLA_ROLE_AUTH,
			joinColumns = @JoinColumn (
					name = DB_ROLE_ID, 
					referencedColumnName = DB_ID),
			inverseJoinColumns = @JoinColumn (
					name = DB_AUTH_ID, 
					referencedColumnName = SecAuthorityEntity.DB_ID),
			foreignKey = @ForeignKey (
					name = DB_ROLEAUTHS_FK),
			inverseForeignKey = @ForeignKey (
					name = DB_AUTHROLES_FK))
	@ManyToMany (
			// Associations marked as mappedBy must not define database mappings like @JoinTable or @JoinColumn		
			// mappedBy = "roles",
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH, CascadeType.PERSIST })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecAuthorityEntity> authorities;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	public SecRoleEntity() {
		super();
	}

	public SecRoleEntity(
			SecApplicationEntity application,
			@NotNull(message = "El c�digo no puede ser nulo") @Size(max = 32, message = "El c�digono puede sobrepasar los {max} caracteres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 64, message = "El nombre puede sobrepasar los {max} caracteres.") String name,
			Set<SecAuthorityEntity> authorities) {
		super();
		this.code = code;
		this.name = name;
		this.setApplication(application);
		this.setAuthorities(authorities);
	}
	
	public void setApplication(SecApplicationEntity item) {
		if (item != null) {
			this.application = item;
			if (item.getId() != null) {
				this.applicationId = item.getId();
			}
		}
	}

	public void setUsers(Set<SecUserEntity> items) {
		if ((items != null) && !items.isEmpty()) {
			this.users = items;
			Set<Long> itemIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
			if ((itemIds != null) && !itemIds.isEmpty()) {
				this.userIds = itemIds;
			}
		}
	}

	public void setAuthorities(Set<SecAuthorityEntity> items) {
		if ((items != null) && !items.isEmpty()) {
			this.authorities = items;
			Set<Long> itemIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
			if ((itemIds != null) && !itemIds.isEmpty()) {
				this.authorityIds = itemIds;
			}
		}
	}

}
