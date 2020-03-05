package es.rcs.tfm.db.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
@Embeddable
public class PubArticleAuthorKey implements Serializable {

	private static final long serialVersionUID = -722387528497835381L;
	
	public static final String DB_AUTHOR_ID				= "aut_id";
	public static final String DB_CENTRE_ID				= "cen_id";
	public static final String DB_ARTICLE_ID			= "art_id";
	public static final String DB_PUBLICATION_ID		= "pub_id";

	@Column(
			name = DB_AUTHOR_ID, 
			unique = false,
			nullable = false)
	@NotNull(
			message = "El autor no puede ser nulo")
    private Long authorId;
	 
	@Column(
			name = DB_ARTICLE_ID, 
			unique = false,
			nullable = false)
	@NotNull(
			message = "El articulo no puede ser nulo")
	private Long articleId;

	@Column(
			name = DB_CENTRE_ID, 
			unique = false,
			nullable = true)
	private Long centreId;

	@Column(
			name = DB_PUBLICATION_ID, 
			unique = false,
			nullable = true)
	private Long publicationId;


	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	public PubArticleAuthorKey() {
		super();
	}
	
	public PubArticleAuthorKey(
			@NotNull(message = "El articulo no puede ser nulo") Long articleId,
			@NotNull(message = "El autor no puede ser nulo") Long authorId, 
			Long centreId, 
			Long publicationId) {
		super();
		this.articleId = articleId;
		this.authorId = authorId;
		this.centreId = centreId;
		this.publicationId = publicationId;
	}
 
}
