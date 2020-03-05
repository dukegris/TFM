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

import org.hibernate.envers.Audited;
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
		name = PubArticleFileEntity.DB_TABLE)
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class PubArticleFileEntity {

	public static final String DB_TABLE			= "pub_file_articles";
	public static final String DB_ARTICLES_FK	= "pub_fil_art_art_fk";
	public static final String DB_FILES_FK		= "pub_fil_art_fil_fk";

	
	@EmbeddedId
	private PubArticleFileKey id;

	
	@JoinColumn(
			name = PubArticleFileKey.DB_FILE_ID,
			referencedColumnName = PubFileEntity.DB_ID,
			foreignKey = @ForeignKey (
					name = DB_FILES_FK))
	@ManyToOne(
			fetch = FetchType.LAZY,
			cascade = {CascadeType.DETACH},
			optional = false)
	@MapsId(
			value = PubArticleFileKey.DB_FILE_ID)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private PubFileEntity file;


	@JoinColumn(
			name = PubArticleFileKey.DB_ARTICLE_ID,
			referencedColumnName = PubArticleEntity.DB_ID,
			foreignKey = @ForeignKey (
					name = DB_ARTICLES_FK))
	@ManyToOne(
			fetch = FetchType.LAZY,
			cascade = {CascadeType.DETACH},
			optional = false)
	@MapsId(
			value = PubArticleFileKey.DB_ARTICLE_ID)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private PubArticleEntity article;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public void setFile(final PubFileEntity item) {
		if (item != null) {
			this.file = item;
			if (item.getId() != null) {
				if (this.id == null) this.id = new PubArticleFileKey();
				this.id.setFileId(item.getId());
			}
		}
	}

	public void setArticle(final PubArticleEntity item) {
		if (item != null) {
			this.article = item;
			if (item.getId() != null) {
				if (this.id == null) this.id = new PubArticleFileKey();
				this.id.setArticleId(item.getId());
			}
		}
	}
	
}
