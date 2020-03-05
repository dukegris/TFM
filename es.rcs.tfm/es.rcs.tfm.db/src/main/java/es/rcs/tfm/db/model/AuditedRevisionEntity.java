package es.rcs.tfm.db.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.LastModifiedBy;

import es.rcs.tfm.db.setup.RevisionEntityListener;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter()
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@Table(name="cfg_revisions")
@Entity
@RevisionEntity(value = RevisionEntityListener.class)
public class AuditedRevisionEntity {

	@Transient
	private static final long serialVersionUID = 8285764735164645528L;

	@Transient
	private static final Logger LOG = LoggerFactory.getLogger(AuditedBaseEntity.class);

	@RevisionNumber
	@Id
	@GeneratedValue(
			strategy = GenerationType.IDENTITY)
	@Column(
			unique = true, 
			nullable = false,
			insertable = true,
			updatable = true)
	protected int id;

	@RevisionTimestamp
	@Column(
			unique = false, 
			nullable = false,
			insertable = true,
			updatable = true)
	private Long timestamp;
	
	@Column(
			unique = false, 
			nullable = false, 
			length = 256)
    @LastModifiedBy
    private String modifiedBy;
	
	@Transient
	public LocalDateTime getRevisionDate() {
		return Instant.ofEpochMilli(timestamp).atZone(ZoneId.of("UTC")).toLocalDateTime();
	}

}
