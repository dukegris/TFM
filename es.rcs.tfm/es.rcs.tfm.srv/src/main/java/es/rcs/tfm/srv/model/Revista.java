package es.rcs.tfm.srv.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import es.rcs.tfm.srv.model.Articulo.IdType;
import es.rcs.tfm.srv.model.Articulo.MediumType;
import es.rcs.tfm.srv.model.Articulo.PublicationType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class Revista {
	
	private Map<IdType, String> ids = new HashMap<IdType, String>();
	private PublicationType type;
	private MediumType medium;
	private String abbreviation;
	private String name;
	private String country;

	public void addIds(
			final Map<IdType, String> items) {
		if ((items != null) && (!items.isEmpty()))
			this.ids.putAll(items);
	}

	public void addId(
			final Entry<IdType, String> item) {
		if (item!= null)
			this.ids.put(item.getKey(), item.getValue());
	}

}
