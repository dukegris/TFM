package es.rcs.tfm.srv.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.rcs.tfm.srv.model.Articulo.IdType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class Autor {

	public enum AuthorType {
		NONE, 
		OTHER, 
		AUTHOR,
		GROUP,
		INVESTIGATOR,
		EDITOR,
		EDITOR_GROUP
	}
	public static final Tabla<String, AuthorType> AUTHOR_TYPES = new Tabla<>(AuthorType.NONE, AuthorType.OTHER);
	static {
		
		// AuthorList
		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-AuthorList.html
		// authors | editors
		AUTHOR_TYPES.put("authors", AuthorType.AUTHOR);
		AUTHOR_TYPES.put("editors", AuthorType.EDITOR);
		
		AUTHOR_TYPES.put("investigators", AuthorType.INVESTIGATOR);
		
	}

	private Map<IdType, String> ids = new HashMap<>();
	private List<Centro> centers = new ArrayList<Centro>();
	private AuthorType type;
	private String initials;
	private String suffix;
	private String givenName;
	private String familyName;

	public void addIds(
			final Map<IdType, String> items) {
		if ((items != null) && (!items.isEmpty()))
			this.ids.putAll(items);
	}

	public void addCenters(
			final List<Centro> items) {
		if ((items!= null) && (!items.isEmpty()))
			this.centers.addAll(items);
	}

}
