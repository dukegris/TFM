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
public class PubArticleFileKey implements Serializable {
	
	private static final long serialVersionUID = -722387528497835381L;
	
	public static final String DB_FILE_ID				= "fil_id";
	public static final String DB_ARTICLE_ID			= "art_id";
	 
		@Column(
				name = DB_ARTICLE_ID, 
				unique = false,
				nullable = false)
		@NotNull(
				message = "El articulo no puede ser nulo")
		private Long articleId;

	@Column(
			name = DB_FILE_ID, 
			unique = false,
			nullable = false)
	@NotNull(
			message = "El fichero no puede ser nulo")
	private Long fileId;

	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	public PubArticleFileKey() {
		super();
	}
	
	public PubArticleFileKey(
			@NotNull(message = "El articulo no puede ser nulo") Long articleId,
			@NotNull(message = "El fichero no puede ser nulo") Long fileId) {
		super();
		this.articleId = articleId;
		this.fileId = fileId;
	}
 
}
