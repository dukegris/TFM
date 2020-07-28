package es.rcs.tfm.srv.setup;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.lang3.StringUtils;
import org.nlm.mesh.DateCreated;
import org.nlm.mesh.DateEstablished;
import org.nlm.mesh.DateRevised;
import org.nlm.mesh.DescriptorRecord;
import org.nlm.mesh.DescriptorRecordSet;
import org.nlm.mesh.EntryCombinationList;
import org.nlm.mesh.PharmacologicalActionList;
import org.nlm.mesh.PharmacologicalActionSet;
import org.nlm.mesh.PreviousIndexingList;
import org.nlm.mesh.QualifierRecord;
import org.nlm.mesh.QualifierRecordSet;
import org.nlm.mesh.SeeRelatedList;
import org.nlm.mesh.SupplementalRecordSet;

import es.rcs.tfm.srv.SrvException;
import es.rcs.tfm.srv.SrvException.SrvViolation;
import es.rcs.tfm.srv.model.Articulo.OwnerType;
import es.rcs.tfm.srv.model.Descriptor;
import es.rcs.tfm.srv.model.Fecha;
import es.rcs.tfm.srv.model.Fecha.DateType;
import es.rcs.tfm.srv.model.Termino;
import es.rcs.tfm.srv.model.Termino.DescType;
import es.rcs.tfm.srv.model.Termino.TermType;

public class MeshXmlProcessor implements Iterator<Termino> {

	private List<?> items = null;
	private boolean allOk = false;
	private int index = 0;

