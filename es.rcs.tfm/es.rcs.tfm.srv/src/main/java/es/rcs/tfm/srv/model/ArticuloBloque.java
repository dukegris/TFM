package es.rcs.tfm.srv.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class ArticuloBloque {

	public enum BlockType {
		NONE,
		
		TITLE,
		ABSTRACT,
		OTHER
	}

	private Integer offset = 0;
	private BlockType type = BlockType.NONE;
	private String text = "";
	private Map<String, ArticuloBloqueAnotacion> notes = new HashMap<String, ArticuloBloqueAnotacion>();

	public boolean isTitle() {
		return BlockType.TITLE.equals(this.type);	
	}
	
	public void addNote(
			final String key, 
			final ArticuloBloqueAnotacion value) {
		if (StringUtils.isBlank(key) || (value == null))
			return;
		notes.put(key, value);
	}

	public void addNotes(
			final Map<String, ArticuloBloqueAnotacion> items) {
		if ((items != null) && (!items.isEmpty()))
			this.notes.putAll(items);
	}

	public void recalculateOffset(
			final int addvalue) {
		notes.forEach((k, n) -> {
			n.getPos().forEach(p -> {
				p.setOffset(p.getOffset() + addvalue);
			});
		});
	}
	
}
