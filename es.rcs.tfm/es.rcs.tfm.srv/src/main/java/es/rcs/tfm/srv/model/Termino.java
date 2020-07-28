package es.rcs.tfm.srv.model;

import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class Termino {

	public enum ProviderType {
		NONE,
		OTHER,
		MESH
	}

	public enum TermType {
		NONE,
		OTHER,
		DESCRIPTOR,
		QUALIFIER,
		SUPPLEMENTAL,
		PHARMALOGICAL
	}

	// SUPPLEMENTAL
	// Class 1 (Chemical and drug)
	// Class 2 (Protocol)
	// Class 3 (Disease)
	// Class 4 (Organism )
	public enum DescType {
		NONE,
		OTHER,

		CHEMICAL,
		DISEASE,
		DRUG,
		ECIN,
		ECOUT,
		ORGANISM,
		PROTOCOL,
		PUBLICATION,
		RELATED
	}

	public static final Tabla<String, DescType> DESC_TYPES = new Tabla<>(DescType.NONE, DescType.OTHER);
	static {
		
		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-SupplMeshName.html
		// Disease | Protocol | Organism
		DESC_TYPES.put("CHEMICAL", DescType.CHEMICAL);
		DESC_TYPES.put("DRUG", DescType.DRUG);
		DESC_TYPES.put("DISEASE", DescType.DISEASE);
		DESC_TYPES.put("PROTOCOL", DescType.PROTOCOL);
		DESC_TYPES.put("ORGANISM", DescType.ORGANISM);
		
		// https://www.nlm.nih.gov/mesh/xml_data_elements.html#EntryCombinationList
		// ECIN, ECOUT
		DESC_TYPES.put("ECIN", DescType.ECIN);
		DESC_TYPES.put("ECOUT", DescType.ECOUT);
		
		// https://www.nlm.nih.gov/mesh/xml_data_elements.html#PharmacologicalAction
		// CHEMICAL
		DESC_TYPES.put("CHEMICAL", DescType.CHEMICAL);
		
		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-PublicationType.html
		// https://www.nlm.nih.gov/mesh/pubtypes.html
		DESC_TYPES.put("PUBLICATION", DescType.PUBLICATION);
		
		// https://www.nlm.nih.gov/mesh/xml_data_elements.html#SeeRelatedDescriptor
		DESC_TYPES.put("RELATED", DescType.RELATED);

	}

	private ProviderType provider;
	private TermType termtype;
	private DescType desctype;
	private String code;
	private String value;
	private String data;
	private List<Termino> subterms = new ArrayList<>();
	private List<Fecha> dates = new ArrayList<>(); // pubmed | medline | entrez
	private List<Descriptor> notes = new ArrayList<>();

	public Termino(
			final TermType termtype,
			final DescType desctype) {
		this.provider = ProviderType.MESH;
		this.termtype = termtype;
		this.desctype = desctype;
	}

	public Termino(
			final TermType termtype,
			final DescType desctype,
			final String code,
			final String value) {
		this.provider = ProviderType.MESH;
		this.termtype = termtype;
		this.desctype = desctype;
		this.code = code;
		this.value = value;
	}

	public Termino(
			final TermType termtype,
			final DescType desctype,
			final String code,
			final String value,
			final String data) {
		this.provider = ProviderType.MESH;
		this.termtype = termtype;
		this.desctype = desctype;
		this.code = code;
		this.value = value;
		this.data = data;
	}

	public Termino(
			final TermType termtype,
			final DescType desctype,
			final String code,
			final String value,
			final List<Termino> subterminos) {
		super();
		this.provider = ProviderType.MESH;
		this.termtype = termtype;
		this.desctype = desctype;
		this.code = code;
		this.value = value;
		addSubterms(subterminos);
	}

	public void addSubterms(
			final List<Termino> list) {
		if ((list != null) && (!list.isEmpty()))
			this.subterms.addAll(list);
	}

	public void addDate(
			final Fecha item) {
		if ((item != null))
			this.dates.add(item);
	}

	public void addNote(
			final Descriptor item) {
		if ((item != null))
			this.notes.add(item);
	}

	public void addNotes(
			final List<Descriptor> items) {
		if ((items!= null) && (!items.isEmpty()))
			this.notes.addAll(items);
	}

}
