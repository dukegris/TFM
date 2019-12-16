package es.rcs.tfm.db.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.crnk.core.queryspec.pagingspec.NumberSizePagingSpec;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=true)
@JsonApiResource(
		type = "apps",
		resourcePath = "apps",
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
						columnNames = { "code" })})
@EntityListeners(AuditingEntityListener.class)
public class SecAppEntity extends AuditedBaseEntity {

	@JsonProperty(
			value = "code",
			required = true)
	@Column(
			name = "code", 
			unique = false,
			nullable = false, 
			length = 32)
	@NotNull(message = "El código no puede ser nulo")
	@Size(max = 32, message = "El códigono puede sobrepasar los {max} carcateres.")
	public String code;

	@JsonProperty(
			value = "name",
			required = true)
	@Column(
			name = "name", 
			unique = false,
			nullable = false, 
			length = 32)
	@NotNull(message = "El nombre no puede ser nulo")
	@Size(max = 32, message = "El nombre puede sobrepasar los {max} carcateres.")
	public String name;

	@JsonApiRelation(
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER, 
			mappedBy = "app")
	@OneToMany(
			mappedBy = "app",
			fetch = FetchType.EAGER,
			cascade = { CascadeType.ALL })
	private Set<SecModuleEntity> modules;

}
