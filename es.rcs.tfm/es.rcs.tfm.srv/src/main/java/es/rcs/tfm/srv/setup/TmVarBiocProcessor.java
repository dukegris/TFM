package es.rcs.tfm.srv.setup;

import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.lang3.StringUtils;
import org.bioc.Annotation;
import org.bioc.Collection;
import org.bioc.Document;
import org.bioc.Infon;
import org.bioc.Location;
import org.bioc.Passage;
import org.bioc.Relation;
import org.bioc.Sentence;
import org.bioc.Text;

import es.rcs.tfm.srv.SrvException;
import es.rcs.tfm.srv.SrvException.SrvViolation;
import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.model.ArticuloBloque;
import es.rcs.tfm.srv.model.ArticuloBloque.BlockType;
import es.rcs.tfm.srv.model.ArticuloBloqueAnotacion;
import es.rcs.tfm.srv.model.ArticuloBloqueAnotacion.NoteType;
import es.rcs.tfm.srv.model.ArticuloBloquePosicion;
import es.rcs.tfm.srv.model.Tabla;

/**
 * tmBioC es una especificación basada en BioC para el intercambio de documentos
 * con marcaciones. BioC es una DtD que requiere un fichero key donde se explica
 * el correcto uso de determinado elementos, especialmente los denominados infon
 * No utiliza el componente sentence, por lo que en esta implementación se obvia
 * @author dukegris
 *
 */
public class TmVarBiocProcessor extends ArticleProcessor {

	public static final String PASSAGE_TYPE = "type";
	public static final Tabla<String, BlockType> BLOCKS_NORMALIZE = new Tabla<>(BlockType.NONE, BlockType.OTHER);
	static {
		BLOCKS_NORMALIZE.put("title", BlockType.TITLE);
		BLOCKS_NORMALIZE.put("abstract", BlockType.ABSTRACT);
	}

	private List<Document> items = null;
	private boolean allOk = false;
	private int index = 0;
	
