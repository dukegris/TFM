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
public class PubArticlePublicationKey implements Serializable {

	private static final long serialVersionUID = 52952585016831500L;

	public static final String DB_PUBLICATION_ID		= "pub_id";
	public static final String DB_ARTICLE_ID			= "art_id";

	@Column(
			name = DB_PUBLICATION_ID, 
			unique = false,
			nullable = false)
	@NotNull(
			message = "La publicacion no puede ser nula")
	private Long publicationId;
 
	@Column(
			name = DB_ARTICLE_ID, 
			unique = false,
			nullable = false)
	@NotNull(
			message = "El articulo no puede ser nulo")
	private Long articleId;

	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public PubArticlePublicationKey() {
		super();
	}

	public PubArticlePublicationKey(
			@NotNull(message = "El articulo no puede ser nulo") Long articleId,
			@NotNull(message = "La publicacion no puede ser nula") Long publicationId) {
		super();
		this.articleId = articleId;
		this.publicationId = publicationId;
	}

}
