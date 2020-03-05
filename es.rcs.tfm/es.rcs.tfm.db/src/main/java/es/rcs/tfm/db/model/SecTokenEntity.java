package es.rcs.tfm.db.model;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@EqualsAndHashCode(
		callSuper = true)
@JsonApiResource(
		type = SecTokenEntity.RES_TYPE,
		resourcePath = SecTokenEntity.RES_ACTION,
		postable = false, patchable = false, deletable = false, 
		readable = true, sortable = true, filterable = true,
		pagingSpec = NumberSizePagingSpec.class )
@Table(
		name = SecTokenEntity.DB_TABLE,
		uniqueConstraints = {
				@UniqueConstraint(
						name = SecTokenEntity.DB_ID_PK, 
						columnNames = { SecTokenEntity.DB_ID }),
				@UniqueConstraint(
					name = SecTokenEntity.DB_UID_UK, 
					columnNames = { SecTokenEntity.DB_UID }) })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class SecTokenEntity extends AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(SecTokenEntity.class);

	public static final String RES_ACTION		= "tokens";
	public static final String RES_TYPE			= "Token";

	public static final String RES_SERIE		= "serie";
	public static final String RES_TOKEN		= "token";
	public static final String RES_LASTUSED		= "lastUsed";
	public static final String RES_USER_ID		= "userId";
	public static final String RES_USER			= "user";

	public static final String DB_TABLE 		= "sec_user_tokens";
	public static final String DB_ID_PK 		= "sec_tkn_pk";
	public static final String DB_UID_UK		= "sec_mod_uid_uk";
	public static final String DB_USER_ID_FK	= "sec_tkn_usr_fk";

	public static final String DB_ID			= "id";
	public static final String DB_SERIE			= "tkn_ser";
	public static final String DB_TOKEN			= "tkn_txt";
	public static final String DB_LASTUSED		= "tkn_tsp";
	public static final String DB_USER_ID		= "usr_id";

	public static final String ATT_USER			= "user";
	public static final String ATT_USER_ID		= "userId";

	
	@JsonProperty(
			value = RES_SERIE,
			required = true)
	@Column(
			name = DB_SERIE, 
			unique = false,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "La serie no puede ser nula")
	@Size(
			max = 64, 
			message = "La serie no puede sobrepasar los {max} caracteres.")
	private String serie;

	
	@JsonProperty(
			value = RES_TOKEN,
			required = true)
	@Column(
			name = DB_TOKEN, 
			unique = false,
			nullable = false, 
			length = 64)
	@NotNull(
			message = "El token no puede ser nulo")
	@Size(
			max = 64, 
			message = "El token puede sobrepasar los {max} caracteres.")
	private String token;


	@JsonProperty(
			value = RES_LASTUSED,
			required = true)
	@Column(
			name = DB_LASTUSED, 
			unique = false,
			nullable = false)
	@NotNull(
			message = "La fecha de �ltimo uso no puede ser nula")
	private LocalDateTime lastUsed;

	
	@JsonApiRelationId
	@JsonProperty(
			value = RES_USER_ID)
	@Column(
			name = DB_USER_ID,
			unique = false,
			nullable = false)
	private Long userId;

	
	@JsonApiRelation(
			idField = ATT_USER_ID,
			lookUp = LookupIncludeBehavior.NONE,
			serialize = SerializeType.ONLY_ID)
	@JsonProperty(
			value = RES_USER)
	@ManyToOne(
			optional = false,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	@EqualsAndHashCode.Exclude
	@Setter(
			value = AccessLevel.NONE)
	private SecUserEntity user;
	
	
	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public SecTokenEntity() {
		super();
	}

	public SecTokenEntity(SecUserEntity user, String serie, String token, LocalDateTime lastUsed) {
		super();
		this.setUser(user);
		this.serie = serie;
		this.token = token;
		this.lastUsed = lastUsed;
	}

	private void setUser(SecUserEntity item) {
		if (item != null) {
			this.user = item;
			if (user.getId() != null) {
				this.userId = item.getId();
			}
		}
	}

}
