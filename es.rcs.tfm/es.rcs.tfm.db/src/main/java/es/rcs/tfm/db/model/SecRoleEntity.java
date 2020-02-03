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
@EqualsAndHashCode(callSuper=true)
@JsonApiResource(
		type = "Role",
		resourcePath = "roles",
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Entity
@Audited
@Table(
		name = "sec_roles",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "sec_role_code_uk", 
						columnNames = { "role_code" })})
@EntityListeners(AuditingEntityListener.class)
public class SecRoleEntity extends AuditedBaseEntity {

	
	@JsonProperty(
			value = "code",
			required = true)
	@Column(
			name = "role_code", 
			unique = false,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "El c�digo no puede ser nulo")
	@Size(
			max = 32, 
			message = "El c�digono puede sobrepasar los {max} carcateres.")
	public String code;

	
	@JsonProperty(
			value = "name",
			required = true)
	@Column(
			name = "role_name", 
			unique = false,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "El nombre no puede ser nulo")
	@Size(
			max = 64, 
			message = "El nombre puede sobrepasar los {max} carcateres.")
	public String name;

	
	@JsonProperty(
			value = "applicationId")
	@JsonApiRelationId
	@Column(
			name = "app_id",
			unique = false,
			nullable = false)
	private Long applicationId;

	
	@JsonProperty(
			value = "application")
	@JsonApiRelation(
			idField = "applicationId",
			mappedBy = "roles",
			lookUp = LookupIncludeBehavior.NONE,
			serialize = SerializeType.ONLY_ID)
	@JoinColumn(
			name = "app_id", 
			unique = false,
			nullable = false, 
			insertable = false,
			updatable = false,
			referencedColumnName = "id",
			foreignKey = @ForeignKey(
					value = ConstraintMode.NO_CONSTRAINT,
					name = "sec_role_app_fk"))
	@ManyToOne(
			optional = false,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	public SecApplicationEntity application;

	
	@JsonProperty(
			value = "userIds")
	@JsonApiRelationId
	@Transient
	private Set<Long> userIds = null;
	
	
	@JsonProperty(
			value = "users")
	@JsonApiRelation(
			idField = "userIds", 
			mappedBy = "roles")
	@ManyToMany(
			mappedBy = "roles",
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecUserEntity> users = null;

	
	@JsonProperty(
			value = "authorityIds")
	@JsonApiRelationId
	@Transient
	private Set<Long> authorityIds = null;

	
	@JsonProperty(
			value = "authorities")
	@JsonApiRelation(
			idField = "authorityIds", 
			mappedBy = "roles")
	@JoinTable (
			name = "sec_role_authorities",
			joinColumns = @JoinColumn (
					name = "role_id", 
					referencedColumnName="id"),
			inverseJoinColumns = @JoinColumn (
					name = "auth_id", 
					referencedColumnName = "id"),
			foreignKey = @ForeignKey (
					name = "sec_role_auth_fk"),
			inverseForeignKey = @ForeignKey (
					name = "sec_auth_role_fk"))
	@ManyToMany (
			// Associations marked as mappedBy must not define database mappings like @JoinTable or @JoinColumn		
			// mappedBy = "roles",
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH, CascadeType.PERSIST })
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
			@NotNull(message = "El c�digo no puede ser nulo") @Size(max = 32, message = "El c�digono puede sobrepasar los {max} carcateres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 64, message = "El nombre puede sobrepasar los {max} carcateres.") String name,
			Set<SecAuthorityEntity> authorities) {
		super();
		this.code = code;
		this.name = name;
		this.setApplication(application);
		this.setAuthorities(authorities);
	}
	
	public void setApplication(SecApplicationEntity item) {
		this.application = item;
		this.applicationId = (item != null) ? item.getId() : null;
	}

	public void setUsers(Set<SecUserEntity> items) {
		this.users = items;
		if (items != null) this.userIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}

	public void setAuthorities(Set<SecAuthorityEntity> items) {
		this.authorities = items;
		if (items != null) this.authorityIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}

	
}