	public MeshXmlProcessor(Path path) {

		SAXSource source = ArticleProcessor.getSourceFromPath(path);
		if (source != null) {
			try {

				JAXBContext jaxbContext = JAXBContext.newInstance(DescriptorRecordSet.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				Object recordSet = jaxbUnmarshaller.unmarshal(source);
				if ((recordSet != null) && (recordSet instanceof DescriptorRecordSet)) {
					DescriptorRecordSet descriptorRecordSet = (DescriptorRecordSet) recordSet;
					if (	(descriptorRecordSet.getDescriptorRecord() != null) && 
							(!descriptorRecordSet.getDescriptorRecord().isEmpty())) {
						items = descriptorRecordSet.getDescriptorRecord();
						allOk = true;
					}
				} else if ((recordSet != null) && (recordSet instanceof QualifierRecordSet)) {
					QualifierRecordSet qualifierRecordSet = (QualifierRecordSet) recordSet;
					if (	(qualifierRecordSet.getQualifierRecord() != null) && 
							(!qualifierRecordSet.getQualifierRecord().isEmpty())) {
						items = qualifierRecordSet.getQualifierRecord();
						allOk = true;
					}
				} else if ((recordSet != null) && (recordSet instanceof SupplementalRecordSet)) {
					SupplementalRecordSet supplementalRecordSet = (SupplementalRecordSet) recordSet;
					if (	(supplementalRecordSet.getSupplementalRecord() != null) && 
							(!supplementalRecordSet.getSupplementalRecord().isEmpty())) {
						items = supplementalRecordSet.getSupplementalRecord();
						allOk = true;
					}
				} else if ((recordSet != null) && (recordSet instanceof PharmacologicalActionSet)) {
					PharmacologicalActionSet pharmacologicalActionSet = (PharmacologicalActionSet) recordSet;
					if (	(pharmacologicalActionSet.getPharmacologicalAction() != null) && 
							(!pharmacologicalActionSet.getPharmacologicalAction().isEmpty())) {
						items = pharmacologicalActionSet.getPharmacologicalAction();
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
		if (items == null) return false;
		return (index < items.size() - 1);
	}

	@Override
	public Termino next() {

		if ((!this.allOk) || (!this.hasNext())) {
			this.allOk = false;
			throw new SrvException(SrvViolation.NO_MORE_DATA, "No hay mas datos");
		}

		Termino result = null;

		Object item = items.get(index);
		if (item instanceof DescriptorRecord ) {
			result = getDescriptor((DescriptorRecord)item);			
		} else if (item instanceof QualifierRecordSet) {
			result = getQualifier((QualifierRecord)item);			
		} else if (item instanceof SupplementalRecordSet) {
			//result = getSupplemental((SupplementalRecordSet)item);			
		} else if (item instanceof PharmacologicalActionSet) {
			//result = getPharmalogical((PharmacologicalActionSet)item);			
		}
		

		this.index++;

		return result;

	}

	private Termino getQualifier(QualifierRecord item) {

		Termino obj = new Termino(TermType.QUALIFIER, DescType.NONE);

		obj.setCode(item.getQualifierUI());
		if ((item.getQualifierName() != null) && StringUtils.isNotBlank(item.getQualifierName().getString())) {
			obj.setValue(item.getQualifierName().getString());
		}
		obj.addDate(makeFecha(DateType.CREATION, item.getDateCreated()));
		obj.addDate(makeFecha(DateType.ESTABLISH, item.getDateEstablished()));
		obj.addDate(makeFecha(DateType.REVISION, item.getDateRevised()));

		obj.addNote(makeNota(OwnerType.PUBMED_NOTE, item.getAnnotation()));
		obj.addNote(makeNota(OwnerType.HISTORY_NOTE, item.getHistoryNote()));
		obj.addNote(makeNota(OwnerType.ONLINE_NOTE, item.getOnlineNote()));

/*
		qualifierRecordSet.getLanguageCode();

		qualifierRecordSet.getQualifierRecord().get(0).getConceptList();
		qualifierRecordSet.getQualifierRecord().get(0).getTreeNumberList();
		//SupplementalRecord supplementalRecord;
*/
		// TODO Auto-generated method stub
		return null;
	}

	private Termino getDescriptor(DescriptorRecord item) {

		Termino obj = new Termino(TermType.DESCRIPTOR, DescType.NONE);

		obj.setCode(item.getDescriptorUI());
		if ((item.getDescriptorName() != null) && StringUtils.isNotBlank(item.getDescriptorName().getString())) {
			obj.setValue(item.getDescriptorName().getString());
		}
		obj.addDate(makeFecha(DateType.CREATION, item.getDateCreated()));
		obj.addDate(makeFecha(DateType.ESTABLISH, item.getDateEstablished()));
		obj.addDate(makeFecha(DateType.REVISION, item.getDateRevised()));

		obj.addNote(makeNota(OwnerType.PUBMED_NOTE, item.getAnnotation()));
		obj.addNote(makeNota(OwnerType.HISTORY_NOTE, item.getHistoryNote()));
		obj.addNote(makeNota(OwnerType.ONLINE_NOTE, item.getOnlineNote()));
		obj.addNote(makeNota(OwnerType.MESH_NOTE, item.getPublicMeSHNote()));
		obj.addNotes(makeVersiones(OwnerType.INDEX_NOTE, item.getPreviousIndexingList()));

		obj.addSubterms(makeVease(item.getSeeRelatedList()));
		obj.addSubterms(makeFarmacologia(item.getPharmacologicalActionList()));
		obj.addSubterms(makeRelaciones(item.getEntryCombinationList()));
		/*

		obj.addClas(makeClass(item.getDescriptorClass()));
		obj.setConsiderar(item.getConsiderAlso());
		obj.setClasificacion(item.getNLMClassificationNumber());

		obj.addEnlaces(makeEnlaces(item.getTreeNumberList()));
		obj.addConceptos(makeConceptos(item.getConceptList()));
		obj.addCualificadores(makeCualificadores(item.getAllowableQualifiersList()));

		item.getConsiderAlso();
		item.getNLMClassificationNumber();
		item.getDescriptorClass();
		item.getTreeNumberList().getTreeNumber().get(0).getvalue();
		item.getConceptList().getConcept().get(0).getCASN1Name();
		item.getConceptList().getConcept().get(0).getConceptName().getString();
		item.getConceptList().getConcept().get(0).getConceptRelationList().getConceptRelation().get(0).getConcept1UI();
		item.getConceptList().getConcept().get(0).getConceptRelationList().getConceptRelation().get(0).getConcept2UI();
		item.getConceptList().getConcept().get(0).getConceptRelationList().getConceptRelation().get(0).getRelationName();
		item.getConceptList().getConcept().get(0).getConceptUI();
		item.getConceptList().getConcept().get(0).getPreferredConceptYN();
		item.getConceptList().getConcept().get(0).getRegistryNumber();
		item.getConceptList().getConcept().get(0).getRelatedRegistryNumberList().getRelatedRegistryNumber().get(0).getvalue();
		item.getConceptList().getConcept().get(0).getScopeNote();
		item.getConceptList().getConcept().get(0).getTermList().getTerm().get(0).getTermUI();
		item.getConceptList().getConcept().get(0).getTermList().getTerm().get(0).getTermNote();
		item.getConceptList().getConcept().get(0).getTermList().getTerm().get(0).getAbbreviation();
		item.getConceptList().getConcept().get(0).getTermList().getTerm().get(0).getConceptPreferredTermYN();
		item.getConceptList().getConcept().get(0).getTermList().getTerm().get(0).getDateCreated();
		item.getConceptList().getConcept().get(0).getTermList().getTerm().get(0).getEntryVersion();
		item.getConceptList().getConcept().get(0).getTermList().getTerm().get(0).getIsPermutedTermYN();
		item.getConceptList().getConcept().get(0).getTermList().getTerm().get(0).getLexicalTag();
		item.getConceptList().getConcept().get(0).getTermList().getTerm().get(0).getRecordPreferredTermYN();
		item.getConceptList().getConcept().get(0).getTermList().getTerm().get(0).getSortVersion();
		item.getConceptList().getConcept().get(0).getTermList().getTerm().get(0).getString();
		item.getConceptList().getConcept().get(0).getTermList().getTerm().get(0).getThesaurusIDlist().getThesaurusID().get(0).getvalue();
		item.getConceptList().getConcept().get(0).getRegistryNumber();
		item.getAllowableQualifiersList().getAllowableQualifier().get(0).getAbbreviation();
		item.getAllowableQualifiersList().getAllowableQualifier().get(0).getQualifierReferredTo();
		*/

		return obj;
		
	}

	/*

	private Object makeEnlaces(TreeNumberList list) {
		return null;
	}


	private Object makeConceptos(ConceptList list) {
		list.getConcept().get(0).getConceptName().getString();
		list.getConcept().get(0).getCASN1Name().codePointAt(0);
		
		return null;
	}

	private Object makeCualificadores(AllowableQualifiersList list) {
		return null;
	}
*/
	private List<Termino> makeFarmacologia(PharmacologicalActionList list) {

		if (	(list == null) || 
				(list.getPharmacologicalAction() == null) || 
				(list.getPharmacologicalAction().isEmpty())) return null;

		List<Termino> result = list.
			getPharmacologicalAction().
			stream().
			filter(p -> 	(p != null) && 
							(p.getDescriptorReferredTo() != null)).
			map(instance -> {
				String code = instance.getDescriptorReferredTo().getDescriptorUI();
				String value = null;
				if (	(instance.getDescriptorReferredTo() != null) &&
						(StringUtils.isNotBlank(instance.getDescriptorReferredTo().getDescriptorName().getString()))) {
					value = instance.getDescriptorReferredTo().getDescriptorName().getString();
				}
				return new Termino(
						TermType.DESCRIPTOR,
						DescType.CHEMICAL,
						code, 
						value);
			}).
			filter(p -> 	(p != null)).
			collect(Collectors.toList());

		return result;
	}

	private List<Termino> makeVease(SeeRelatedList list) {

		if (	(list == null) || 
				(list.getSeeRelatedDescriptor() == null) || 
				(list.getSeeRelatedDescriptor().isEmpty())) return null;

		List<Termino> result = list.
			getSeeRelatedDescriptor().
			stream().
			filter(p -> 	(p != null) && 
							(p.getDescriptorReferredTo() != null)).
			map(instance -> {
				String code = instance.getDescriptorReferredTo().getDescriptorUI();
				String value = null;
				if (	(instance.getDescriptorReferredTo() != null) &&
						(StringUtils.isNotBlank(instance.getDescriptorReferredTo().getDescriptorName().getString()))) {
					value = instance.getDescriptorReferredTo().getDescriptorName().getString();
				}
				return new Termino(
						TermType.DESCRIPTOR,
						DescType.RELATED,
						code, 
						value);
			}).
			filter(p -> 	(p != null)).
			collect(Collectors.toList());

		return result;

	}

	private List<Termino> makeRelaciones(EntryCombinationList list) {

		if (	(list == null) || 
				(list.getEntryCombination() == null) || 
				(list.getEntryCombination().isEmpty())) return null;

		List<Termino> result = list.
			getEntryCombination().
			stream().
			filter(p -> 	(p != null) && (
							(p.getECIN() != null) ||
							(p.getECOUT() != null))
							).
			flatMap(instance -> {
				List<Termino> items = new ArrayList<>();
				String code = null;
				String value = null;
				
				if (	(instance.getECIN() != null) &&
						(instance.getECIN().getDescriptorReferredTo() != null) &&
						(instance.getECIN().getDescriptorReferredTo().getDescriptorUI() != null)) {
					code = instance.getECIN().getDescriptorReferredTo().getDescriptorUI();
					value = null;
					if (	(StringUtils.isNotBlank(instance.getECIN().getDescriptorReferredTo().getDescriptorName().getString()))) {
						value = instance.getECIN().getDescriptorReferredTo().getDescriptorName().getString();
					}
					items.add(new Termino(
							TermType.DESCRIPTOR, 
							DescType.ECIN, 
							code, 
							value)); 
				}

				if (	(instance.getECIN() != null) &&
						(instance.getECIN().getQualifierReferredTo() != null) &&
						(instance.getECIN().getQualifierReferredTo().getQualifierUI() != null)) {
					code = instance.getECIN().getQualifierReferredTo().getQualifierUI();
					value = null;
					if (	(StringUtils.isNotBlank(instance.getECIN().getQualifierReferredTo().getQualifierName().getString()))) {
						value = instance.getECIN().getQualifierReferredTo().getQualifierName().getString();
					}
					items.add(new Termino(
							TermType.QUALIFIER, 
							DescType.ECIN, 
							code, 
							value)); 
				}
				
				if (	(instance.getECIN() != null) &&
						(instance.getECIN().getDescriptorReferredTo() != null) &&
						(instance.getECIN().getDescriptorReferredTo().getDescriptorUI() != null)) {
					code = instance.getECIN().getDescriptorReferredTo().getDescriptorUI();
					value = null;
					if (	(StringUtils.isNotBlank(instance.getECIN().getDescriptorReferredTo().getDescriptorName().getString()))) {
						value = instance.getECIN().getDescriptorReferredTo().getDescriptorName().getString();
					}
					items.add(new Termino(
							TermType.DESCRIPTOR, 
							DescType.ECOUT, 
							code, 
							value)); 
				}

				if (	(instance.getECIN() != null) &&
						(instance.getECIN().getQualifierReferredTo() != null) &&
						(instance.getECIN().getQualifierReferredTo().getQualifierUI() != null)) {
					code = instance.getECIN().getQualifierReferredTo().getQualifierUI();
					value = null;
					if (	(StringUtils.isNotBlank(instance.getECIN().getQualifierReferredTo().getQualifierName().getString()))) {
						value = instance.getECIN().getQualifierReferredTo().getQualifierName().getString();
					}
					items.add(new Termino(
							TermType.QUALIFIER, 
							DescType.ECOUT, 
							code, 
							value)); 
				}

				return items.stream();
			}).
			filter(p -> 	(p != null)).
			collect(Collectors.toList());

		return result;

	}

	private List<Descriptor> makeVersiones(OwnerType type, PreviousIndexingList list) {

		if (	(list == null) || 
				(list.getPreviousIndexing() == null) || 
				(list.getPreviousIndexing().isEmpty())) return null;

		List<Descriptor> result = list.
			getPreviousIndexing().
			stream().
			filter(p -> 	(p != null) && 
							(StringUtils.isNotBlank(p.getvalue()))).
			map(instance -> new Descriptor(type, instance.getvalue())).
			filter(p -> p!=null).
			collect(Collectors.toList());

		return result;

	}

	private Descriptor makeNota(OwnerType owner, String value) {

		if (	(owner == null)  ||
				(OwnerType.NONE.equals(owner))  ||
				(StringUtils.isNotBlank(value))) {
			return null; 
		}
		return new Descriptor(owner, value);
		
	}

	private Fecha makeFecha(DateType type, DateRevised date) {
		
		if (date == null) return null;

		int year = 1;
		try { if (StringUtils.isNotBlank(date.getYear())) year = Integer.parseInt(date.getYear()); } catch (Exception ex) {}

		int month = 1;
		try { if (StringUtils.isNotBlank(date.getMonth())) month = Integer.parseInt(date.getMonth()); } catch (Exception ex) {}
		
		int dayOfMonth = 1;
		try { if (StringUtils.isNotBlank(date.getDay())) dayOfMonth = Integer.parseInt(date.getDay()); } catch (Exception ex) {}
		
		Fecha resultado = null;
		LocalDate localdate = LocalDate.of(year, month, dayOfMonth);
		if (localdate != null) {
			resultado = new Fecha(type, localdate);
		}
	
		return resultado;

	}

	private Fecha makeFecha(DateType type, DateEstablished date) {
		
		if (date == null) return null;

		int year = 1;
		try { if (StringUtils.isNotBlank(date.getYear())) year = Integer.parseInt(date.getYear()); } catch (Exception ex) {}

		int month = 1;
		try { if (StringUtils.isNotBlank(date.getMonth())) month = Integer.parseInt(date.getMonth()); } catch (Exception ex) {}
		
		int dayOfMonth = 1;
		try { if (StringUtils.isNotBlank(date.getDay())) dayOfMonth = Integer.parseInt(date.getDay()); } catch (Exception ex) {}
		
		Fecha resultado = null;
		LocalDate localdate = LocalDate.of(year, month, dayOfMonth);
		if (localdate != null) {
			resultado = new Fecha(type, localdate);
		}
	
		return resultado;

	}

	private Fecha makeFecha(DateType type, DateCreated date) {
		
		if (date == null) return null;

		int year = 1;
		try { if (StringUtils.isNotBlank(date.getYear())) year = Integer.parseInt(date.getYear()); } catch (Exception ex) {}

		int month = 1;
		try { if (StringUtils.isNotBlank(date.getMonth())) month = Integer.parseInt(date.getMonth()); } catch (Exception ex) {}
		
		int dayOfMonth = 1;
		try { if (StringUtils.isNotBlank(date.getDay())) dayOfMonth = Integer.parseInt(date.getDay()); } catch (Exception ex) {}
		
		Fecha resultado = null;
		LocalDate localdate = LocalDate.of(year, month, dayOfMonth);
		if (localdate != null) {
			resultado = new Fecha(type, localdate);
		}
	
		return resultado;

	}
	
}
