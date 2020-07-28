package es.rcs.tfm.srv.model;

import java.util.HashMap;
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
public class Referencia {

	private Map<IdType, String> ids = new HashMap<>();
	private String cita = new String();

	public void addIds(
			final Map<IdType, String> items) {
		if ((items!= null) && (!items.isEmpty()))
			this.ids.putAll(items);
	}

}
