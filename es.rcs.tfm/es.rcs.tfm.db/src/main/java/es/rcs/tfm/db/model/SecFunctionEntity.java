package es.rcs.tfm.db.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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
		type = "Function",
		resourcePath = "functions",
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Entity
@Audited
@Table(
		name = "sec_functions",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "sec_func_code_uk", 
						columnNames = { "func_code" })})
@EntityListeners(AuditingEntityListener.class)
public class SecFunctionEntity extends AuditedBaseEntity {

	
	@JsonProperty(
			value = "code",
			required = true)
	@Column(
			name = "func_code", 
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
			name = "func_name", 
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
			value = "moduleId")
	@JsonApiRelationId
	@Column(
			name = "mod_id",
			unique = false,
			nullable = false)
	private Long moduleId;

	
	@JsonProperty(
			value = "module")
	@JsonApiRelation(
			idField = "moduleId",
			mappedBy = "functions",
			lookUp = LookupIncludeBehavior.NONE,
			serialize = SerializeType.ONLY_ID)
	@JoinColumn(
			name = "mod_id", 
			unique = false,
			nullable = false, 
			insertable = false,
			updatable = false,
			referencedColumnName = "id",
			foreignKey = @ForeignKey(
					value = ConstraintMode.NO_CONSTRAINT,
					name = "sec_func_mods_fk"))
	@ManyToOne(
			optional = false,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private SecModuleEntity module;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	public SecFunctionEntity() {
		super();
	}

	public SecFunctionEntity(
			SecModuleEntity module,
			@NotNull(message = "El c�digo no puede ser nulo") @Size(max = 32, message = "El c�digono puede sobrepasar los {max} carcateres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 64, message = "El nombre puede sobrepasar los {max} carcateres.") String name) {
		super();
		this.code = code;
		this.name = name;
		this.setModule(module);
	}

	public SecFunctionEntity(
			SecModuleEntity module,
			@NotNull(message = "El c�digo no puede ser nulo") @Size(max = 32, message = "El c�digono puede sobrepasar los {max} carcateres.") String code,
			@NotNull(message = "El nombre no puede ser nulo") @Size(max = 64, message = "El nombre puede sobrepasar los {max} carcateres.") String name,
			@NotNull(message = "La url base no puede ser nula") @Size(max = 64, message = "La url base no puede sobrepasar los {max} carcateres.") String url) {
		super();
		this.code = code;
		this.name = name;
		this.url = url;
		this.setModule(module);
	}
	
	public void setModule(SecModuleEntity item) {
		this.module = item;
		this.moduleId = (item != null) ? item.getId() : null;
	}
	
	
}
