package es.rcs.tfm.db.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import javax.persistence.Version;

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
@EqualsAndHashCode(callSuper=false)
public class AuditedBaseEntity {

	
	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(AuditedBaseEntity.class);

	
	@JsonApiId
	@Id
	@GeneratedValue(
			strategy = GenerationType.IDENTITY)
	@Column(
			unique = true, 
			nullable = false,
			insertable = true,
			updatable = true)
	protected Long id;
	
	
	@JsonProperty(
			value = "uuid",
			required = true)
	@Column(
			nullable = false, 
			unique = true, 
			length = 256)
	@Getter(AccessLevel.NONE)
	protected String uuid;

	
	@JsonProperty(
			value = "release",
			required = true)
	@Column(
			unique = false, 
			nullable = false)
	@Version
	protected Long lock = 1L;
	
	
	@JsonProperty(
			value = "status",
			required = true)
	@Column(
			unique = false, 
			nullable = true, 
			length = 32)
    protected String status;	
	
	
	@JsonProperty(
			value = "comment",
			required = false)
	@Column(
			unique = false, 
			nullable = true, 
			length = 1024)
    protected String comment;	

	
	@JsonIgnore
	@Column(
			unique = false, 
			nullable = false)
    @CreatedDate
    private ZonedDateTime createdAt;
	
	
	@JsonIgnore
	@Column(
			unique = false, 
			nullable = false, 
			length = 256)
    @CreatedBy
    private String createdBy;
	
	
	@JsonIgnore
	@Column(
			unique = false, 
			nullable = false)
    @LastModifiedDate
    private ZonedDateTime modifiedAt;
	
	@JsonIgnore
	@Column(
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
