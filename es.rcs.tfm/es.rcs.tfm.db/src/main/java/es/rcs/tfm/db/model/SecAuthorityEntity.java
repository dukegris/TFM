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
		type = SecAuthorityEntity.RES_NAME,
		resourcePath = SecAuthorityEntity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = SecAuthorityEntity.DB_TABLE,
		uniqueConstraints = {
			@UniqueConstraint(
					name = SecAuthorityEntity.DB_ID_PK, 
					columnNames = { SecAuthorityEntity.DB_ID }),
			@UniqueConstraint(
					name = SecAuthorityEntity.DB_UID_UK, 
					columnNames = { SecAuthorityEntity.DB_UID }),
			@UniqueConstraint(
					name = SecAuthorityEntity.DB_CODE_UK, 
					columnNames = { SecAuthorityEntity.DB_APPLICATION_ID, SecAuthorityEntity.DB_CODE }) ,
			@UniqueConstraint(
					name = SecAuthorityEntity.DB_AUTHORITY_UK, 
					columnNames = { SecAuthorityEntity.DB_APPLICATION_ID, SecAuthorityEntity.DB_AUTHORITY }) })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class SecAuthorityEntity extends AuditedBaseEntity {

	@Transient
	protected Logger LOG = LoggerFactory.getLogger(SecAuthorityEntity.class);

	public static final String RES_ACTION			= "authorities";
	public static final String RES_NAME				= "Authority";

	public static final String RES_CODE				= "code";
	public static final String RES_AUTHORITY		= "authority";

	public static final String RES_APPLICATION_ID	= "aplicationId";
	public static final String RES_APPLICATION		= "aplication";

	public static final String RES_FUNCTION_IDS		= "functionIds";
	public static final String RES_FUNCTIONS		= "functions";
	public static final String RES_ROLE_IDS			= "roleIds";
	public static final String RES_ROLES			= "roles";
	public static final String RES_GROUP_IDS		= "groupIds";
	public static final String RES_GROUPS			= "groups";
	public static final String RES_USER_IDS			= "userIds";
	public static final String RES_USERS			= "users";

	public static final String DB_TABLE 			= "sec_authority";
	public static final String DB_ID_PK 			= "sec_aut_pk";
	public static final String DB_UID_UK			= "sec_aut_uid_uk";
	public static final String DB_AUTHORITY_UK		= "sec_aut_aut_uk";
	public static final String DB_CODE_UK			= "sec_aut_cod_uk";
	public static final String DB_APPLICATION_ID_FK	= "sec_aut_app_fk";

	public static final String DB_CODE				= "aut_cod";
	public static final String DB_AUTHORITY			= "aut_txt";
	public static final String DB_APPLICATION_ID	= "app_id";

	public static final String ATT_APPLICATION		= "application";
	public static final String ATT_APPLICATION_ID	= "applicationId";
	public static final String ATT_ROLES			= "roles";
	public static final String ATT_ROLE_IDS		= "roleIds";
	public static final String ATT_GROUPS			= "groups";
	public static final String ATT_GROUP_IDS		= "groupIds";
	public static final String ATT_USERS			= "users";
	public static final String ATT_USER_IDS		= "userIds";

	
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
			message = "El código no puede sobrepasar los {max} caracteres.")
	private String code;


	@JsonProperty(
			value = RES_AUTHORITY,
			required = true)
	@Column(
			name = DB_AUTHORITY, 
			unique = true,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "El nombre no puede ser nulo")
	@Size(
			max = 64, 
			message = "El nombre puede sobrepasar los {max} caracteres.")
	private String name;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_APPLICATION_ID)
	@Column(
			name = DB_APPLICATION_ID,
			unique = false,
			nullable = false)
	private Long applicationId;

	
	@JsonApiRelation(
			idField = RES_APPLICATION_ID,
			mappedBy = SecApplicationEntity.ATT_AUTHORITIES,
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
			value = RES_USER_IDS)
	@Transient
	private Set<Long> userIds = null;
	
	
	@JsonProperty(
			value = RES_USERS)
	@JsonApiRelation(
			idField = RES_USER_IDS, 
			mappedBy = SecUserEntity.ATT_AUTHORITIES)
	@ManyToMany(
			mappedBy = SecUserEntity.ATT_AUTHORITIES,
			cascade = { CascadeType.DETACH },
			fetch = FetchType.LAZY)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecUserEntity> users = null;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_ROLE_IDS)
	@Transient
	private Set<Long> roleIds = null;

	
	@JsonProperty(
			value = RES_ROLES)
	@JsonApiRelation(
			idField = RES_ROLE_IDS, 
			mappedBy = SecRoleEntity.ATT_AUTHORITIES)
	@ManyToMany(
			mappedBy = SecRoleEntity.ATT_AUTHORITIES,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecRoleEntity> roles = null;


	@JsonApiRelationId
	@JsonProperty(
			value = RES_GROUP_IDS)
	@Transient
	private Set<Long> groupIds = null;
	
	
	@JsonApiRelation(
			idField = RES_GROUPS_IDS, 
			mappedBy = SecGroupEntity.ATT_AUTHORITIES)
	@JsonProperty(
			value = RES_GROUPS)
	@ManyToMany(
			mappedBy = SecGroupEntity.ATT_AUTHORITIES,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecGroupEntity> groups = null;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	public SecAuthorityEntity() {
		super();
	}

	public SecAuthorityEntity(
			SecApplicationEntity application,
			@NotNull(message = "El código no puede ser nulo") @Size(max = 32, message = "El códigono puede sobrepasar los {max} caracteres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 64, message = "El nombre puede sobrepasar los {max} caracteres.") String name) {
		super();
		this.code = code;
		this.name = name;
		this.setApplication(application);
	}
	
	public void setApplication(SecApplicationEntity item) {
		this.application = item;
		this.applicationId = (item != null) ? item.getId() : null;
	}

	public void setUsers(Set<SecUserEntity> items) {
		this.users = items;
		if (items != null) this.userIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}

	public void setARoles(Set<SecRoleEntity> items) {
		this.roles = items;
		if (items != null) this.roleIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}

	public void setGroups(Set<SecGroupEntity> items) {
		this.groups = items;
		if (items != null) this.groupIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}

}
