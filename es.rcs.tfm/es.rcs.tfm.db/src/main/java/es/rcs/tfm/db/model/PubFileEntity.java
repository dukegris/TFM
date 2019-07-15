package es.rcs.tfm.db.model;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=true)
@Entity
@Audited
@Table(
		name = "pubmed_files",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "pubmed_files_source_uk", 
						columnNames = { "source", "filename" })})
@EntityListeners(AuditingEntityListener.class)
public class PubFileEntity extends AuditedBaseEntity {

	public static final String FTP_PUBMED = "FTP_PUBMED";
	
	@Column(
			name = "source", 
			unique = false,
			nullable = false, 
			length = 16)
	public String source;
	
	@Column(
			name = "filename", 
			unique = false,
			nullable = false, 
			length = 32)
	public String fileName;

	@Column(
			name = "type", 
			unique = false, 
			nullable = true)
	private Integer type;

	@Column(
			name = "gzfilename", 
			unique = false,
			nullable = true, 
			length = 32)
	public String gzFileName;

	@Column(
			name = "gzsize", 
			unique = false, 
			nullable = true)
	private Long gzSize;

	@Column(
			name = "gztimestamp", 
			unique = false, 
			nullable = true)
    @CreatedDate
    private ZonedDateTime gzTimeStamp;
	
	@Column(
			name = "md5filename", 
			unique = false,
			nullable = true, 
			length = 32)
	public String md5FileName;
	
	@Column(
			name = "xmlfilename", 
			unique = false,
			nullable = true, 
			length = 32)
	public String xmlFilename;

	@Column(
			name = "xmlsize", 
			unique = false, 
			nullable = true)
	private Long xmlSize;

	@Column(
			name = "xmltimestamp", 
			unique = false, 
			nullable = true)
    @CreatedDate
    private ZonedDateTime xmlTimeStamp;
	
}
