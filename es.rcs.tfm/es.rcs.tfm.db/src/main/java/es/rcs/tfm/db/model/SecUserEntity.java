package es.rcs.tfm.db.model;

import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
import io.crnk.core.resource.annotations.JsonApiField;
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
		type = SecUserEntity.RES_NAME,
		resourcePath = SecUserEntity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = SecUserEntity.DB_TABLE,
		uniqueConstraints = {
				@UniqueConstraint(
						name = SecUserEntity.DB_ID_PK, 
						columnNames = { SecUserEntity.DB_ID }),
				@UniqueConstraint(
						name = SecUserEntity.DB_UID_UK, 
						columnNames = { SecUserEntity.DB_UID }),
				@UniqueConstraint(
						name = SecUserEntity.DB_EMAIL_UK, 
						columnNames = { SecUserEntity.DB_EMAIL }),
				@UniqueConstraint(
						name = SecUserEntity.DB_USERNAME_UK, 
						columnNames = { SecUserEntity.DB_USERNAME }) })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class SecUserEntity extends AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(SecUserEntity.class);

	public static final String RES_ACTION			= "users";
	public static final String RES_NAME				= "User";

	public static final String RES_USERNAME			= "username";
	public static final String RES_USR_ENABLED		= "enabled";
	public static final String RES_USR_EXPIRED		= "expired";
	public static final String RES_USR_LOCKED		= "locked";
	public static final String RES_USR_TWOFACTOR	= "seguridadDobleFactor";
	public static final String RES_PASSWORD			= "password";
	public static final String RES_PWD_EXPIRED		= "expiredPassword";
	public static final String RES_EMAIL			= "email";
	public static final String RES_EMA_CONFIRMED	= "emailConfirmed";
	public static final String RES_ROLES_IDS		= "roleIds";
	public static final String RES_ROLES			= "roles";
	public static final String RES_GROUPS_IDS		= "groupIds";
	public static final String RES_GROUPS			= "groups";
	public static final String RES_AUTHORITIES_IDS	= "authorityIds";
	public static final String RES_AUTHORITIES		= "authorities";
	public static final String RES_TOKENS_IDS		= "tokenIds";
	public static final String RES_TOKENS			= "tokenss";

	public static final String DB_TABLE 			= "sec_users";
	public static final String DB_ID_PK 			= "sec_usr_pk";
	public static final String DB_UID_UK			= "sec_usr_uid_uk";
	public static final String DB_USERNAME_UK		= "sec_usr_usr_uk";
	public static final String DB_EMAIL_UK			= "sec_usr_ema_uk";

	public static final String DB_USERNAME			= "usr_txt";
	public static final String DB_USR_ENABLED		= "usr_ena";
	public static final String DB_USR_EXPIRED		= "usr_exp";
	public static final String DB_USR_LOCKED		= "usr_lck";
	public static final String DB_USR_TWOFACTOR		= "usr_two";
	public static final String DB_PASSWORD			= "usr_pwd";
	public static final String DB_PWD_EXPIRED		= "usr_pwd_exp";
	public static final String DB_EMAIL				= "usr_ema";
	public static final String DB_EMA_CONFIRMED		= "usr_ema_con";

	public static final String DB_TABLE_USER_ROLES	= "sec_user_roles";
	public static final String DB_USERROLES_FK		= "sec_usr_rol_fk";
	public static final String DB_ROLESUSERS_FK		= "sec_rol_usr_fk";
	
	public static final String DB_TABLE_USER_AUTH	= "sec_user_authorities";
	public static final String DB_USERAUTHS_FK		= "sec_usr_aut_fk";
	public static final String DB_AUTHSUSERS_FK		= "sec_aut_usr_fk";
	
	public static final String DB_TABLE_USER_GROUP	= "sec_user_groups";
	public static final String DB_USERGROUPS_FK		= "sec_usr_grp_fk";
	public static final String DB_GROUPUSERS_FK		= "sec_grp_usr_fk";
	
	public static final String DB_USER_ID			= "usr_id";
	public static final String DB_ROLE_ID			= "rol_id";
	public static final String DB_AUTH_ID			= "aut_id";
	public static final String DB_GROUP_ID			= "grp_id";
	
	public static final String ATT_AUTHORITIES		= "authorities";
	public static final String ATT_AUTHORITIES_IDS	= "authorityIds";
	public static final String ATT_GROUPS			= "groups";
	public static final String ATT_GROUPS_IDS		= "groupIds";
	public static final String ATT_ROLES			= "roles";
	public static final String ATT_ROLES_IDS		= "roleIds";
	public static final String ATT_TOKENS			= "tokens";
	public static final String ATT_TOKENS_IDS		= "tokenIds";
	
	@JsonProperty(
			value = RES_USERNAME,
			required = true)
	@Column(
			name = DB_USERNAME, 
			unique = true,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "El nombre del usuario no puede ser nulo")
	@Size(
			max = 64, 
			message = "El nombre del usuario no puede sobrepasar los {max} caracteres.")
	private String username;

	
	@JsonProperty(
			value = RES_USR_ENABLED,
			required = true)
	@Column(
			name = DB_USR_ENABLED,
			unique = false,
			nullable = false)
	@NotNull(
			message = "La habilitaci�n del usuario no puede ser nula")
	private boolean enabled = false;

	
	@JsonProperty(
			value = RES_USR_EXPIRED,
			required = true)
	@Column(
			name = DB_USR_EXPIRED,
			unique = false,
			nullable = false)
	@NotNull(
			message = "El estado de expiraci�n del usuario no puede ser nulo")
	private boolean expired = true;

	
	@JsonProperty(
			value = RES_USR_LOCKED,
			required = true)
	@Column(
			name = DB_USR_LOCKED, 
			unique = false,
			nullable = false)
	@NotNull(
			message = "El estado de bloqueo del usuario no puede ser nulo")
	private boolean locked = true;

	
	@JsonApiField(
			postable = false,
			readable = false,
			patchable = false, 
			sortable = false, 
			filterable = false)
	@JsonProperty(
			value = RES_PASSWORD,
			required = true)
	@Column(
			name = DB_PASSWORD, 
			unique = false,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "La clave del usuario no puede ser nula.")
	@Size(
			max = 64, 
			message = "La clave del usuario no puede sobrepasar los {max} caracteres.")
	private String password;

	
	@JsonProperty(
			value = RES_PWD_EXPIRED,
			required = true)
	@Column(
			name = DB_PWD_EXPIRED, 
			unique = false,
			nullable = false)
	@NotNull(
			message = "El estado de expiraci�n de la contrase�a del usuario no puede ser nulo")
	private boolean passwordExpired = true;

	
	@JsonProperty(
			value = RES_EMAIL,
			required = true)
	@Column(
			name = DB_EMAIL,
			unique = false,
			nullable = false, 
			length = 128)
	@NotNull(
			message = "El correo electr�nico del usuario no puede ser nulo.")
	@Size(
			max = 128, 
			message = "El correo electr�nico del usuario no puede sobrepasar los {max} caracteres.")
	private String email;

	
	@JsonProperty(
			value = RES_EMA_CONFIRMED,
			required = true)
	@Column(
			name = DB_EMA_CONFIRMED,
			unique = false,
			nullable = false)
	@NotNull(
			message = "La verificaci�n del correo electr�nico del usuario no puede ser nula")
	private boolean emailConfirmed = false;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_ROLES_IDS)
	@Transient
	private Set<Long> roleIds = null;

	
	@JsonProperty(
			value = RES_ROLES)
	@JsonApiRelation(
			idField = RES_ROLES_IDS, 
			mappedBy = SecRoleEntity.ATT_USERS)
	@JoinTable (
			name = DB_TABLE_USER_ROLES,
			joinColumns = @JoinColumn (
					name = DB_USER_ID, 
					referencedColumnName = DB_ID),
			inverseJoinColumns = @JoinColumn (
					name = DB_ROLE_ID, 
					referencedColumnName = SecRoleEntity.DB_ID),
			foreignKey = @ForeignKey (
					name = DB_USERROLES_FK),
			inverseForeignKey = @ForeignKey (
					name = DB_ROLESUSERS_FK))
	@ManyToMany (
			// Associations marked as mappedBy must not define database mappings like @JoinTable or @JoinColumn		
			// mappedBy = "users",
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH, CascadeType.PERSIST })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecRoleEntity> roles;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_GROUPS_IDS)
	@Transient
	private Set<Long> groupIds = null;

	
	@JsonApiRelation(
			idField = RES_GROUPS_IDS, 
			mappedBy = SecGroupEntity.ATT_USERS)
	@JsonProperty(
			value = RES_GROUPS)
	@JoinTable (
			name = DB_TABLE_USER_GROUP,
			joinColumns = @JoinColumn (
					name = DB_USER_ID, 
					referencedColumnName = DB_ID),
			inverseJoinColumns = @JoinColumn (
					name = DB_GROUP_ID, 
					referencedColumnName = SecGroupEntity.DB_ID),
			foreignKey = @ForeignKey (
					name = DB_USERGROUPS_FK),
			inverseForeignKey = @ForeignKey (
					name = DB_GROUPUSERS_FK))
	@ManyToMany (
			// Associations marked as mappedBy must not define database mappings like @JoinTable or @JoinColumn		
			// mappedBy = "users",
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH, CascadeType.PERSIST })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecGroupEntity> groups;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_AUTHORITIES_IDS)
	@Transient
	private Set<Long> authorityIds = null;

	
	@JsonApiRelation(
			idField = RES_AUTHORITIES_IDS, 
			mappedBy = SecAuthorityEntity.ATT_USERS)
	@JsonProperty(
			value = RES_AUTHORITIES)
	@JoinTable (
			name = DB_TABLE_USER_AUTH,
			joinColumns = @JoinColumn (
					name = DB_USER_ID, 
					referencedColumnName = DB_ID),
			inverseJoinColumns = @JoinColumn (
					name = DB_AUTH_ID, 
					referencedColumnName = SecAuthorityEntity.DB_ID),
			foreignKey = @ForeignKey (
					name = DB_USERAUTHS_FK),
			inverseForeignKey = @ForeignKey (
					name = DB_AUTHSUSERS_FK))
	@ManyToMany (
			// Associations marked as mappedBy must not define database mappings like @JoinTable or @JoinColumn		
			// mappedBy = "users",
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH, CascadeType.PERSIST })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecAuthorityEntity> authorities;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_TOKENS_IDS)
	@Transient
	private Set<Long> tokenIds = null;

	
	@JsonApiRelation( 
			idField = RES_TOKENS_IDS, 
			mappedBy = SecTokenEntity.ATT_USER,
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@JsonProperty(
			value = RES_TOKENS,
			required = false)
	@OneToMany(
			mappedBy = SecTokenEntity.ATT_USER,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.ALL })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecTokenEntity> tokens;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	public SecUserEntity() {
		super();
	}

	public SecUserEntity(
			@NotNull(message = "El nombre del usuario no puede ser nulo") @Size(max = 64, message = "El nombre ndel usuario o puede sobrepasar los {max} caracteres.") String username,
			@NotNull(message = "El correo electr�nico del usuario no puede ser nulo") @Size(max = 128, message = "El correo electr�nico del usuario no puede sobrepasar los {max} caracteres.") String email,
			@NotNull(message = "La clave del usuario no puede ser nula") @Size(max = 64, message = "La clave del usuario no puede sobrepasar los {max} caracteres.") String password) {
		super();
		this.username = username;
		this.email = email;
		this.password = password;
	}

	public void setTokens(Set<SecTokenEntity> items) {
		this.tokens = items;
		if (items != null) this.tokenIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}

	public void setRoles(Set<SecRoleEntity> items) {
		this.roles = items;
		if (items != null) this.roleIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}

	public void setGroups(Set<SecGroupEntity> items) {
		this.groups = items;
		if (items != null) this.groupIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}

	public void setAuthorities(Set<SecAuthorityEntity> items) {
		this.authorities = items;
		if (items != null) this.authorityIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}

}
