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
@EqualsAndHashCode(callSuper=true)
@JsonApiResource(
		type = "User",
		resourcePath = "users",
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Entity
@Audited
@Table(
		name = "sec_users",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "sec_usr_username_uk", 
						columnNames = { "user_name" })})
@EntityListeners(AuditingEntityListener.class)
public class SecUserEntity extends AuditedBaseEntity {

/*	
	@JsonProperty(
			value = "code",
			required = true)
	@Column(
			name = "user_code", 
			unique = false,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "El código no puede ser nulo")
	@Size(
			max = 32, 
			message = "El códigono puede sobrepasar los {max} carcateres.")
	public String code;
*/
	
	@JsonProperty(
			value = "username",
			required = true)
	@Column(
			name = "user_name", 
			unique = false,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "El nombre no puede ser nulo")
	@Size(
			max = 64, 
			message = "El nombre no puede sobrepasar los {max} carcateres.")
	public String username;

	
	@JsonApiField(
			postable=false,
			readable=false,
			patchable=false, 
			sortable=false, 
			filterable=false)
	@JsonProperty(
			value = "enabled",
			required = true)
	@Column(
			name = "user_enabled", 
			unique = false,
			nullable = false)
	@NotNull(
			message = "La habilitación del usuario no puede ser nula")
	public boolean enabled = false;

	
	@JsonProperty(
			value = "expired",
			required = true)
	@Column(
			name = "user_expired", 
			unique = false,
			nullable = false)
	@NotNull(
			message = "El estado de expiración del usuario no puede ser nulo")
	public boolean expired = true;

	
	@JsonProperty(
			value = "locked",
			required = true)
	@Column(
			name = "user_locked", 
			unique = false,
			nullable = false)
	@NotNull(
			message = "El estado de bloqueo del usuario no puede ser nulo")
	public boolean locked = true;

	
	@JsonProperty(
			value = "password",
			required = true)
	@Column(
			name = "user_pwd", 
			unique = false,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "La contraseóa no puede ser nula")
	@Size(
			max = 64, 
			message = "La contraseóa no puede sobrepasar los {max} carcateres.")
	public String password;

	
	@JsonProperty(
			value = "expiredPassword",
			required = true)
	@Column(
			name = "user_pwd_expired", 
			unique = false,
			nullable = false)
	@NotNull(
			message = "El estado de expiración de la contraseóa del usuario no puede ser nulo")
	public boolean passwordExpired = true;

	
	@JsonProperty(
			value = "email",
			required = true)
	@Column(
			name = "user_email", 
			unique = false,
			nullable = false, 
			length = 128)
	@NotNull(
			message = "El correo electrónico del usuario no puede ser nulo")
	@Size(
			max = 128, 
			message = "El correo electrónico del usuario no puede sobrepasar los {max} carcateres.")
	public String email;

	
	@JsonProperty(
			value = "emailConfirmed",
			required = true)
	@Column(
			name = "user_email_confirm", 
			unique = false,
			nullable = false, 
			length = 128)
	@NotNull(
			message = "La verificación del correo electrónico del usuario no puede ser nula")
	public boolean emailConfirmed = false;

	
	@JsonProperty(
			value = "roleIds")
	@JsonApiRelationId
	@Transient
	private Set<Long> roleIds = null;

	
	@JsonProperty(
			value = "roles")
	@JsonApiRelation(
			idField = "rolesIds", 
			mappedBy = "users")
	@JoinTable (
			name = "sec_users_roles",
			joinColumns = @JoinColumn (
					name = "user_id", 
					referencedColumnName="id"),
			inverseJoinColumns = @JoinColumn (
					name = "role_id", 
					referencedColumnName = "id"),
			foreignKey = @ForeignKey (
					name = "sec_user_role_fk"),
			inverseForeignKey = @ForeignKey (
					name = "sec_role_user_fk"))
	@ManyToMany (
			// Associations marked as mappedBy must not define database mappings like @JoinTable or @JoinColumn		
			// mappedBy = "users",
			fetch = FetchType.EAGER,
			cascade = { CascadeType.DETACH, CascadeType.PERSIST })
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecRoleEntity> roles;

	
	@JsonApiRelationId
	@JsonProperty(
			value = "groupIds")
	@Transient
	private Set<Long> groupIds = null;

	
	@JsonProperty(
			value = "groups")
	@JsonApiRelation(
			idField = "groupIds", 
			mappedBy = "users")
	@JoinTable (
			name = "sec_users_groups",
			joinColumns = @JoinColumn (
					name = "user_id", 
					referencedColumnName="id"),
			inverseJoinColumns = @JoinColumn (
					name = "group_id", 
					referencedColumnName = "id"),
			foreignKey = @ForeignKey (
					name = "sec_user_group_fk"),
			inverseForeignKey = @ForeignKey (
					name = "sec_group_user_fk"))
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

	
	@JsonProperty(
			value = "authorityIds")
	@JsonApiRelationId
	@Transient
	private Set<Long> authorityIds = null;

	
	@JsonProperty(
			value = "authorities")
	@JsonApiRelation(
			idField = "authorityIds", 
			mappedBy = "users")
	@JoinTable (
			name = "sec_user_authorities",
			joinColumns = @JoinColumn (
					name = "user_id", 
					referencedColumnName="id"),
			inverseJoinColumns = @JoinColumn (
					name = "auth_id", 
					referencedColumnName = "id"),
			foreignKey = @ForeignKey (
					name = "sec_user_auth_fk"),
			inverseForeignKey = @ForeignKey (
					name = "sec_auth_user_fk"))
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

	
	@JsonProperty(
			value = "tokenIds")
	@JsonApiRelationId
	@Transient
	private Set<Long> tokenIds = null;

	
	@JsonProperty(
			value = "tokens",
			required = false)
	@JsonApiRelation( 
			idField = "tokenIds", 
			mappedBy = "user",
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@OneToMany(
			mappedBy = "user",
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
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 64, message = "El nombre no puede sobrepasar los {max} carcateres.") String username,
			@NotNull(message = "El correo electrónico del usuario no puede ser nulo") @Size(max = 128, message = "El correo electrónico del usuario no puede sobrepasar los {max} carcateres.") String email,
			@NotNull(message = "La contraseóa no puede ser nula") @Size(max = 64, message = "La contraseóa no puede sobrepasar los {max} carcateres.") String password) {
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
