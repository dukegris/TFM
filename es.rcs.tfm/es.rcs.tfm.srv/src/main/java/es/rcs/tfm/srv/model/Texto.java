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
public class Texto {

	public enum TextType {
		NONE,
		OTHER,
		TITLE,
		VERNACULAR_TITLE,
		ABSTRACT,
		OTHER_ABSTRACT,
		OBSERVATIONS
	}
	public static final Tabla<String, TextType> TEXT_TYPES = new Tabla<>(TextType.NONE, TextType.OTHER);
	static {
		TEXT_TYPES.put("title", TextType.TITLE);
		TEXT_TYPES.put("vernacular", TextType.VERNACULAR_TITLE);
		TEXT_TYPES.put("abstract", TextType.ABSTRACT);
		TEXT_TYPES.put("other", TextType.OTHER_ABSTRACT);
		TEXT_TYPES.put("observations", TextType.OBSERVATIONS);
	}

	public static final Integer ORDER_TITLE = 1;
	public static final Integer ORDER_VERNACULAR_TITLE = 6;
	public static final Integer ORDER_OBSERVATIONS = 10;
	public static final Integer ORDER_ABSTRACT = 20;
	public static final Integer ORDER_OTHER_ABSTRACT = 60;

	private TextType type;
	private String subtype;
	private String language;
	private String copyright;
	private Integer order;
	private String label;
	private String category;
	private String text;

	public Texto(
			final TextType type,
			final String subtype,
			final String language,
			final String copyright,
			final Integer order,
			final String label,
			final String category,
			final String text) {
		super();
		this.type = type;
		this.subtype = subtype;
		this.language = language;
		this.copyright = copyright;
		this.order = order;
		this.label = label;
		this.category = category;
		this.text = text;
	}

}
