package es.rcs.tfm.srv.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import es.rcs.tfm.srv.setup.ArticleProcessor;
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
public class Anotacion {
	
	// infon["type"]:  The type of bioconcept, e.g. "Gene", "Species", "Disease", "Chemical" or "Mutation"
	// infon["MeSH"]:  The bio-concept identifier in MeSH as detected by DNorm or tmChem
	// infon["OMIM"]:  The bio-concept identifier in OMIM as detected by DNorm
	// infon["NCBI_Gene"]:  The bio-concept identifier in NCBI Gene as detected by GenNorm
	// infon["NCBI_Taxonomy"]:  The bio-concept identifier in NCBI Taxonomy as detected by SR4GN
	// infon["ChEBI"]:  The bio-concept identifier in ChEBI as detected by tmChem
	// infon["tmVar"]:  The intelligent key generated artificially for the mention detected by tmVar (<Sequence type>|<Mutation type>|<Wild type>|<Mutation position>|<Mutant>)

	private String id = "";
	private String type = "";
	private String value = "";
	private String text = ""; 
	// La DTD de BIOC admite n posiciones, pero solo he visto una por anotacion
	private ArrayList<Posicion> pos = new ArrayList<Posicion>();

	public void addPosition(Posicion item) {
		this.pos.add(item);
	}
	public void addPositions(List<Posicion> items) {
		if ((items!= null) && (!items.isEmpty())) this.pos.addAll(items);
	}
	public void addInfon(Map<String, String> items) {
		if ((items!= null) && (!items.isEmpty())) {
	 		// Realmente el DTD especifica una lista pero hay un type que indica el tipo de 
			// mutacion y que enlaza a otra mutacion (1: type="DNAMutation" 2: DNAMutation="xx|xxx|xxx|xx
			
			// Buscamos el infon type
			String type = items.get(BloqueAnotado.PASSAGE_TYPE);
			if (StringUtils.isNotBlank(type)) {
				// Buscamos el tipo que utilizaremos en los procesos NER. Si no lo hay lo a�adimos
				String ner = ArticleProcessor.MUTATIONS_NORMALIZE.get(type);
				if (StringUtils.isBlank(ner)) {
					ArticleProcessor.MUTATIONS_NORMALIZE.put(type, type);
					ner = type;
				}
				// Buscamos la mutaci�n
				String mutation = items.get(type);
				
				this.type = ner;
				this.value = mutation;
			} else {
				System.out.println ("INFON incompatible: ");
				items.entrySet().forEach(item -> {System.out.print(String.format("\\r\\n\t%s: %s", item.getKey(), item.getValue()));});
			}
		}
	}
}
