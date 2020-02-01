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
		type = "Authority",
		resourcePath = "authorities",
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Entity
@Audited
@Table(
		name = "sec_authorities",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "sec_aut_code_uk", 
						columnNames = { "auth_code" })})
@EntityListeners(AuditingEntityListener.class)
public class SecAuthorityEntity extends AuditedBaseEntity {

	
	@JsonProperty(
			value = "code",
			required = true)
	@Column(
			name = "auth_code", 
			unique = false,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "El código no puede ser nulo")
	@Size(
			max = 32, 
			message = "El códigono puede sobrepasar los {max} carcateres.")
	public String code;

	
	@JsonProperty(
			value = "name",
			required = true)
	@Column(
			name = "auth_name", 
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
			value = "users")
	@JsonApiRelation(
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
					name = "sec_mods_app_fk"))
	@ManyToOne(
			optional = false,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private SecApplicationEntity application;

	
	@JsonProperty(
			value = "userIds")
	@JsonApiRelationId
	@Transient
	private Set<Long> userIds = null;
	
	
	@JsonProperty(
			value = "users")
	@JsonApiRelation(
			idField = "userIds", 
			mappedBy = "authorities")
	@ManyToMany(
			cascade = { CascadeType.DETACH },
			fetch = FetchType.LAZY,
			mappedBy = "authorities")
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecUserEntity> users = null;

	
	@JsonProperty(
			value = "roleIds")
	@JsonApiRelationId
	@Transient
	private Set<Long> roleIds = null;
	
	
	@JsonProperty(
			value = "roles")
	@JsonApiRelation(
			idField = "roleIds", 
			mappedBy = "authorities")
	@ManyToMany(
			cascade = { CascadeType.DETACH },
			fetch = FetchType.LAZY,
			mappedBy = "authorities")
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecRoleEntity> roles = null;

	
	@JsonProperty(
			value = "groupIds")
	@JsonApiRelationId
	@Transient
	private Set<Long> groupIds = null;
	
	
	@JsonProperty(
			value = "groups")
	@JsonApiRelation(
			idField = "groupIds", 
			mappedBy = "authorities")
	@ManyToMany(
			cascade = { CascadeType.DETACH },
			fetch = FetchType.LAZY,
			mappedBy = "authorities")
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
			@NotNull(message = "El código no puede ser nulo") @Size(max = 32, message = "El códigono puede sobrepasar los {max} carcateres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 64, message = "El nombre puede sobrepasar los {max} carcateres.") String name) {
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
