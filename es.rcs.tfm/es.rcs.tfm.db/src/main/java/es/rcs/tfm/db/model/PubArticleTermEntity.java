package es.rcs.tfm.db.model;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
@Table(
		name = PubArticleTermEntity.DB_TABLE)
@Entity
@EntityListeners(
		value = AuditingEntityListener.class)
public class PubArticleTermEntity {

	public static final String DB_TABLE			= "pub_article_terms";
	public static final String DB_ARTICLE_FK	= "pub_art_ter_art_fk";
	public static final String DB_TERM_FK		= "pub_art_ter_ter_fk";
	
	@EmbeddedId
	private PubArticleTermKey id;

	
	@JoinColumn(
			name = PubArticleTermKey.DB_TERM_ID,
			referencedColumnName = PubTermEntity.DB_ID,
			foreignKey = @ForeignKey (
					name = DB_TERM_FK))
	@ManyToOne(
			fetch = FetchType.LAZY,
			cascade = {CascadeType.DETACH},
			optional = false)
	@MapsId(
			value = PubArticleTermKey.DB_TERM_ID)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private PubTermEntity term;


	@JoinColumn(
			name = PubArticleTermKey.DB_ARTICLE_ID,
			referencedColumnName = PubArticleEntity.DB_ID,
			foreignKey = @ForeignKey (
					name = DB_ARTICLE_FK))
	@ManyToOne(
			fetch = FetchType.LAZY,
			cascade = {CascadeType.DETACH},
			optional = false)
	@MapsId(
			value = PubArticleTermKey.DB_ARTICLE_ID)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private PubArticleEntity article;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public void setTerm(final PubTermEntity item) {
		if (item != null) {
			this.term = item;
			if (item.getId() != null) {
				if (this.id == null) this.id = new PubArticleTermKey();
				this.id.setTermId(item.getId());
			}
		}
	}

	public void setArticle(final PubArticleEntity item) {
		if (item != null) {
			this.article = item;
			if (item.getId() != null) {
				if (this.id == null) this.id = new PubArticleTermKey();
				this.id.setArticleId(item.getId());
			}
		}
	}
	
}
