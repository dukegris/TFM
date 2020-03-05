package es.rcs.tfm.db.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

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
		callSuper = true)
@Table(
		name = PubArticleAuthorEntity.DB_TABLE,
		uniqueConstraints = {
				@UniqueConstraint(
						name = PubArticleAuthorEntity.DB_ID_PK, 
						columnNames = { PubArticleEntity.DB_ID }),
				@UniqueConstraint(
						name = PubArticleAuthorEntity.DB_UID_UK, 
						columnNames = { PubArticleAuthorEntity.DB_UID }),
				@UniqueConstraint(
						name = PubArticleAuthorEntity.DB_KEY_UK, 
						columnNames = { 
								PubArticleAuthorKey.DB_ARTICLE_ID,
								PubArticleAuthorKey.DB_PUBLICATION_ID,
								PubArticleAuthorKey.DB_AUTHOR_ID,
								PubArticleAuthorKey.DB_CENTRE_ID })  })
@Entity
@Audited
@EntityListeners(
		value = AuditingEntityListener.class)
public class PubArticleAuthorEntity extends AuditedBaseEntity {

	public static final String RES_AUTHTYPE					= "type";

	public static final String DB_TABLE						= "pub_article_authors";
	public static final String DB_ID_PK 					= "pub_aut_art_pk";
	public static final String DB_KEY_UK					= "pub_aut_art_key_uk";
	public static final String DB_UID_UK					= "pub_art_art_uid_uk";
	public static final String DB_ARTICLE_FK				= "pub_aut_art_art_fk";
	public static final String DB_AUTHOR_FK					= "pub_aut_art_aut_fk";
	public static final String DB_CENTRE_FK					= "pub_aut_art_cen_fk";
	public static final String DB_PUBLICATION_FK			= "pub_aut_art_pub_fk";
	
	public static final String DB_AUTHTYPE					= "aut_typ";
	
	public static final String ATT_ARTICLE					= "article";
	public static final String ATT_AUTHOR					= "author";
	public static final String ATT_CENTRE					= "centre";
	public static final String ATT_PUBLICATION				= "publication";


	@Embedded
	private PubArticleAuthorKey key;

	
	@ManyToOne(
			fetch = FetchType.LAZY,
			cascade = {CascadeType.DETACH},
			optional = false)
	@MapsId(
			value = PubArticleAuthorKey.DB_ARTICLE_ID)
	@JoinColumn(
			name = PubArticleAuthorKey.DB_ARTICLE_ID,
			referencedColumnName = PubArticleEntity.DB_ID,
			foreignKey = @ForeignKey (
					name = DB_ARTICLE_FK))
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private PubArticleEntity article;

	
	@ManyToOne(
			fetch = FetchType.LAZY,
			cascade = {CascadeType.DETACH},
			optional = true)
	@MapsId(
			value = PubArticleAuthorKey.DB_AUTHOR_ID)
	@JoinColumn(
			nullable = true,
			name = PubArticleAuthorKey.DB_AUTHOR_ID,
			referencedColumnName = PubAuthorEntity.DB_ID,
			foreignKey = @ForeignKey (
					name = DB_AUTHOR_FK))
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private PubAuthorEntity author;

	
	@ManyToOne(
			fetch = FetchType.LAZY,
			cascade = {CascadeType.DETACH},
			optional = true)
	@MapsId(
			value = PubArticleAuthorKey.DB_CENTRE_ID)
	@JoinColumn(
			nullable = true,
			name = PubArticleAuthorKey.DB_CENTRE_ID,
			referencedColumnName = PubCenterEntity.DB_ID,
			foreignKey = @ForeignKey (
					name = DB_CENTRE_FK))
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private PubCenterEntity centre;

	
	@ManyToOne(
			fetch = FetchType.LAZY,
			cascade = {CascadeType.DETACH},
			optional = true)
	@MapsId(
			value = PubArticleAuthorKey.DB_PUBLICATION_ID)
	@JoinColumn(
			nullable = true,
			name = PubArticleAuthorKey.DB_PUBLICATION_ID,
			referencedColumnName = PubPublicationEntity.DB_ID,
			foreignKey = @ForeignKey (
					name = DB_PUBLICATION_FK))
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private PubPublicationEntity publication;

	
	@Column(
			name = DB_AUTHTYPE, 
			unique = false,
			nullable = true, 
			length = 32)
	@Size(
			max = 32, 
			message = "El tipo no puede sobrepasar los {max} caracteres.")
	public String type;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public void setAuthor(final PubAuthorEntity item) {
		if (item != null) {
			this.author = item;
			if (item.getId() != null) {
				if (this.key == null) this.key = new PubArticleAuthorKey();
				this.key.setAuthorId(item.getId());
			}
		}
	}

	public void setArticle(final PubArticleEntity item) {
		if (item != null) {
			this.article = item;
			if (item.getId() != null) {
				if (this.key == null) this.key = new PubArticleAuthorKey();
				this.key.setArticleId(item.getId());
			}
		}
	}

	public void setCentre(final PubCenterEntity item) {
		if (item != null) {
			this.centre = item;
			if (item.getId() != null) {
				if (this.key == null) this.key = new PubArticleAuthorKey();
				this.key.setCentreId(item.getId());
			}
		}
	}

	public void setBook(final PubPublicationEntity item) {
		if (item != null) {
			this.publication = item;
			if (item.getId() != null) {
				if (this.key == null) this.key = new PubArticleAuthorKey();
				this.key.setPublicationId(item.getId());
			}
		}
	}

}
