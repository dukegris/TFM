package es.rcs.tfm.srv.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.rcs.tfm.srv.model.Articulo.IdType;
import es.rcs.tfm.srv.model.Articulo.MediumType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class Libro {

	private Titulo title;
	private Titulo collectionTitle;
	private String volume;
	private String volumeTitle;
	private MediumType medium;
	private Localizacion localization;
	private List<Fecha> dates = new ArrayList<Fecha>();
	private List<Autor> authors = new ArrayList<Autor>();

	private String report;
	private String edition;
	private String editor;
	private String city;

	private HashMap<IdType, String> ids = new HashMap<IdType, String>();

	public void addIds(
			final Map<IdType, String> items) {
		if ((items != null) && (!items.isEmpty()))
			this.ids.putAll(items);
	}

	public void addDates(
			final List<Fecha> items) {
		if ((items != null) && (!items.isEmpty()))
			this.dates.addAll(items);
	}

	public void addDate(
			final Fecha item) {
		if ((item != null))
			this.dates.add(item);
	}

	public void addAuthors(
			final List<Autor> items) {
		if ((items!= null) && (!items.isEmpty()))
			this.authors.addAll(items);
	}

}
