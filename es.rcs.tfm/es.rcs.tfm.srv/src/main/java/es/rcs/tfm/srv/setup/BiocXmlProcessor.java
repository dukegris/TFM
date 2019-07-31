package es.rcs.tfm.srv.setup;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.lang3.StringUtils;
import org.bioc.Annotation;
import org.bioc.Collection;
import org.bioc.Document;
import org.bioc.Infon;
import org.bioc.Location;
import org.bioc.Passage;
import org.bioc.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import es.rcs.tfm.srv.model.MarkedText;
import es.rcs.tfm.srv.model.MarkedText.Block;
import es.rcs.tfm.srv.model.MarkedText.Note;
import es.rcs.tfm.srv.model.MarkedText.Position;

public class BiocXmlProcessor extends MarkedTextProcessor {
	
	private static final String INFON_TYPE = "type";
	
	private List<Document> items = null;
	private boolean allOk = false;
	private int index = 0;
	
	public BiocXmlProcessor(
			Path path) {

		if (	!path.toFile().exists() ||
				!path.toFile().isFile()) throw new IllegalArgumentException(); 
			
		JAXBContext jaxbContext;
		
		try {
		    
		    SAXParserFactory spf = SAXParserFactory.newInstance();
	        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
	        //spf.setFeature(XMLConstants.ACCESS_EXTERNAL_DTD, false);
	        
	        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
	        InputSource inputSource = new InputSource(new FileReader(path.toFile()));
	        
	        SAXSource source = new SAXSource(xmlReader, inputSource);
	        
			jaxbContext = JAXBContext.newInstance(Collection.class);
		    
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		    //SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI );
		    //Schema schema = sf.newSchema( new File( "countries_validation.xsd" ) );  
		    //StreamSource source = new StreamSource(getClass().getResourceAsStream(schemaLocation));
            //Schema schema = schemaFactory.newSchema(source);
            //jaxbUnmarshaller.setSchema(schema);
			Collection collection = (Collection) jaxbUnmarshaller.unmarshal(source);
			if (collection != null) {
				if (	(collection.getDocument() != null) &&
						(!collection.getDocument().isEmpty())) {
					items = collection.getDocument();
					allOk = true;
				}
			}
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXNotRecognizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public boolean hasNext() {

		boolean more = index < items.size()-1;
		
		if (!more) {
			System.out.println("TEXTOS");
			MarkedText.BLOCKS.forEach((e,v) -> System.out.println ("BLOCK: " + e + ": " + v));
			System.out.println("MUTACIONES");
			MarkedText.MUTATIONS.forEach((e,v) -> System.out.println ("TYPE: " + e + ": " + v));
		}
		
		return more;

	}
	
	@Override
	public MarkedText next() {
		
		if (	(!this.allOk) ||
				(!this.hasNext())) {
			this.allOk = false;
			throw new NoSuchElementException();
		}

		Document doc = items.get(index);

		MarkedText documento = new MarkedText();
		
		doc.getInfon(); //TODO
		doc.getRelation(); //TODO

		documento.pmid = doc.getId();

		if (	(doc.getPassage() != null) &&
				(!doc.getPassage().isEmpty())) {
			List<Passage> passages = doc.getPassage();
			passages.forEach(passage -> {
				
				passage.getRelation(); //TODO

				Block block = documento.new Block();
				block.offset = passage.getOffset();
				
				if (	(passage.getInfon() != null) &&
						(!passage.getInfon().isEmpty())) {
					List<Infon> infons = passage.getInfon();
					infons.forEach(infon -> {
						if (INFON_TYPE.equals(infon.getKey())) {
							block.type = infon.getvalue();
							if (MarkedText.BLOCKS.containsKey(block.type)) {
								block.type = MarkedText.BLOCKS.get(block.type);
							} else {
								MarkedText.BLOCKS.put(block.type, block.type);
							}
							
						}
					});
				}

				if (	(passage.getTextOrAnnotationOrSentence() != null) &&
						(!passage.getTextOrAnnotationOrSentence().isEmpty())) {
					List<Object> items = passage.getTextOrAnnotationOrSentence();
					for (Object item : items) {
						if ((item != null) && item instanceof Text) {
							Text texto = (Text)item;
							String str = texto.getvalue();
							if (StringUtils.isNotBlank(str)) {
								block.text = str;
							}
						}
						if ((item != null) && item instanceof Annotation) {
							Annotation annotation = (Annotation)item;
							Note nota = documento.new Note();
							nota.id = annotation.getId();
							if (	(annotation.getInfon() != null) &&
									(!annotation.getInfon().isEmpty())) {
								List<Infon> infons = annotation.getInfon();
								infons.forEach(infon -> {
									if (INFON_TYPE.equals(infon.getKey())) {
										nota.type = infon.getvalue();
										if (MarkedText.MUTATIONS.containsKey(nota.type)) {
											nota.type = MarkedText.MUTATIONS.get(nota.type);
										} else {
											MarkedText.MUTATIONS.put(nota.type, nota.type);
										}
									} else {
										nota.value = infon.getvalue();
									}
								});
							}
							if (annotation.getText() != null) nota.text = annotation.getText().getvalue();
							
							if (	(annotation.getLocation() != null) &&
									(!annotation.getLocation().isEmpty())) {
								List<Location> locations = annotation.getLocation();
								for (Location loc : locations) {
									try {
										Position pos = documento.new Position(
												Integer.parseInt(loc.getOffset()),
												Integer.parseInt(loc.getLength()));
										nota.pos.add(pos);
									} catch (NumberFormatException ex) {
										
									}
								}
							}
							block.notes.put(nota.type, nota);
						}
					}
				}
				
				documento.blocks.put(block.type, block);

			});
			
		}
		
		this.index++;

		return documento;
		
	}
	
}
