package es.rcs.tfm.db.model;

import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
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
		type = "Application",
		resourcePath = "applications",
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Entity
@Audited
@Table(
		name = "sec_apps",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "sec_apps_code_uk", 
						columnNames = { "app_code" })})
@EntityListeners(AuditingEntityListener.class)
public class SecApplicationEntity extends AuditedBaseEntity {

	
	@JsonProperty(
			value = "code",
			required = true)
	@Column(
			name = "app_code", 
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
			name = "app_name", 
			unique = false,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "El nombre no puede ser nulo")
	@Size(
			max = 32, 
			message = "El nombre puede sobrepasar los {max} carcateres.")
	public String name;

	
	@JsonProperty(
			value = "url",
			required = true)
	@Column(
			name = "mod_url", 
			unique = false,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "La url base no puede ser nula")
	@Size(
			max = 64, 
			message = "La url base no puede sobrepasar los {max} carcateres.")
	public String url;

	
	@JsonProperty(
			value = "moduleIds")
	@JsonApiRelationId
	@Transient
	private Set<Long> moduleIds = null;

	
	@JsonProperty(
			value = "modules",
			required = false)
	@JsonApiRelation( 
			idField = "moduleIds", 
			mappedBy = "application",
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@OneToMany(
			mappedBy = "application",
			fetch = FetchType.EAGER,
			cascade = { CascadeType.ALL })
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecModuleEntity> modules;

	
	@JsonProperty(
			value = "roleIds")
	@JsonApiRelationId
	@Transient
	private Set<Long> roleIds = null;

	
	@JsonProperty(
			value = "roles",
			required = false)
	@JsonApiRelation(
			idField = "roleIds", 
			mappedBy = "application",
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@OneToMany(
			fetch = FetchType.EAGER,
			cascade = { CascadeType.ALL },
			mappedBy = "application")
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecRoleEntity> roles;

	
	@JsonProperty(
			value = "authorityIds")
	@JsonApiRelationId
	@Transient
	private Set<Long> authorityIds = null;

	
	@JsonProperty(
			value = "authorities",
			required = false)
	@JsonApiRelation(
			idField = "authorityIds", 
			mappedBy = "application",
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@OneToMany(
			fetch = FetchType.EAGER,
			cascade = { CascadeType.ALL },
			mappedBy = "application")
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecAuthorityEntity> authorities;

	
	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	public SecApplicationEntity() {
		super();
	}

	public SecApplicationEntity(
			@NotNull(message = "El c�digo no puede ser nulo") @Size(max = 32, message = "El c�digono puede sobrepasar los {max} carcateres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 32, message = "El nombre puede sobrepasar los {max} carcateres.") String name) {
		super();
		this.code = code;
		this.name = name;
	}

	public SecApplicationEntity(
			@NotNull(message = "El c�digo no puede ser nulo") @Size(max = 32, message = "El c�digono puede sobrepasar los {max} carcateres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 32, message = "El nombre puede sobrepasar los {max} carcateres.") String name,
			@NotNull(message = "La url base no puede ser nula") @Size(max = 64, message = "La url base no puede sobrepasar los {max} carcateres.") String url) {
		super();
		this.code = code;
		this.name = name;
		this.url = url;
	}

	public void setModules(Set<SecModuleEntity> items) {
		this.modules = items;
		if (items != null) this.moduleIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}

	public void setAuthorities(Set<SecAuthorityEntity> items) {
		this.authorities = items;
		if (items != null) this.authorityIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}

	public void setRoles(Set<SecRoleEntity> items) {
		this.roles = items;
		if (items != null) this.roleIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}

}
