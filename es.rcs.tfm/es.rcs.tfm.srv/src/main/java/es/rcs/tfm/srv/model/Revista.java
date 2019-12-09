package es.rcs.tfm.srv.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Revista {
	
	private HashMap<String, String> ids = new HashMap<String, String>();
	private String tipo;
	private String medio; // Electronic | Print
	private String abreviatura;
	private String nombre;
	private String pais;

	public void addIds(Map<String, String> items) {
		if ((items!= null) && (!items.isEmpty())) this.ids.putAll(items);
	}

	public void addId(Entry<String, String> item) {
		if (item!= null) this.ids.put(item.getKey(), item.getValue());
	}

}
