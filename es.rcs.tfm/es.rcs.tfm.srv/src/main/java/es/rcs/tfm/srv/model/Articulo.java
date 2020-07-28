package es.rcs.tfm.srv.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import es.rcs.tfm.db.model.PubArticleEntity;
import es.rcs.tfm.solr.model.PubArticleIdx;
import es.rcs.tfm.srv.model.ArticuloBloque.BlockType;
import es.rcs.tfm.srv.model.Texto.TextType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class Articulo {

	public static final String SEPARATOR = ";";

	public enum StatusType {
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
		IN_ECOLLECTION,
		IN_PRINT,
		IN_REVIEW,
		ENTREZ,
		ESTABLISH,
		PMC_PUBLICATION,
		PMC_RECEIVED,
		PMC_RELEASE,
		PUBMED,
		PUBMED_PUBLICATION,
		PUBMED_RECEIVED,
		MEDLINE,
		MEDLINE_OLD,
		MEDLINE_PREVIOUS,
		MEDLINE_PUBLICATION,
		MEDLINE_RECEIVED,
		PUBLICATION,
		PUBLISHER,
		RECEIVED,
		RETRACTED,
		REVISION
	}	
	public static final Tabla<String, StatusType> STATUS_TYPES = new Tabla<>(StatusType.NONE, StatusType.OTHER);
	static {		
		// PubStatus
		// received | accepted | epublish | ppublish | revised | aheadofprint | retracted | ecollection | pmc | pmcr | pubmed | pubmedr | premedline | medline | medliner | entrez | pmc-release
		STATUS_TYPES.put("received", StatusType.RECEIVED);
		STATUS_TYPES.put("accepted", StatusType.ACCEPTED);
		STATUS_TYPES.put("epublish", StatusType.ELECTRONIC);
		STATUS_TYPES.put("ppublish", StatusType.PUBLICATION);
		STATUS_TYPES.put("revised", StatusType.REVISION);
		STATUS_TYPES.put("aheadofprint", StatusType.AVAIBLE);
		STATUS_TYPES.put("retracted", StatusType.RETRACTED);
		STATUS_TYPES.put("ecollection", StatusType.IN_ECOLLECTION);
		STATUS_TYPES.put("pmc", StatusType.PMC_PUBLICATION);
		STATUS_TYPES.put("pmcr", StatusType.PMC_RECEIVED);
		STATUS_TYPES.put("pmc-release", StatusType.PMC_RELEASE);
		STATUS_TYPES.put("pubmed", StatusType.PUBMED_PUBLICATION);
		STATUS_TYPES.put("pubmedr", StatusType.PUBMED_RECEIVED);
		STATUS_TYPES.put("premedline", StatusType.MEDLINE_PREVIOUS);
		STATUS_TYPES.put("medline", StatusType.MEDLINE_PUBLICATION);
		STATUS_TYPES.put("medliner", StatusType.MEDLINE_RECEIVED);
		STATUS_TYPES.put("entrez", StatusType.ENTREZ);

		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-MedlineCitation.html
		// Completed | In-Process | PubMed-not-MEDLINE | In-Data-Review | Publisher | MEDLINE | OLDMEDLINE
		STATUS_TYPES.put("Completed", StatusType.COMPLETE);
		STATUS_TYPES.put("In-Process", StatusType.IN_PRINT);
		STATUS_TYPES.put("PubMed-not-MEDLINE", StatusType.PUBMED);
		STATUS_TYPES.put("In-Data-Review", StatusType.IN_REVIEW);
		STATUS_TYPES.put("Publisher", StatusType.PUBLISHER);
		//STATUS_TYPES.put("MEDLINE", StatusType.MEDLINE);
		STATUS_TYPES.put("OLDMEDLINE", StatusType.MEDLINE_OLD);		
	}
	
	public enum PublicationType {
		NONE,
		OTHER,
		
		BOOK,
		JOURNAL
	}

	// Print | Print-Electronic | Electronic | Electronic-Print | Electronic-eCollection
	public enum MediumType {
		NONE,
		OTHER,
		
		ELECTRONIC,
		ELECTRONIC_COLLECTION,
		ELECTRONIC_PRINT,
		INTERNET,
		PRINT,
		PRINT_ELECTRONIC
	}	
	public static final Tabla<String, MediumType> MEDIUM_TYPES = new Tabla<>(MediumType.NONE, MediumType.OTHER);
	static {
		
		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-ISSN.html
		// Electronic | Print
		MEDIUM_TYPES.put("Electronic", MediumType.ELECTRONIC);
		MEDIUM_TYPES.put("Print", MediumType.PRINT);

		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-Article.html
		// Print | Print-Electronic | Electronic | Electronic-Print | Electronic-eCollection
		MEDIUM_TYPES.put("Print-Electronic", MediumType.PRINT_ELECTRONIC);
		MEDIUM_TYPES.put("Electronic-Print", MediumType.ELECTRONIC_PRINT);
		MEDIUM_TYPES.put("Electronic-eCollection", MediumType.ELECTRONIC_COLLECTION);
		
		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-Medium.html
		// Internet CDATA
		MEDIUM_TYPES.put("Internet", MediumType.INTERNET);
	}

	public enum IdType {
		NONE,
		OTHER,

		ARPL,
		BOOKACCESS,
		CLML,
		CPC,
		CPFH,
		DOI,
		IND,
		ISBN,
		ISSN,
		KIE,
		MID,
		NASA,
		NLM,
		NRCBL,
		ORCID,
		MEDLINE,
		PII,
		POP,
		PIP,
		PMC,
		PMC_BOOK,
		PMC_ID,
		PMC_PID,
		PMP_ID,
		PUBMED,
		QCIM,
		SICI	
	}	
	public static final Tabla<String, IdType> ID_TYPES = new Tabla<>(IdType.NONE, IdType.OTHER);
	static {		
		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-ELocationID.html
		// doi | pii
		
		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-ArticleId.html
		// doi | pii | pmcpid | pmpid | pmc | mid | sici | pubmed | medline | pmcid | pmcbook | bookaccession
		ID_TYPES.put("doi", IdType.DOI);
		ID_TYPES.put("pii", IdType.PII);
		ID_TYPES.put("pmcpid", IdType.PMC_PID);
		ID_TYPES.put("pmpid", IdType.PMP_ID);
		ID_TYPES.put("pmc", IdType.PMC);
		ID_TYPES.put("mid", IdType.MID);
		ID_TYPES.put("sici", IdType.SICI);
		ID_TYPES.put("pubmed", IdType.PUBMED);
		ID_TYPES.put("medline", IdType.MEDLINE);
		ID_TYPES.put("pmcid", IdType.PMC_ID);
		ID_TYPES.put("pmcbook", IdType.PMC_BOOK);
		ID_TYPES.put("bookaccession", IdType.BOOKACCESS);
		
		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-Identifier.html
		ID_TYPES.put("orcid", IdType.ORCID);
		
		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-OtherID.html
		// NASA | KIE | PIP | POP | ARPL | CPC  | IND | CPFH | CLML | NRCBL | NLM | QCIM
		ID_TYPES.put("NASA", IdType.NASA);
		ID_TYPES.put("KIE", IdType.KIE);
		ID_TYPES.put("PIP", IdType.PIP);
		ID_TYPES.put("POP", IdType.POP);
		ID_TYPES.put("ARPL", IdType.ARPL);
		ID_TYPES.put("CPC", IdType.CPC );
		ID_TYPES.put("IND", IdType.IND);
		ID_TYPES.put("CPFH", IdType.CPFH);
		ID_TYPES.put("CLML", IdType.CLML);
		ID_TYPES.put("NRCBL", IdType.NRCBL);
		ID_TYPES.put("NLM", IdType.NLM);
		ID_TYPES.put("QCIM", IdType.QCIM);
		
		ID_TYPES.put("pmid", IdType.PUBMED);
		ID_TYPES.put("isbn", IdType.ISBN);
		ID_TYPES.put("issn", IdType.ISSN);
	}
	

	public enum OwnerType {
		NONE, 
		OTHER,
		
		DATABANK,
		HISTORY_NOTE,
		HHS,
		HMD,
		HSR,
		INDEX_NOTE,
		KIE,
		ONLINE_NOTE,
		MESH_NOTE,
		NASA,
		NASA_FLIGHT,
		NLM,
		NLMAUTO,
		NOTE,
		NOTNLM,
		PIP,
		PUBMED,
		PUBMED_GENE,
		PUBMED_NOTE,
		PUBMED_SYNONYMS,
		VERSION_NOTE
	}	
	public static final Tabla<String, OwnerType> OWNERS_TYPES = new Tabla<>(OwnerType.NONE, OwnerType.OTHER);
	static {		
		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-DataBankName.html
		OWNERS_TYPES.put("DataBankName", OwnerType.DATABANK);

		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-GeneralNote.html
		// NLM | NASA | PIP | KIE | HSR | HMD
		OWNERS_TYPES.put("NLM", OwnerType.NLM);
		OWNERS_TYPES.put("NASA", OwnerType.NASA);
		OWNERS_TYPES.put("PIP", OwnerType.PIP);
		OWNERS_TYPES.put("KIE", OwnerType.KIE);
		OWNERS_TYPES.put("HMD", OwnerType.HMD);
		OWNERS_TYPES.put("HSR", OwnerType.HSR);

		// https://dtd.nlm.nih.gov/ncbi/pubmed/att-ListType.html
		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-GeneSymbol.html
		OWNERS_TYPES.put("synonyms", OwnerType.PUBMED_SYNONYMS);

		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-KeywordList.html
		// NLM | NLM-AUTO | NASA | PIP | KIE | NOTNLM | HHS)
		OWNERS_TYPES.put("NLM-AUTO", OwnerType.NLMAUTO);
		OWNERS_TYPES.put("NOTNLM", OwnerType.NOTNLM);
		OWNERS_TYPES.put("HHS", OwnerType.HHS);
		
		// https://dtd.nlm.nih.gov/ncbi/pubmed/el-MedlineCitation.html
		// NLM | NASA | PIP | KIE | HSR | HMD | NOTNLM

		// https://www.nlm.nih.gov/mesh/xml_data_elements.html#Annotation
		// https://www.nlm.nih.gov/mesh/xml_data_elements.html#HistoryNote
		// https://www.nlm.nih.gov/mesh/xml_data_elements.html#Note
		// https://www.nlm.nih.gov/mesh/xml_data_elements.html#OnlineNote
		// https://www.nlm.nih.gov/mesh/xml_data_elements.html#PreviousIndexing
		// https://www.nlm.nih.gov/mesh/xml_data_elements.html#PublicMeSHNote
		OWNERS_TYPES.put("ANNOTATION", OwnerType.PUBMED_NOTE);
		OWNERS_TYPES.put("HISTORY", OwnerType.HISTORY_NOTE);
		OWNERS_TYPES.put("NOTE", OwnerType.NOTE);
		OWNERS_TYPES.put("ONLINE", OwnerType.ONLINE_NOTE);
		OWNERS_TYPES.put("INDEXNOTE", OwnerType.INDEX_NOTE);
		OWNERS_TYPES.put("MESHNOTE", OwnerType.MESH_NOTE);

		
		OWNERS_TYPES.put("PUBMED", OwnerType.PUBMED);
		OWNERS_TYPES.put("NASA_FLIGHT", OwnerType.NASA_FLIGHT);
	}

	public static final String DESCRIPTORES_MEDLINE = "MEDLINE_DESC";

	private String pmid = new String();
	private Titulo title;
	private OwnerType owner;
	
	private String language;
	private String versionId;
	private LocalDate versionDate;
	private MediumType medium = MediumType.NONE;
	private StatusType status = StatusType.NONE;
	private Revista journal;
	private Fasciculo issue;
	private Libro book;

	private Map<IdType, String> ids = new HashMap<>();
	private Map<String, Map<String, String>> properties = new HashMap<>();
	
	private List<Seccion> sections;
	private List<Localizacion> localizations = new ArrayList<>();
	private List<Fecha> dates = new ArrayList<>();
	private List<Autor> authors = new ArrayList<>();
	private List<Permiso> grants = new ArrayList<>();
	private List<Referencia> references = new ArrayList<>();
	private List<Termino> terms = new ArrayList<>();
	private List<Texto> texts = new ArrayList<>();
	private List<ArticuloBloque> blocks = new ArrayList<>();
	private List<Descriptor> data = new ArrayList<>();
	private List<Descriptor> items = new ArrayList<>();
	private List<Descriptor> keywords = new ArrayList<>();
	private List<Descriptor> genes = new ArrayList<>();
	private List<Descriptor> notes = new ArrayList<>();
	private List<Descriptor> flights = new ArrayList<>();

	private boolean changesInDb = false;
	private boolean changesInIdx = false;
	
	private Fichero pmcFile;
	private Fichero pubmedFile;
	
	private PubArticleEntity entity;
	private PubArticleIdx index;
	private Object xml;

	public Articulo() {
		super();
	}

	public boolean containsBlockOfType(
			final BlockType type) {

		if (type == null)
			return false;

		boolean result = false;
		for (final ArticuloBloque block : this.blocks) {
			if (type.equals(block.getType())) {
				result = true;
				break;
			}
		}
		return result;

	}

	public ArticuloBloque getBlocksOfType(
			final BlockType type) {

		if (type == null)
			return null;

		ArticuloBloque result = null;
		for (final ArticuloBloque block : this.blocks) {
			if (type.equals(block.getType())) {
				result = block;
				break;
			}
		}
		return result;

	}

	public List<ArticuloBloque> generateBlocks() {

		final List<ArticuloBloque> result = new ArrayList<>();
		if (this.getBlocks() != null)
			result.addAll(this.getBlocks());

		int offset = 0;
		final String str = null;
		if ((this.getTexts() != null) && !this.getTexts().isEmpty()) {
			for (final Texto texto : this.getTexts()) {
				TextType articleTextType = texto.getType();
				BlockType type = BlockType.NONE;
				if (TextType.TITLE.equals(articleTextType)) {
					type = BlockType.TITLE;
				} else if (TextType.ABSTRACT.equals(articleTextType)) {
					type = BlockType.ABSTRACT;
				} else {
					type = BlockType.OTHER;
				}
				final String text = texto.getText();
				if (StringUtils.isNotBlank(str)) {
					final ArticuloBloque block = new ArticuloBloque();
					block.setType(type);
					block.setOffset(offset);
					block.setText(text);
					offset += text.length();
					result.add(block);
				}
			}
		}
		return result;

	}

	public final String generateTitle() {

		if (this.getTitle() != null) {
			if (StringUtils.isNotBlank(this.getTitle().getBookId())) {
				System.out.print(" L-" + this.getTitle().getBookId());
			}
			if (StringUtils.isNotBlank(this.getTitle().getPartId())) {
				System.out.print(" P-" + this.getTitle().getPartId());
			}
			if (StringUtils.isNotBlank(this.getTitle().getSectionId())) {
				System.out.print(" S-" + this.getTitle().getSectionId());
			}
			if (StringUtils.isNotBlank(this.getTitle().getTitle())) {
				System.out.println(" T(" + this.getPmid() + ") ]" + this.getTitle().getTitle() + "[");
			}
		}

		String result = "";
		if (this.getTitle() != null)
			result = this.getTitle().getTitle();
		if (StringUtils.isBlank(result))
			result = "";

		return result;

	}

	public void addIds(
			final Map<IdType, String> items) {
		if ((items != null) && (!items.isEmpty()))
			this.ids.putAll(items);
		final String str = this.ids.get(IdType.PUBMED);
		if (StringUtils.isNotBlank(str))
			this.pmid = str;
	}

	public void addIds(
			final Entry<IdType, String> item) {
		if (item != null)
			this.ids.put(item.getKey(), item.getValue());
	}

	public void addIds(
			final List<Entry<IdType, String>> items) {
		if ((items != null) && (!items.isEmpty())) {
			items.forEach(entry -> {
				this.ids.put(entry.getKey(), entry.getValue());
			});
		}
		final String str = this.ids.get(IdType.PUBMED);
		if (StringUtils.isNotBlank(str))
			this.pmid = str;
	}

	public void addAuthors(
			final List<Autor> items) {
		if ((items != null) && (!items.isEmpty()))
			this.authors.addAll(items);
	}

	public void addBlocks(
			final List<ArticuloBloque> items) {
		if ((items != null) && (!items.isEmpty()))
			this.blocks.addAll(items);
	}

	public void addBlock(
			final ArticuloBloque item) {
		if (item != null)
			this.blocks.add(item);
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

	public void addLocalizations(
			final List<Localizacion> items) {
		if ((items != null) && (!items.isEmpty()))
			this.localizations.addAll(items);
	}

	public void addLocalization(
			final Localizacion item) {
		if ((item != null))
			this.localizations.add(item);
	}

	public void addReferences(
			final List<Referencia> items) {
		if ((items != null) && (!items.isEmpty()))
			this.references.addAll(items);
	}

	public void addSections(
			final List<Seccion> items) {
		if ((items != null) && (!items.isEmpty()))
			this.sections.addAll(items);
	}

	public void addGrants(
			final List<Permiso> items) {
		if ((items != null) && (!items.isEmpty()))
			this.grants.addAll(items);
	}

	public void addTerms(
			final List<Termino> items) {
		if ((items != null) && (!items.isEmpty()))
			this.terms.addAll(items);
	}

	public void addData(
			final List<Descriptor> items) {
		if ((items != null) && (!items.isEmpty()))
			this.data.addAll(items);
	}

	public void addKeywords(
			final List<Descriptor> items) {
		if ((items != null) && (!items.isEmpty()))
			this.keywords.addAll(items);
	}

	public void addItems(
			final List<Descriptor> items) {
		if ((items != null) && (!items.isEmpty()))
			this.items.addAll(items);
	}

	public void addNotes(
			final List<Descriptor> items) {
		if ((items != null) && (!items.isEmpty()))
			this.notes.addAll(items);
	}

	public void addFlights(
			final List<Descriptor> items) {
		if ((items != null) && (!items.isEmpty()))
			this.flights.addAll(items);
	}

	public void addGenes(
			final List<Descriptor> items) {
		if ((items != null) && (!items.isEmpty()))
			this.genes.addAll(items);
	}

	public void addProperties(
			final Map<String, Map<String, String>> items) {
		if ((items != null) && !items.isEmpty()) {
			properties.putAll(items);
		}
	}

	public void addText(
			final Texto item) {
		if ((item != null))
			this.texts.add(item);
	}

	public void addTexts(
			final List<Texto> items) {
		if ((items != null) && (!items.isEmpty()))
			this.texts.addAll(items);
	}

	// TODO
	public void mergeJournal(
			final Revista revista) {

		if (revista == null)
			return;

		if (this.getJournal() == null) {
			this.setJournal(revista);
		} else {
			final Revista old = this.getJournal();
			if (StringUtils.isNotBlank(revista.getAbbreviation()))
				old.setAbbreviation(revista.getAbbreviation());
			if ((revista.getMedium() != null) && !(MediumType.NONE.equals(revista.getMedium())))
				old.setMedium(revista.getMedium());
			if (StringUtils.isNotBlank(revista.getName()))
				old.setName(revista.getName());
			if (StringUtils.isNotBlank(revista.getCountry()))
				old.setCountry(revista.getCountry());
			if ((revista.getType() != null) && (!PublicationType.NONE.equals(revista.getType())))
				old.setType(revista.getType());
			if ((revista.getIds() != null) && !revista.getIds().isEmpty())
				old.addIds(revista.getIds());
		}

	}

	// TODO
	public void mergeIssue(
			final Fasciculo fasciculo) {

		if (fasciculo == null)
			return;

		if (this.getIssue() == null) {
			this.setIssue(fasciculo);
		} else {
			final Fasciculo old = this.getIssue();
			if (fasciculo.getDate() != null)						old.setDate(fasciculo.getDate());
			if (StringUtils.isNotBlank(fasciculo.getNumber()))		old.setNumber(fasciculo.getNumber());
			if ((fasciculo.getMedium() != null) &&
				(!MediumType.NONE.equals(fasciculo.getMedium())))	old.setMedium(fasciculo.getMedium());
		}

	}

}
