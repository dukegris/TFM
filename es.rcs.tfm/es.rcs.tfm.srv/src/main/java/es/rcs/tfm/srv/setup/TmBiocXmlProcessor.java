package es.rcs.tfm.srv.setup;

import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
import org.bioc.Sentence;
import org.bioc.Text;

import es.rcs.tfm.srv.SrvException;
import es.rcs.tfm.srv.SrvException.SrvViolation;
import es.rcs.tfm.srv.model.Anotacion;
import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.model.BloqueAnotado;
import es.rcs.tfm.srv.model.Posicion;

/**
 * tmBioC es una especificación basada en BioC para el intercambio de documentos
 * con marcaciones. BioC es una DtD que requiere un fichero key donde se explica
 * el correcto uso de determinado elementos, especialmente los denominados infon
 * No utiliza el componente sentence, por lo que en esta implementación se obvia
 * @author dukegris
 *
 */
public class TmBiocXmlProcessor extends ArticleProcessor {
	
	private List<Document> items = null;
	private boolean allOk = false;
	private int index = 0;
	
	public TmBiocXmlProcessor(Path path) {
		
        SAXSource source = ArticleProcessor.getSourceFromPath(path);
        if (source != null) {
			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				Collection collection = (Collection) jaxbUnmarshaller.unmarshal(source);
				if (collection != null) {
					if (	(collection.getDocument() != null) &&
							(!collection.getDocument().isEmpty())) {
						items = collection.getDocument();
						collection.getDate(); // TODO
						collection.getInfon(); // TODO
						collection.getKey(); // TODO
						collection.getSource(); // TODO
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

		boolean more = index < items.size()-1;
		
		if (!more) {
			System.out.println("TEXTOS");
			ArticleProcessor.BLOCKS_NORMALIZE.forEach((e,v) -> System.out.println ("BLOCK: " + e + ": " + v));
			System.out.println("MUTACIONES");
			ArticleProcessor.MUTATIONS_NORMALIZE.forEach((e,v) -> System.out.println ("TYPE: " + e + ": " + v));
		}
		
		return more;

	}
	
	@Override
	public Articulo next() {
		
		if (	(!this.allOk) ||
				(!this.hasNext())) {
			this.allOk = false;
			throw new SrvException(SrvViolation.NO_MORE_DATA, "No hay mas datos");
		}

		Articulo result = null;

		Document item = items.get(index);
		result = getMarkedText(item);
		
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

		result.setPmid(		doc.getId());
		result.addBlocks(	makeBlocks(doc.getPassage()));
		doc.getInfon(); //TODO
		doc.getRelation(); //TODO
		
		this.index++;

		return result;
		
	}

	/**
	 * @param passage
	 * @return
	 */
	private List<BloqueAnotado> makeBlocks(List<Passage> passage) {

		if (	(passage == null) ||
				(passage.isEmpty()) ) return null;

		//passage.get(0).getRelation(); //TODO
		//passage.get(0).getTextOrAnnotationOrSentence().get(0);
		AtomicInteger blockOffset = new AtomicInteger(0);
		List<BloqueAnotado> resultado = passage.stream().
			filter(p ->		(p != null) ).
			map(instance -> 	{
				
				BloqueAnotado block = new BloqueAnotado();
				block.addInfonType(	makeInfon(instance.getInfon()));
				block.setOffset(	makeOffset(instance.getOffset()));
				block.setText(		makeText(instance.getTextOrAnnotationOrSentence()));

				try {
					int delta = makeOffset(instance.getOffset());
					if (!block.isTitle()) blockOffset.addAndGet(delta);
				} catch (NumberFormatException ex) {}
				block.addNotes(		makeNotes(instance.getTextOrAnnotationOrSentence(), blockOffset.get()));
				
				return block;
				
			}).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}
	
	private Integer makeOffset(String offset) {
		
		if (offset == null) return 0;
		
		int resultado = 0;
		try {
			resultado = Integer.parseInt(offset);
		} catch (NumberFormatException ex) {
			resultado = 0;
		}
		return resultado;

	}
	
	private String makeText(List<Object> texts) {

		if (	(texts == null) ||
				(texts.isEmpty()) ) return null;
		
		List<String> resultado = texts.stream().
			filter(p ->		(p != null) ).
			map(instance -> 	{
				String str = "";
				if (instance instanceof Sentence) {
					Sentence sentence = (Sentence)instance;
					// sentence.getInfon();
					// sentence.getOffset();
					// sentence.getText();
					// sentence.getAnntotation();
					// sentence.getRelation();
					if (	(sentence.getText() != null) &&
							(StringUtils.isNotBlank(sentence.getText().getvalue())) ) {
						str = sentence.getText().getvalue();
					}
				} else if (instance instanceof Text) {
					Text text = (Text)instance;
					str = text.getvalue();
				}
				return str;
			}).
			collect(Collectors.toList());
		
		if (resultado.isEmpty()) resultado = null;
		StringBuffer sb = new StringBuffer();
		resultado.forEach(s -> sb.append(s));
		return sb.toString();

	}

	private Map<String, Anotacion> makeNotes(List<Object> annotations, int blockOffset) {

		if (	(annotations == null) ||
				(annotations.isEmpty()) ) return null;
		
		Map<String, Anotacion> resultado = annotations.stream().
			filter(p ->		(p != null) &&
							(p instanceof Annotation) ).
			map(instance -> 	{
				Anotacion note = makeNote((Annotation)instance, blockOffset);
				return new SimpleEntry<String, Anotacion>(
						note.getId(),
						note);
			}).
			collect(Collectors.toMap(	
				p -> p.getKey(), 
				p -> p.getValue(),
				(o1, o2) -> o1 ));
//				(o1, o2) -> o1 + ", " + o2 ));
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

	private Anotacion makeNote(Annotation annotation, int blockOffset) {

		if (	(annotation == null) ) return null;
		
		Anotacion note = new Anotacion();
		note.setId(			annotation.getId());
		note.setText(		(annotation.getText() != null) ? annotation.getText().getvalue() : null); 
		note.addPositions(	makePositions(annotation.getLocation(), blockOffset));
		note.addInfon(		makeInfon(annotation.getInfon()) );
		
		return note;
	}

	private Map<String, String> makeInfon(List<Infon> infon) {
		
		if (	(infon == null) ||
				(infon.isEmpty())) return null;
		
		Map<String, String> resultado = infon.stream().
			filter(p ->		(p != null) &&
							(StringUtils.isNotBlank(p.getKey())) &&
							(StringUtils.isNotBlank(p.getvalue())) ).
			map(instance -> {
				return new SimpleEntry<String, String>(
						instance.getKey(),
						instance.getvalue());
			}).
			collect(Collectors.toMap(	
				p -> p.getKey(), 
				p -> p.getValue(),
				(o1, o2) -> o1 + ", " + o2 ));
		
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}
	
	private List<Posicion> makePositions(List<Location> location, int blockOffset) {

		if (	(location == null) ||
				(location.isEmpty())) return null;
		
		List<Posicion> resultado = location.stream().
			filter(p ->		(p!=null) ).
			map(instance ->	 	{
				return new Posicion(
						// Integer.parseInt(instance.getOffset()) - Integer.parseInt(block.offset),
						Integer.parseInt(instance.getOffset()) - blockOffset,
						Integer.parseInt(instance.getLength()) );
			}).
			collect(Collectors.toList());
			
		if (resultado.isEmpty()) resultado = null;
		return resultado;

	}

}
