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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=true)
@JsonApiResource(
		type = "modules",
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
						columnNames = { "code" })})
@EntityListeners(AuditingEntityListener.class)
public class SecModuleEntity extends AuditedBaseEntity {

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

	@JsonApiRelationId
	@Column(
			name = "app_id",
			unique = false,
			nullable = false)
	private Long appId;

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
	SecAppEntity app;
}
