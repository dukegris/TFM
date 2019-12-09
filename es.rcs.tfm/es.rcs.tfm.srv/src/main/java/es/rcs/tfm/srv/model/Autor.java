package es.rcs.tfm.srv.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class Autor {

	private HashMap<String, String> ids = new HashMap<String, String>();
	private ArrayList<Centro> centros = new ArrayList<Centro>();
	private String tipo;
	private String iniciales;
	private String sufijo;
	private String nombre;
	private String apellidos;
	private String grupo;

	public void addIds(Map<String, String> items) {
		if ((items!= null) && (!items.isEmpty())) this.ids.putAll(items);
	}
	
	public void addCentros(List<Centro> items) {
		if ((items!= null) && (!items.isEmpty())) this.centros.addAll(items);
	}

}
