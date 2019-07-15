package es.rcs.tfm.db.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
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
public class PubArticleEntity extends AuditedBaseEntity {
	
	@JoinColumn(
			name = "file_id", 
			unique = false,
			nullable = false, 
			referencedColumnName = "id",
			foreignKey = @ForeignKey(
					value = ConstraintMode.NO_CONSTRAINT,
					name = "pubmed_files_art_fil_fk"))
	@ManyToOne(
			optional= false,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	public PubFileEntity file;

	@Column(
			name = "pmid", 
			unique = false,
			nullable = true, 
			length = 16)
	public String pmid;

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
			length = 10240)
	public String summary;

	@Column(
			name = "language", 
			unique = false,
			nullable = true, 
			length = 128)
	public String language;

	@Column(
			name = "media_type", 
			unique = false,
			nullable = true, 
			length = 128)
	public String mediaType;

	@CollectionTable(
			name = "pubmed_article_ids",
			joinColumns = @JoinColumn(
						name = "article_id"))
	@MapKeyColumn(
			name="type",
			unique = false,
			nullable = false,
			length = 32)
	@Column(
			name = "value",
			unique = false,
			nullable = false,
			length = 32)
	@ElementCollection(
			fetch = FetchType.EAGER)
	private Map<String, String> identifiers = new HashMap<String, String>();
	
	@CollectionTable(
			name = "pubmed_article_keywords",
			joinColumns = @JoinColumn(
						name = "article_id"))
	@Column(
			name = "keyword",
			unique = false,
			nullable = false,
			length = 32)
	@ElementCollection(
			fetch = FetchType.EAGER)
	private Set<String> keywords = new HashSet<String>();

}
