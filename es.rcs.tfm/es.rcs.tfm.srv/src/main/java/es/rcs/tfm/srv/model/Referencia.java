package es.rcs.tfm.srv.model;

import java.util.HashMap;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Referencia {

	private Map<String, String> ids = new HashMap<String, String>();
	private String cita = new String();

	public void addIds(Map<String, String> items) {
		if ((items!= null) && (!items.isEmpty())) this.ids.putAll(items);
	}

}
