package es.rcs.tfm.srv.model;

import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * https://www.ncbi.nlm.nih.gov/CBBresearch/Lu/Demo/PubTator/tutorial/tmVar.html
 *  
 * @author dukegris
 *
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(
		callSuper = false)
public class ArticuloBloqueAnotacion {

	public enum NoteType {
		NONE,
		OTHER,
		
		MUT_DNA,
		MUT_PRO,
		MUT_SNP
	}
	public static final Tabla<String, NoteType> NOTE_TYPES = new Tabla<>(NoteType.NONE, NoteType.OTHER);
	static {
		NOTE_TYPES.put("DNAMutation", NoteType.MUT_DNA);
		NOTE_TYPES.put("ProteinMutation", NoteType.MUT_PRO);
		NOTE_TYPES.put("SNP", NoteType.MUT_SNP);
	}

	private NoteType type = NoteType.NONE;
	private String id = "";
	private String value = "";
	private String text = ""; 
	// La DTD de BIOC admite n posiciones, pero solo he visto una por anotacion
	private List<ArticuloBloquePosicion> pos = new ArrayList<ArticuloBloquePosicion>();

	public void addPosition(
			final ArticuloBloquePosicion item) {
		this.pos.add(item);
	}

	public void addPositions(
			final List<ArticuloBloquePosicion> items) {
		if ((items != null) && (!items.isEmpty()))
			this.pos.addAll(items);
	}

	/*
	public enum InfonType {
		NONE,
		OTHER,
		TYPE,
		MESH,
		OMIM,
		NCBI_GENE,
		NCBI_TAXONOMY,
		CHEBI,
		TMVAR
	}
	private static final Tabla<String, InfonType> INFON_TYPES = new Tabla<>(InfonType.NONE, InfonType.OTHER);
	static {
		// infon["type"]:  The type of bioconcept, e.g. "Gene", "Species", "Disease", "Chemical" or "Mutation"
		// infon["MeSH"]:  The bio-concept identifier in MeSH as detected by DNorm or tmChem
		// infon["OMIM"]:  The bio-concept identifier in OMIM as detected by DNorm
		// infon["NCBI_Gene"]:  The bio-concept identifier in NCBI Gene as detected by GenNorm
		// infon["NCBI_Taxonomy"]:  The bio-concept identifier in NCBI Taxonomy as detected by SR4GN
		// infon["ChEBI"]:  The bio-concept identifier in ChEBI as detected by tmChem
		// infon["tmVar"]:  The intelligent key generated artificially for the mention detected by tmVar (<Sequence type>|<Mutation type>|<Wild type>|<Mutation position>|<Mutant>)
		INFON_TYPES.put("type", InfonType.TYPE);
		INFON_TYPES.put("MeSH", InfonType.MESH);
		INFON_TYPES.put("OMIM", InfonType.OMIM);
		INFON_TYPES.put("NCBI_Gene", InfonType.NCBI_GENE);
		INFON_TYPES.put("NCBI_Taxonomy", InfonType.NCBI_TAXONOMY);
		INFON_TYPES.put("ChEBI", InfonType.CHEBI);
		INFON_TYPES.put("tmVar", InfonType.TMVAR);
	}
	*/

	/*

	public void addInfon(
			final Map<String, String> items) {

		if ((items != null) && (!items.isEmpty())) {
			// Realmente el DTD especifica una lista pero hay un type que indica el tipo de
			// mutacion y que enlaza a otra mutacion (1: type="DNAMutation" 2:
			// DNAMutation="xx|xxx|xxx|xx

			// Buscamos el infon type
			final String type = items.get(ArticuloBloque.PASSAGE_TYPE);
			if (StringUtils.isNotBlank(type)) {
				// Buscamos el tipo que utilizaremos en los procesos NER. Si no lo hay lo
				// añadimos
				String ner = ArticleProcessor.MUTATIONS_NORMALIZE.get(type);
				if (StringUtils.isBlank(ner)) {
					ArticleProcessor.MUTATIONS_NORMALIZE.put(type, type);
					ner = type;
				}
				// Buscamos la mutaciï¿½n
				final String mutation = items.get(type);
				
				this.type = ner;
				this.value = mutation;
			} else {
				System.out.println ("INFON incompatible: ");
				items.entrySet().forEach(item -> {System.out.print(String.format("\\r\\n\t%s: %s", item.getKey(), item.getValue()));});
			}
		}

	}
	*/

}