	public TmVarBiocProcessor(Path path) {
		
        SAXSource source = ArticleProcessor.getSourceFromPath(path);
        if (source != null) {
			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				// Group of documents, usually from a larger corpus. If
			    // a group of documents is from several corpora, use several
			    // collections
				Collection collection = (Collection) jaxbUnmarshaller.unmarshal(source);
				if (collection != null) {
					if (	(collection.getDocument() != null) &&
							(!collection.getDocument().isEmpty())) {
						// TODO Name of the source corpus from which the documents were selected
						collection.getSource();
						// TODO Date documents extracted from original source
						collection.getDate(); 
						// TODO Separate file describing the infons used and any other useful information
						collection.getKey(); 
						// TODO key-value pairs
						collection.getInfon(); 
						// A document in the collection. A single, complete stand-alone document
						items = collection.getDocument();
						allOk = true;
					}
				}
			} catch (JAXBException ex) {
				throw new SrvException(SrvViolation.JAXB_FAIL, ex);
			} catch (Exception ex) {
				throw new SrvException(SrvViolation.UNKNOWN, ex);
			}
		}

	}

	@Override
	public boolean hasNext() {
		return  index < items.size();
	}

	@Override
	public Articulo next() {
		
		if (	(!this.allOk) ||
				(!this.hasNext())) {
			this.allOk = false;
			throw new SrvException(SrvViolation.NO_MORE_DATA, "No hay mas datos");
		}

		Document item = items.get(index);
		Articulo result = getMarkedText(item);
		
		return result;
	
	}

	/**
	 * Transforma un item documento en un POJO de tipo MarkedText
	 * @param doc
	 * @return
	 */
	private Articulo getMarkedText(Document doc) {

		if (doc == null) return null;

		Articulo result = new Articulo();				

		// id:  Typically, the id of the document in the parent document
		result.setPmid(		doc.getId());
		
		// passage: One portion of the document. Could be sections such as
	    // Introduction, Materials and Methods, or Conclusion. Another option
	    // would be paragraphs
		result.addBlocks(	makeBlocks(doc.getPassage()));
		
		// TODO Lo admite la DTD pero no lo he visto
		doc.getInfon(); 
		
		 //TODO Relation between multiple annotations. Relations are allowed 
		// to appear at several levels (document, passage, and sentence)
		doc.getRelation();
		
		this.index++;

		return result;
		
	}
	
	/**
	 * @param passage
	 * @return
	 */
	private List<ArticuloBloque> makeBlocks(List<Passage> passage) {

		if (	(passage == null) ||
				(passage.isEmpty()) ) return null;

		// Fase 1: Cargar artículo
		AtomicInteger blockOffset = new AtomicInteger(0);
		List<ArticuloBloque> resultado = passage.
			stream().
			filter(p ->	(p != null) ).
			flatMap(instance -> 	{

				Stream<ArticuloBloque> result = null;
				// passage ( infon*, offset, ( ( text?, annotation* ) | sentence* ), relation* )
				List<Object> passageItem = instance.getTextOrAnnotationOrSentence();
				if ((passageItem != null) && (!passageItem.isEmpty())) {
					// BioC passage se puede comportar como una sentence
					// Se establece sentence para agrupar varios textos en un passage
					result = passageItem.
						stream().
						filter(p ->		(p != null) && (
										(p instanceof Sentence) ||
										(p instanceof Text))).
						map(item -> {
							ArticuloBloque block = null;
							if (item instanceof Sentence) {
								// sentence ( infon*, offset, text?, annotation*, relation* )
								Sentence s = (Sentence)item;
								block = makeBlock(
										s.getInfon(),
										blockOffset.get(), //item.getOffset(),
										s.getText(),
										s.getAnnotation(),
										s.getRelation());
							} else if (item instanceof Text) {
								Text t = (Text)item;
								block = makeBlock(
										instance.getInfon(),
										blockOffset.get(), //instance.getOffset(),
										t,
										instance.getTextOrAnnotationOrSentence(),
										instance.getRelation());
							}
							try {
								/*
								if (!block.isTitle()) {
									int delta = makeOffset(instance.getOffset());
									blockOffset.addAndGet(delta);
								}
								*/
								int delta = block.getText().length() + 1;
								blockOffset.addAndGet(delta);
							} catch (NumberFormatException ex) {}
							
							return block;
						});
				}
				return result;

			}).
			collect(Collectors.toList());
		
		// Fase 2: Revisar resultado. Se debe de obtener dos resultados para PubMed, titulo y abstract
		// Offset basado en CDATA Son CDATA e interpreta &gt; como > 4 a 1.
		// TODO Posibles incidencias en documentos con UTF8
		// Length de texto plano. Avisamos si no se ha procesado correctamente
		if (resultado.size() == 2) {
			int offset = resultado.get(1).getOffset(); 
			int textlength = resultado.get(0).getText().length();
			if (offset - 1 != textlength) {
				System.out.println (
						"Error en el offset del fichero: Recalcular notas.Hay " + 
						textlength +
						" en el titulo y " +
						offset +
						" en el fichero");
				resultado.get(1).recalculateOffset(offset - textlength - 1);
				resultado.get(1).setOffset(textlength + 1);
				
				offset = resultado.get(1).getOffset();
				textlength = resultado.get(0).getText().length();
				if (offset - 1 != textlength) {
					System.out.println (
							"Tras el recalculo sigue el error en el offset del fichero: " + 
							"recalcular notas.Hay " + 
							textlength +
							" en el titulo y " +
							offset +
							" en el fichero");
				}
			}
		}
			
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}
	
	protected Integer makeOffset(String offset) {
		
		if (offset == null) return 0;
		
		int resultado = 0;
		try {
			resultado = Integer.parseInt(offset);
		} catch (NumberFormatException ex) {
			resultado = 0;
		}
		return resultado;

	}
	
	private ArticuloBloque makeBlock(
			List<Infon> infons,
			int offset,
			Text text,
			List<?> annotations,
		    List<Relation> relations) {
		
		ArticuloBloque block = new ArticuloBloque();

		// infon: For PubMed references, passage "type" might signal "title" or "abstract"
		block.setType(		makeBlockType(infons));

		// offset: Where the passage occurs in the parent document
		// El Offset esta en estos ficheros de tmVAR referenciado al tamnio en CDATA del pasaje anterior con lo que &gt; ocupa 4 caracteres. en vez de 1
		// Los datos de location estan copiados y son referidos a caracteres no CDATA
		//block.setOffset(	makeOffset(instance.getOffset()));
		block.setOffset(	offset);

		// text: The original text, sentence of the passage
		if (text != null)
		block.setText(		text.getvalue());

		// annotation:  Stand-off annotation
		block.addNotes(		makeNotes(annotations, offset));
		
		return block;
		
	}

	private BlockType makeBlockType(List<Infon> infons) {
		
		if (	(infons == null) ||
				(infons.isEmpty())) return BlockType.NONE;
		
		BlockType resultado = BlockType.NONE;
		for (Infon infon: infons) {
			if(PASSAGE_TYPE.equals(infon.getKey())) {
				BlockType type = BLOCKS_NORMALIZE.get(infon.getvalue(), BlockType.NONE);
				if (!BlockType.NONE.equals(type)) {
					resultado = type;
					break;
				}
			}
		}
		return resultado;

	}

	private Map<String, ArticuloBloqueAnotacion> makeNotes(
			List<?> annotations,
			int blockOffset) {

		if (	(annotations == null) ||
				(annotations.isEmpty()) ) return null;
		
		Map<String, ArticuloBloqueAnotacion> resultado = annotations.stream().
			filter(p ->		(p != null) &&
							(p instanceof Annotation) ).
			map(instance -> 	{
				ArticuloBloqueAnotacion note = makeNote((Annotation)instance, blockOffset);
				SimpleEntry<String, ArticuloBloqueAnotacion> entry = null;
				if (note != null) entry = new SimpleEntry<>(
						note.getId(),
						note);
				return entry;
			}).
			filter(p ->		(p != null)).
			collect(Collectors.toMap(	
				p -> p.getKey(), 
				p -> p.getValue(),
				(o1, o2) -> o1 ));
//				(o1, o2) -> o1 + ", " + o2 ));
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	private ArticuloBloqueAnotacion makeNote(
			Annotation annotation,
			int blockOffset) {

		if (	(annotation == null) ) return null;
		
		ArticuloBloqueAnotacion note = null;
		String type = makeType(annotation.getInfon());
		note = new ArticuloBloqueAnotacion();
		note.setId(			annotation.getId());
		note.setType(		ArticuloBloqueAnotacion.NOTE_TYPES.get(type, NoteType.OTHER));
		note.setText(		(annotation.getText() != null) ? annotation.getText().getvalue() : null); 
		note.setValue(		makeValue(annotation.getInfon(), type));
		note.addPositions(	makePositions(annotation.getLocation(), blockOffset));
				
		return note;
		
	}

	private String makeType(List<Infon> infons) {
		return makeValue(infons, PASSAGE_TYPE);

	}

	private String makeValue(List<Infon> infons, String type) {
		
		if (	(infons == null) ||
				(infons.isEmpty()) ||
				(StringUtils.isBlank(type))) return null;
		
		String resultado = null;
		for (Infon infon: infons) {
			if(type.equals(infon.getKey())) {
				resultado = infon.getvalue();
				break;
			}
		}
		return resultado;

	}

	private List<ArticuloBloquePosicion> makePositions(List<Location> location, int blockOffset) {

		if (	(location == null) ||
				(location.isEmpty())) return null;
		
		List<ArticuloBloquePosicion> resultado = location.stream().
			filter(p ->		(p!=null) ).
			map(instance ->	 	{
				return new ArticuloBloquePosicion(
					// Integer.parseInt(instance.getOffset()) - Integer.parseInt(block.offset),
					Integer.parseInt(instance.getOffset()) - blockOffset,
					Integer.parseInt(instance.getLength()) );
			}).
			collect(Collectors.toList());
			
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

}
