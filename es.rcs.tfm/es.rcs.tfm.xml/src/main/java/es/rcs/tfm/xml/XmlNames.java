package es.rcs.tfm.xml;

import org.bioc.Collection;
import org.ncbi.mesh.DescriptorRecordSet;
import org.ncbi.pubmed.PubmedArticleSet;

public class XmlNames {

	// -------------------------------------------------------------------------------------
	// TFM-XMLMODEL: CONFIGURACION
	public static final String XML_CONFIG =						"taoXmlConf";
	public static final String XML_CONFIG_PKG = 				"es.rcs.tfm.xml.config";
	public static final String XML_SETUP_PKG =					"es.rcs.tfm.xml.setup";

	public static final String XML_PUBMED_PKG =					PubmedArticleSet.class.getPackage().getName(); //"org.ncbi.pubmed";
	public static final String XML_MESH_PKG =					DescriptorRecordSet.class.getPackage().getName(); //"org.ncbi.mesh";
	public static final String XML_BIOC_PKG =					Collection.class.getPackage().getName(); //"org.bioc";
	
	// -------------------------------------------------------------------------------------
	// TFM-XMLMODEL: BEAN
	public static final String NCBI_PUBMED_CONTEXT = 			"taoNcbiPubmedContext";
	public static final String NCBI_PUBMED_MARSHALLER = 		"taoNcbiPubmedMarshaller";
	public static final String NCBI_PUBMED_UNMARSHALLER = 		"taoNcbiPubmedUnmarshaller";

	public static final String NCBI_MESH_CONTEXT = 				"taoNcbiMeshContext";
	public static final String NCBI_MESH_MARSHALLER = 			"taoNcbiMeshMarshaller";
	public static final String NCBI_MESH_UNMARSHALLER = 		"taoNcbiMeshUnmarshaller";

	public static final String BIOC_CONTEXT = 					"taoBiocContext";
	public static final String BIOC_MARSHALLER = 				"taoBiocMarshaller";
	public static final String BIOC_UNMARSHALLER = 				"taoBiocUnmarshaller";

}
