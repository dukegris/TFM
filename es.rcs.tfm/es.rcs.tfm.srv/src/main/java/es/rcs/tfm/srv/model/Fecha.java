package es.rcs.tfm.srv.model;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class Fecha {

	public enum DateType {
		NONE,
		OTHER,
		
		ACCEPTED,
		AVAIBLE,
		BEGIN,
		COMPLETE,
		CONTRIBUTION,
		CREATION,
		EDITION,
		ELECTRONIC,
		INECOLLECTION,
		ENTREZ,
		ESTABLISH,
		PMC_PUBLICATION,
		PMC_RECEIVED,
		PMC_RELEASE,
		PUBMED_PUBLICATION,
		PUBMED_RECEIVED,
		MEDLINE_PREVIOUS,
		MEDLINE_PUBLICATION,
		MEDLINE_RECEIVED,
		PUBLICATION,
		RECEIVED,
		RETRACTED,
		REVISION
	}
	
	public static final Tabla<String, DateType> DATE_TYPES = new Tabla<>(DateType.NONE, DateType.OTHER);
	static {
		
		// PubStatus
		// received | accepted | epublish | ppublish | revised | aheadofprint | retracted | ecollection | pmc | pmcr | pubmed | pubmedr | premedline | medline | medliner | entrez | pmc-release
		DATE_TYPES.put("received", DateType.RECEIVED);
		DATE_TYPES.put("accepted", DateType.ACCEPTED);
		DATE_TYPES.put("epublish", DateType.ELECTRONIC);
		DATE_TYPES.put("ppublish", DateType.PUBLICATION);
		DATE_TYPES.put("revised", DateType.REVISION);
		DATE_TYPES.put("aheadofprint", DateType.AVAIBLE);
		DATE_TYPES.put("retracted", DateType.RETRACTED);
		DATE_TYPES.put("ecollection", DateType.INECOLLECTION);
		DATE_TYPES.put("pmc", DateType.PMC_PUBLICATION);
		DATE_TYPES.put("pmcr", DateType.PMC_RECEIVED);
		DATE_TYPES.put("pmc-release", DateType.PMC_RELEASE);
		DATE_TYPES.put("pubmed", DateType.PUBMED_PUBLICATION);
		DATE_TYPES.put("pubmedr", DateType.PUBMED_RECEIVED);
		DATE_TYPES.put("premedline", DateType.MEDLINE_PREVIOUS);
		DATE_TYPES.put("medline", DateType.MEDLINE_PUBLICATION);
		DATE_TYPES.put("medliner", DateType.MEDLINE_RECEIVED);
		DATE_TYPES.put("entrez", DateType.ENTREZ);

		// DateType
		// https://dtd.nlm.nih.gov/ncbi/pubmed/att-DateType.html
		DATE_TYPES.put("Electronic", DateType.ELECTRONIC);
	}

	private DateType type;
	private String session;
	private String year;

	@Setter(
			value = AccessLevel.NONE)
	private LocalDate date = null;

	public Fecha(
			final DateType type,
			final LocalDate date) {
		super();
		this.type = type;
		this.date = date;
	}

	public Fecha(
			final DateType type) {
		super();
		this.type = type;
	}

	public void setFecha(
			final LocalDate date) {
		this.date = date;
		if (date != null)
			this.year = Integer.toString(date.getYear());
	}

}
