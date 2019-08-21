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
						columnNames = { "type", "filename" })})
@EntityListeners(AuditingEntityListener.class)
public class PubFileEntity extends AuditedBaseEntity {

	public static final String FTP_PUBMED = "FTP_PUBMED";
	public static final String FTP_PMC = "FTP_PMC";
	public static final String FTP_PMC_TEXT = "FTP_PMC_TEXT";
	
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
	private String type;

	@Column(
			name = "gzdirectory", 
			unique = false,
			nullable = true, 
			length = 256)
	public String gzDirectory;

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
			name = "uncompressedfilename", 
			unique = false,
			nullable = true, 
			length = 32)
	public String uncompressedFileName;
	
}
