package es.rcs.tfm.srv.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class Titulo {

	private String title;
	private String bookId;
	private String partId;
	private String sectionId;

	public Titulo(
			final String title) {
		super();
		this.title = title;
	}

}
