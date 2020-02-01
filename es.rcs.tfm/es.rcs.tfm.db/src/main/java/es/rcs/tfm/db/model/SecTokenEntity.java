package es.rcs.tfm.db.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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
		type = "Token",
		resourcePath = "tokens",
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Entity
@Audited
@Table(
		name = "sec_user_tokens")
@EntityListeners(AuditingEntityListener.class)
public class SecTokenEntity extends AuditedBaseEntity {

	
	@JsonProperty(
			value = "serie",
			required = true)
	@Column(
			name = "tk_serie", 
			unique = false,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "La serie no puede ser nula")
	@Size(
			max = 64, 
			message = "ELa serie no puede sobrepasar los {max} carcateres.")
	public String serie;

	
	@JsonProperty(
			value = "token",
			required = true)
	@Column(
			name = "tk_token", 
			unique = false,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "El token no puede ser nulo")
	@Size(
			max = 64, 
			message = "El token puede sobrepasar los {max} carcateres.")
	public String token;


	@JsonProperty(
			value = "lastUsed",
			required = true)
	@Column(
			name = "tk_lastused", 
			unique = false,
			nullable = false)
	@NotNull(
			message = "La fehca de último uso no puede ser nula")
	private Date lastUsed;

	
	@JsonProperty(
			value = "userId")
	@JsonApiRelationId
	@Column(
			name = "usr_id",
			unique = false,
			nullable = false)
	private Long userId;

	
	@JsonProperty(
			value = "user")
	@JsonApiRelation(
			idField = "userId",
			lookUp = LookupIncludeBehavior.NONE,
			serialize = SerializeType.ONLY_ID)
	@ManyToOne(
			optional = false,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	SecUserEntity user;
	
	
	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	public SecTokenEntity() {
		super();
	}

	public SecTokenEntity(SecUserEntity user, String serie, String token, Date lastUsed) {
		super();
		this.setUser(user);
		this.serie = serie;
		this.token = token;
		this.lastUsed = lastUsed;
	}

	private void setUser(SecUserEntity user) {
		this.user = user;
		this.userId = (user != null) ? user.getId() : null;
	}

}
