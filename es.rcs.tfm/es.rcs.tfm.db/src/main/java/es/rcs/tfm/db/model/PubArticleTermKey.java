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
public class PubArticleTermKey implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String DB_TERM_ID			= "ter_id";
	public static final String DB_ARTICLE_ID		= "art_id";

	@Column(
			name = DB_TERM_ID, 
			unique = false,
			nullable = false)
	@NotNull(
			message = "El término no puede ser nulo")
	private Long termId;
 
	@Column(
			name = DB_ARTICLE_ID, 
			unique = false,
			nullable = false)
	@NotNull(
			message = "El articulo no puede ser nulo")
	private Long articleId;

	// CONSTRUCTOR -------------------------------------------------------------------------------------------

	public PubArticleTermKey() {
		super();
	}

	public PubArticleTermKey(
			@NotNull(message = "El articulo no puede ser nulo") Long articleId,
			@NotNull(message = "El término no puede ser nulo") Long termId) {
		super();
		this.articleId = articleId;
		this.termId = termId;
	}

}
