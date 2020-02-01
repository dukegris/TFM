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
		type = "Group",
		resourcePath = "groups",
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Entity
@Audited
@Table(
		name = "sec_groups",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "sec_grp_code_uk", 
						columnNames = { "grp_code" })})
@EntityListeners(AuditingEntityListener.class)
public class SecGroupEntity extends AuditedBaseEntity {

	
	@JsonProperty(
			value = "code",
			required = true)
	@Column(
			name = "grp_code", 
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
			name = "grp_name", 
			unique = false,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "El nombre no puede ser nulo")
	@Size(
			max = 32, 
			message = "El nombre puede sobrepasar los {max} carcateres.")
	public String name;

	
	@JsonApiRelationId
	@JsonProperty(
			value = "userIds")
	@Transient
	private Set<Long> userIds = null;
	
	
	@JsonApiRelation(
			idField = "userIds", 
			mappedBy = "groups")
	@JsonProperty(
			value = "users")
	@ManyToMany(
			cascade = { CascadeType.DETACH },
			fetch = FetchType.LAZY,
			mappedBy = "groups")
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
			mappedBy = "groups")
	@JoinTable (
			name = "sec_group_authorities",
			joinColumns = @JoinColumn (
					name = "grp_id", 
					referencedColumnName="id"),
			inverseJoinColumns = @JoinColumn (
					name = "auth_id", 
					referencedColumnName = "id"),
			foreignKey = @ForeignKey (
					name = "sec_group_auth_fk"),
			inverseForeignKey = @ForeignKey (
					name = "sec_auth_group_fk"))
	@ManyToMany (
			cascade = { CascadeType.DETACH, CascadeType.PERSIST },
			fetch = FetchType.LAZY)
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private Set<SecAuthorityEntity> authorities;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	public SecGroupEntity() {
		super();
	}

	public SecGroupEntity(
			@NotNull(message = "El código no puede ser nulo") @Size(max = 32, message = "El códigono puede sobrepasar los {max} carcateres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 32, message = "El nombre puede sobrepasar los {max} carcateres.") String name,
			Set<SecAuthorityEntity> authorities) {
		super();
		this.code = code;
		this.name = name;
		this.setAuthorities(authorities);
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
