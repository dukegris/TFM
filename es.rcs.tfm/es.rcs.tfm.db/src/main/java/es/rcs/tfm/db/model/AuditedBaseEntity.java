package es.rcs.tfm.db.model;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.crnk.core.resource.annotations.JsonApiId;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@MappedSuperclass
@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(AuditedBaseEntity.class);

	public static final String RES_ID				= "id";
	public static final String RES_UID				= "uuid";
	public static final String RES_RELEASE			= "release";
	public static final String RES_STATUS			= "status";
	public static final String RES_COMMENT			= "comments";

	public static final String DB_ID				= "id";
	public static final String DB_UID				= "uuid";
	public static final String DB_RELEASE			= "lock";
	public static final String DB_CREATED_AT		= "created_at";
	public static final String DB_CREATED_BY		= "created_by";
	public static final String DB_MODIFIED_AT		= "modified_at";
	public static final String DB_MODIFIED_BY		= "modified_by";
	public static final String DB_STATUS			= "status";
	public static final String DB_COMMENT			= "comment";


	@JsonApiId
	@JsonProperty(
			value = RES_ID)
	@Id
	@GeneratedValue(
			strategy = GenerationType.IDENTITY)
	@Column(
			name = DB_ID,
			unique = true, 
			nullable = false,
			insertable = true,
			updatable = true)
	protected Long id;


	@JsonProperty(
			value = RES_UID,
			required = true)
	@Column(
			name = DB_UID,
			nullable = false, 
			unique = true, 
			length = 256)
	@Getter(AccessLevel.NONE)
	protected String uuid;


	@JsonProperty(
			value = RES_RELEASE,
			required = true)
	@Column(
			name = DB_RELEASE,
			unique = false, 
			nullable = false)
	@Version
	protected Long lock = 1L;


	@JsonProperty(
			value = RES_STATUS,
			required = true)
	@Column(
			name = DB_STATUS,
			unique = false, 
			nullable = true, 
			length = 32)
    protected String status;	


	@JsonProperty(
			value = RES_COMMENT,
			required = false)
	@Column(
			name = DB_COMMENT,
			unique = false, 
			nullable = true, 
			length = 1024)
	@Size(
		max = 1024, 
		message = "{Los comentarios no puede sobrepasar los {max} caracteres.}")
	protected String comment;


	@JsonIgnore
	@Column(
			name = DB_CREATED_AT,
			unique = false, 
			nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;


	@JsonIgnore
	@Column(
			name = DB_CREATED_BY,
			unique = false, 
			nullable = false, 
			length = 256)
    @CreatedBy
    private String createdBy;


	@JsonIgnore
	@Column(
			name = DB_MODIFIED_AT,
			unique = false, 
			nullable = false)
    @LastModifiedDate
    private LocalDateTime modifiedAt;


	@JsonIgnore
	@Column(
			name = DB_MODIFIED_BY,
			unique = false, 
			nullable = false, 
			length = 256)
    @LastModifiedBy
    private String modifiedBy;	


	@PrePersist
	public void prePersist() {
		this.uuid = getUuid();
	}

	public String getUuid() {
		if (uuid == null) {
			uuid = UUID.randomUUID().toString();
		}
		return uuid;
	}

}
