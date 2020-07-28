package es.rcs.tfm.srv.setup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import es.rcs.tfm.srv.SrvException;
import es.rcs.tfm.srv.SrvException.SrvViolation;
import es.rcs.tfm.srv.model.Articulo;

public abstract class ArticleProcessor implements Iterator<Articulo>  {

	public static SAXSource getSourceFromPath(Path path) {
		
		if (	path == null ||
				!path.toFile().exists() ||
				!path.toFile().isFile()) throw new SrvException(SrvViolation.FILE_FAIL, "El fichero debe existir"); 

		SAXSource source = null;
		try {
		    
		    SAXParserFactory spf = SAXParserFactory.newInstance();
	        spf.setNamespaceAware(true);
	        spf.setValidating(false);
	        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
	        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	        spf.setFeature("http://xml.org/sax/features/validation", false);
	        
	        SAXParser parser = spf.newSAXParser();
	        // protocols separated by comma: all,file,http,jar[:scheme]
	        // deny se hace con ""
	        //parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
	        //parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
	        //parser.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

	        XMLReader xmlReader = parser.getXMLReader();
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
