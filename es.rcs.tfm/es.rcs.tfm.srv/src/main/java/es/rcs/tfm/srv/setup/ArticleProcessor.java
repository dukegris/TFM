package es.rcs.tfm.srv.setup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import es.rcs.tfm.srv.SrvException;
import es.rcs.tfm.srv.SrvException.SrvViolation;
import es.rcs.tfm.srv.model.Articulo;

public abstract class ArticleProcessor implements Iterator<Articulo>  {
	
	// "title", "abstract" and other passages
	public static final Map<String, String> BLOCKS_NORMALIZE;
	static {
		BLOCKS_NORMALIZE = new HashMap<String, String>();
		BLOCKS_NORMALIZE.put("t", "title");
		BLOCKS_NORMALIZE.put("a", "abstract");
		BLOCKS_NORMALIZE.put("title", "title");
		BLOCKS_NORMALIZE.put("abstract", "abstract");
	}

	public static final Map<String, String> MUTATIONS_NORMALIZE;
	static {
		MUTATIONS_NORMALIZE = new HashMap<String, String>();
		MUTATIONS_NORMALIZE.put("DNAMutation", "MUT_DNA");
		MUTATIONS_NORMALIZE.put("ProteinMutation", "MUT_PRO");
		MUTATIONS_NORMALIZE.put("SNP", "MUT_SNP");
	}

	public static SAXSource getSourceFromPath(Path path) {
		
		if (	path == null ||
				!path.toFile().exists() ||
				!path.toFile().isFile()) throw new SrvException(SrvViolation.FILE_FAIL, "El fichero debe existir"); 

		SAXSource source = null;
		try {
		    
		    SAXParserFactory spf = SAXParserFactory.newInstance();
	        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
	        spf.setNamespaceAware(true);
	        //spf.setFeature(XMLConstants.ACCESS_EXTERNAL_DTD, false);
	        
	        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
	        InputStream inputStream = new FileInputStream(path.toFile());
	        Reader reader = new InputStreamReader(inputStream, "UTF-8");
	        InputSource inputSource = new InputSource(reader);
	        
	        source = new SAXSource(xmlReader, inputSource);

		} catch (SAXException | ParserConfigurationException ex) {
			throw new SrvException(SrvViolation.SAX_FAIL, ex);
		} catch (FileNotFoundException ex) {
			throw new SrvException(SrvViolation.FILE_FAIL, "El fichero debe existir");
		} catch (Exception ex) {
			throw new SrvException(SrvViolation.UNKNOWN, ex);
		}

		return source;
		
	};
}
