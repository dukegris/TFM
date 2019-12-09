package es.rcs.tfm.srv.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class BloqueAnotado {

	public static final String PASSAGE_TYPE				= "type";
	public static final String PASSAGE_TYPE_TITLE		= "title";
	public static final String PASSAGE_TYPE_ABSTRACT	= "abstract";

	private Integer offset = 0;
	private String type = "";
	private String text = "";
	private Map<String, Anotacion> notes = new HashMap<String, Anotacion>();

	public void addNotes(Map<String, Anotacion> items) {
		if ((items!= null) && (!items.isEmpty())) this.notes.putAll(items);
	}
	public void addInfonType(Map<String, String> items) {
		if ((items!= null) && (!items.isEmpty())) {
	 		// Realmente el DTD de BioC especifica una lista. Para tmBioC el tipo lo define 
			// el infon "type". Los valores que puede tener son los definidos de manera estática
			this.type = items.get(PASSAGE_TYPE);
		}
	}
	public boolean isTitle() {
		return PASSAGE_TYPE_TITLE.equals(type);
	}
	public void addNote(String key, Anotacion value) {
		if (StringUtils.isBlank(key) || (value == null)) return;
		notes.put(key, value);
	}
}
