package es.rcs.tfm.srv.setup;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.ncbi.mesh.DescriptorRecord;
import org.ncbi.mesh.DescriptorRecordSet;

import es.rcs.tfm.srv.SrvException;
import es.rcs.tfm.srv.SrvException.SrvViolation;
import es.rcs.tfm.srv.model.Descriptor;

public class MeshXmlProcessor implements Iterator<Descriptor> {
	
	private List<DescriptorRecord> items = null;
	private boolean allOk = false;
	private int index = 0;

	public MeshXmlProcessor(Path path) {
		
        SAXSource source = ArticleProcessor.getSourceFromPath(path);
        if (source != null) {
			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(DescriptorRecordSet.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			    DescriptorRecordSet descriptorRecordSet = (DescriptorRecordSet)jaxbUnmarshaller.unmarshal(source);
			    if (descriptorRecordSet != null) {
			    	if (	(descriptorRecordSet.getDescriptorRecord() != null) && 
			    			(!descriptorRecordSet.getDescriptorRecord().isEmpty())) {
						items = descriptorRecordSet.getDescriptorRecord();
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
	public Descriptor next() {
		
		if (	(!this.allOk) ||
				(!this.hasNext())) {
			this.allOk = false;
			throw new SrvException(SrvViolation.NO_MORE_DATA, "No hay mas datos");
		}

		Descriptor result = null;

		DescriptorRecord item = items.get(index);
		result = getDescriptor(item);

		this.index++;

		return result;
		
	}

	private Descriptor getDescriptor(DescriptorRecord item) {
		// TODO Auto-generated method stub
		return null;
	}


}
