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
public class Libro {

	private Titulo titulo;
	private Titulo tituloColeccion;
	private String volumen;
	private String tituloVolumen;
	private String medio; // internet |
	private Localizacion localizacion;
	private ArrayList<Fecha> fechas = new ArrayList<Fecha>();
	private ArrayList<Autor> autores = new ArrayList<Autor>();
	
	private String informe;
	private String edicion;
	private String editor;
	private String ciudad;
	
	private HashMap<String, String> ids = new HashMap<String, String>();

	public void addIds(Map<String, String> items) {
		if ((items!= null) && (!items.isEmpty())) this.ids.putAll(items);
	}

	public void addFechas(List<Fecha> items) {
		if ((items!= null) && (!items.isEmpty())) this.fechas.addAll(items);
	}

	public void addFecha(Fecha item) {
		if ((item!= null)) this.fechas.add(item);
	}

	public void addAutores(List<Autor> items) {
		if ((items!= null) && (!items.isEmpty())) this.autores.addAll(items);
	}

}
