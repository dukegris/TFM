package es.rcs.tfm.srv.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class Localizacion {

	public enum LocalizationType {
		NONE,
		OTHER,
		DOI,
		PII,
		PAGE
	}
	
	public static final Tabla<String, LocalizationType> LOCALIZATION_TYPES = new Tabla<>(LocalizationType.NONE, LocalizationType.OTHER);
	static {
		
		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-ELocationID.html
		// doi | pii
		LOCALIZATION_TYPES.put("doi", LocalizationType.DOI);
		LOCALIZATION_TYPES.put("pii", LocalizationType.PII);
		
	}

	private LocalizationType type = LocalizationType.NONE;
	private String path = new String();
	private String initialPage = new String();
	private String endPage = new String();
	private String reference = new String();

	public Localizacion() {
		super();
	}
	public Localizacion(
			final LocalizationType type,
			final String path) {
		super();
		this.type = type;
		this.path = path;
	}

	public Localizacion(
			final String initialPage,
			final String endPage,
			final String reference) {
		super();
		this.type = LocalizationType.PAGE;
		this.initialPage = initialPage;
		this.endPage = endPage;
		this.reference = reference;
	}

}