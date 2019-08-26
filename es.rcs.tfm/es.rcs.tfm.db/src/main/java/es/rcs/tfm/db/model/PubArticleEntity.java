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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
@Table(name="pubmed_articles",
	uniqueConstraints = {
			@UniqueConstraint(
					name = "pubmed_articles_pmctxt_uk", 
					columnNames = { "file_pmctxt_id" }),
			@UniqueConstraint(
					name = "pubmed_articles_pmc_uk", 
					columnNames = { "file_pmc_id" })})
@EntityListeners(AuditingEntityListener.class)
public class PubArticleEntity extends AuditedBaseEntity {
	
	@JoinColumn(
			name = "file_pubmed_id", 
			unique = false,
			nullable = false, 
			referencedColumnName = "id",
			foreignKey = @ForeignKey(
					value = ConstraintMode.NO_CONSTRAINT,
					name = "pubmed_article_pubmed_fk"))
	@ManyToOne(
			optional= false,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	public PubFileEntity filePubmed;
	
	@JoinColumn(
			name = "file_pmc_id", 
			unique = false,
			nullable = true, 
			referencedColumnName = "id",
			foreignKey = @ForeignKey(
					value = ConstraintMode.NO_CONSTRAINT,
					name = "pubmed_article_pmc_fk"))
	@OneToOne(
			optional= false,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	public PubFileEntity filePmc;
	
	@JoinColumn(
			name = "file_pmctxt_id", 
			unique = false,
			nullable = true, 
			referencedColumnName = "id",
			foreignKey = @ForeignKey(
					value = ConstraintMode.NO_CONSTRAINT,
					name = "pubmed_article_pmctxt_fk"))
	@OneToOne(
			optional= false,
			fetch = FetchType.LAZY,
			cascade = { CascadeType.DETACH })
	public PubFileEntity filePmcTxt;

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

	@Lob
	@Column(
			name = "text", 
			unique = false,
			nullable = true, 
			length = 65536)
	public String text;

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
