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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@MappedSuperclass
@Getter()
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class AuditedBaseEntity {

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(AuditedBaseEntity.class);

	@Id
	@GeneratedValue(
			strategy = GenerationType.IDENTITY)
	@Column(
			unique = true, 
			nullable = false,
			insertable = true,
			updatable = true)
	protected Long id;
	
	@Column(
			nullable = false, 
			unique = true, 
			length = 256)
	@Getter(AccessLevel.NONE)
	protected String uuid;

	@Version
	@Column(
			unique = false, 
			nullable = false)
	protected Long lock = 1L;

	@Column(
			unique = false, 
			nullable = false)
    @CreatedDate
    private ZonedDateTime createdAt;
	
	@Column(
			unique = false, 
			nullable = false, 
			length = 256)
    @CreatedBy
    private String createdBy;
	
	@Column(
			unique = false, 
			nullable = false)
    @LastModifiedDate
    private ZonedDateTime modifiedAt;
	
	@Column(
			unique = false, 
			nullable = false, 
			length = 256)
    @LastModifiedBy
    private String modifiedBy;	
	
	@Column(
			unique = false, 
			nullable = true, 
			length = 32)
    protected String status;	
	
	@Column(
			unique = false, 
			nullable = true, 
			length = 1024)
    protected String comment;	

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
