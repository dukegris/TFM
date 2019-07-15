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

import org.hibernate.envers.Audited;
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
@Table(name="pubmed_articles")
@EntityListeners(AuditingEntityListener.class)
public class PubJournalIssueEntity extends AuditedBaseEntity {
	
	@JoinColumn(
			name = "file_id", 
			unique = false,
			nullable = false, 
			referencedColumnName = "id",
			foreignKey = @ForeignKey(
					value = ConstraintMode.NO_CONSTRAINT,
					name = "pub_article_file_fk"))
	@ManyToOne(
			optional= false,
			fetch = FetchType.EAGER,
			cascade = { CascadeType.DETACH })
	public PubFileEntity file;

	@Column(
			name = "title", 
			unique = false,
			nullable = false, 
			length = 1024)
	public String title;

	@Column(
			name = "summary", 
			unique = false,
			nullable = true, 
			length = 4096)
	public String summary;
	
}
