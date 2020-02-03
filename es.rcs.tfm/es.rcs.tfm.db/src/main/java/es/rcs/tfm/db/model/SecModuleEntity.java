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
		type = "Module",
		resourcePath = "modules",
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Entity
@Audited
@Table(
		name = "sec_modules",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "sec_mods_code_uk", 
						columnNames = { "mod_code" })})
@EntityListeners(AuditingEntityListener.class)
public class SecModuleEntity extends AuditedBaseEntity {

	
	@JsonProperty(
			value = "code",
			required = true)
	@Column(
			name = "mod_code", 
			unique = false,
			nullable = false, 
			length = 32)
	@NotNull(
			message = "El código no puede ser nulo")
	@Size(
			max = 32, 
			message = "El códigono no puede sobrepasar los {max} carcateres.")
	public String code;

	
	@JsonProperty(
			value = "name",
			required = true)
	@Column(
			name = "mod_name", 
			unique = false,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "El nombre no puede ser nulo")
	@Size(
			max = 64, 
			message = "El nombre no puede sobrepasar los {max} carcateres.")
	public String name;

	
	@JsonProperty(
			value = "icon",
			required = true)
	@Column(
			name = "mod_icon", 
			unique = false,
			nullable = true, 
			length = 64)
	@Size(
			max = 64, 
			message = "El identificador del icono no puede sobrepasar los {max} carcateres.")
	public String icon;

	
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
			mappedBy = "modules",
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
	SecApplicationEntity application;

	
	@JsonProperty(
			value = "functionIds")
	@JsonApiRelationId
	@Transient
	private Set<Long> functionIds = null;

	
	@JsonProperty(
			value = "functions",
			required = false)
	@JsonApiRelation( 
			idField = "functionIds", 
			mappedBy = "module",
			serialize = SerializeType.EAGER,
			lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
			repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
	@OneToMany(
			mappedBy = "module",
			fetch = FetchType.EAGER,
			cascade = { CascadeType.ALL } )
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
			@NotNull(message = "El código no puede ser nulo") @Size(max = 32, message = "El códigono puede sobrepasar los {max} carcateres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 64, message = "El nombre puede sobrepasar los {max} carcateres.") String name) {
		super();
		this.code = code;
		this.name = name;
		this.setApplication(application);
	}

	public SecModuleEntity(
			SecApplicationEntity application,
			@NotNull(message = "El código no puede ser nulo") @Size(max = 32, message = "El códigono no puede sobrepasar los {max} carcateres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 64, message = "El nombre no puede sobrepasar los {max} carcateres.") String name,
			@Size(max = 64, message = "El identificador del icono no puede sobrepasar los {max} carcateres.") String icon,
			@NotNull(message = "La url base no puede ser nula") @Size(max = 64, message = "La url base no puede sobrepasar los {max} carcateres.") String url) {
		super();
		this.code = code;
		this.name = name;
		this.icon = icon;
		this.url = url;
		this.setApplication(application);
	}
	
	public void setApplication(SecApplicationEntity item) {
		this.application = item;
		this.applicationId = (item != null) ? item.getId() : null;
	}

	public void setFunctions(Set<SecFunctionEntity> items) {
		this.functions = items;
		if (items != null) this.functionIds = items.stream().map(f -> f.getId()).collect(Collectors.toSet());
	}
	
	
}
