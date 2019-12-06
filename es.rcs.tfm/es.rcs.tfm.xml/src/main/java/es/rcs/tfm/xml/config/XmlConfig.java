package es.rcs.tfm.xml.config;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.bioc.Collection;
import org.bioc.Document;
import org.bioc.Infon;
import org.bioc.Location;
import org.bioc.Node;
import org.bioc.Passage;
import org.bioc.Relation;
import org.bioc.Sentence;
import org.bioc.Text;
import org.ncbi.mesh.AllowableQualifier;
import org.ncbi.mesh.AllowableQualifiersList;
import org.ncbi.mesh.Concept;
import org.ncbi.mesh.ConceptList;
import org.ncbi.mesh.ConceptName;
import org.ncbi.mesh.ConceptRelation;
import org.ncbi.mesh.ConceptRelationList;
import org.ncbi.mesh.DateCreated;
import org.ncbi.mesh.DateEstablished;
import org.ncbi.mesh.DescriptorRecord;
import org.ncbi.mesh.DescriptorRecordSet;
import org.ncbi.mesh.DescriptorReferredTo;
import org.ncbi.mesh.ECIN;
import org.ncbi.mesh.ECOUT;
import org.ncbi.mesh.EntryCombination;
import org.ncbi.mesh.EntryCombinationList;
import org.ncbi.mesh.PharmacologicalAction;
import org.ncbi.mesh.PharmacologicalActionList;
import org.ncbi.mesh.PreviousIndexing;
import org.ncbi.mesh.PreviousIndexingList;
import org.ncbi.mesh.QualifierReferredTo;
import org.ncbi.mesh.RelatedRegistryNumber;
import org.ncbi.mesh.RelatedRegistryNumberList;
import org.ncbi.mesh.SeeRelatedDescriptor;
import org.ncbi.mesh.SeeRelatedList;
import org.ncbi.mesh.Term;
import org.ncbi.mesh.TermList;
import org.ncbi.mesh.ThesaurusID;
import org.ncbi.mesh.ThesaurusIDlist;
import org.ncbi.mesh.TreeNumber;
import org.ncbi.mesh.TreeNumberList;
import org.ncbi.pubmed.*;
import org.ncbi.pubmed.Object;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import es.rcs.tfm.xml.XmlNames;

@Configuration( XmlNames.XML_CONFIG )
@ComponentScan( basePackages = {
		XmlNames.XML_SETUP_PKG} )
public class XmlConfig {

	@Bean( name = XmlNames.BIOC_CONTEXT )
	public JAXBContext getJaxbBiocContext() {

		JAXBContext bean = null;
		try {
			bean = JAXBContext.newInstance(
					Collection.class,
					Document.class,
					Infon.class,
					Location.class,
					Node.class,
					Passage.class,
					Relation.class,
					Sentence.class,
					Text.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bean;
		
	}
	
	@Bean( name = XmlNames.BIOC_MARSHALLER )
	public Jaxb2Marshaller getBiocJaxbMarshaller () {
		
		Map<String, java.lang.Object> map = new HashMap<String, java.lang.Object>();
		map.put("jaxb.formatted.output", Boolean.TRUE);
	
		Jaxb2Marshaller bean = new Jaxb2Marshaller();
		bean.setPackagesToScan( XmlNames.XML_BIOC_PKG );
		bean.setMarshallerProperties(map);
		
		return bean;
		
	}
	
	@Bean( name = XmlNames.BIOC_UNMARSHALLER )
	public Unmarshaller getBiocJaxbUnmarshaller () throws JAXBException {
				
		Unmarshaller bean = getJaxbBiocContext().createUnmarshaller();
		
		return bean;
		
	}

	@Bean( name = XmlNames.NCBI_MESH_CONTEXT )
	public JAXBContext getJaxbMeshContext() {

		JAXBContext bean = null;
		try {
			bean = JAXBContext.newInstance(
					QualifierReferredTo.class,
					QualifierName.class,
					DateRevised.class,
					DescriptorRecordSet.class,
					DescriptorRecord.class,
					PreviousIndexing.class,
					ThesaurusID.class,
					EntryCombinationList.class,
					EntryCombination.class,
					Concept.class,
					ConceptName.class,
					RelatedRegistryNumberList.class,
					ConceptRelationList.class,
					TermList.class,
					ConceptRelation.class,
					Term.class,
					DateCreated.class,
					ThesaurusIDlist.class,
					ConceptList.class,
					TreeNumberList.class,
					TreeNumber.class,
					ECOUT.class,
					DescriptorReferredTo.class,
					ECIN.class,
					DescriptorName.class,
					DateEstablished.class,
					AllowableQualifiersList.class,
					PreviousIndexingList.class,
					SeeRelatedList.class,
					PharmacologicalActionList.class,
					AllowableQualifier.class,
					RelatedRegistryNumber.class,
					PharmacologicalAction.class,
					SeeRelatedDescriptor.class,
					ObjectFactory.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bean;
		
	}
	
	@Bean( name = XmlNames.NCBI_MESH_MARSHALLER )
	public Jaxb2Marshaller getMeshJaxbMarshaller () {
		
		Map<String, java.lang.Object> map = new HashMap<String, java.lang.Object>();
		map.put("jaxb.formatted.output", Boolean.TRUE);
	
		Jaxb2Marshaller bean = new Jaxb2Marshaller();
		bean.setPackagesToScan( XmlNames.XML_MESH_PKG );
		bean.setMarshallerProperties(map);
		
		return bean;
		
	}
	
	@Bean( name = XmlNames.NCBI_MESH_UNMARSHALLER )
	public Unmarshaller getMeshJaxbUnmarshaller () throws JAXBException {
	
		Unmarshaller bean = getJaxbMeshContext().createUnmarshaller();
		
		return bean;
		
	}
	
	@Bean( name = XmlNames.NCBI_PUBMED_CONTEXT )
	public JAXBContext getJaxbPubmedContext() {

		JAXBContext bean = null;
		try {
			bean = JAXBContext.newInstance(
					MedlinePgn.class,
					MmlFn.class,
					MmlPiecewise.class,
					MmlReln.class,
					MmlDeclare.class,
					MmlInterval.class,
					MmlInverse.class,
					MmlIdent.class,
					MmlDomain.class,
					MmlCodomain.class,
					MmlImage.class,
					MmlLn.class,
					MmlLog.class,
					MmlMoment.class,
					MmlLambda.class,
					MmlCompose.class,
					MmlQuotient.class,
					MmlDivide.class,
					MmlMinus.class,
					MmlPower.class,
					MmlRem.class,
					MmlRoot.class,
					MmlFactorial.class,
					MmlAbs.class,
					MmlConjugate.class,
					MmlArg.class,
					MmlReal.class,
					MmlImaginary.class,
					MmlFloor.class,
					MmlCeiling.class,
					MmlExp.class,
					MmlMax.class,
					MmlMin.class,
					MmlPlus.class,
					MmlTimes.class,
					MmlGcd.class,
					MmlLcm.class,
					MmlAnd.class,
					MmlOr.class,
					MmlXor.class,
					MmlNot.class,
					MmlImplies.class,
					MmlEquivalent.class,
					MmlForall.class,
					MmlExists.class,
					MmlEq.class,
					MmlGt.class,
					MmlLt.class,
					MmlGeq.class,
					MmlLeq.class,
					MmlNeq.class,
					MmlApprox.class,
					MmlFactorof.class,
					MmlTendsto.class,
					MmlInt.class,
					MmlDiff.class,
					MmlPartialdiff.class,
					MmlDivergence.class,
					MmlGrad.class,
					MmlCurl.class,
					MmlLaplacian.class,
					MmlSet.class,
					MmlList.class,
					MmlUnion.class,
					MmlIntersect.class,
					MmlCartesianproduct.class,
					MmlIn.class,
					MmlNotin.class,
					MmlNotsubset.class,
					MmlNotprsubset.class,
					MmlSetdiff.class,
					MmlSubset.class,
					MmlPrsubset.class,
					MmlCard.class,
					MmlSum.class,
					MmlProduct.class,
					MmlLimit.class,
					MmlSin.class,
					MmlCos.class,
					MmlTan.class,
					MmlSec.class,
					MmlCsc.class,
					MmlCot.class,
					MmlSinh.class,
					MmlCosh.class,
					MmlTanh.class,
					MmlSech.class,
					MmlCsch.class,
					MmlCoth.class,
					MmlArcsin.class,
					MmlArccos.class,
					MmlArctan.class,
					MmlArccosh.class,
					MmlArccot.class,
					MmlArccoth.class,
					MmlArccsc.class,
					MmlArccsch.class,
					MmlArcsec.class,
					MmlArcsech.class,
					MmlArcsinh.class,
					MmlArctanh.class,
					MmlMean.class,
					MmlSdev.class,
					MmlVariance.class,
					MmlMedian.class,
					MmlMode.class,
					MmlVector.class,
					MmlMatrix.class,
					MmlMatrixrow.class,
					MmlDeterminant.class,
					MmlTranspose.class,
					MmlSelector.class,
					MmlVectorproduct.class,
					MmlScalarproduct.class,
					MmlOuterproduct.class,
					MmlIntegers.class,
					MmlReals.class,
					MmlRationals.class,
					MmlNaturalnumbers.class,
					MmlComplexes.class,
					MmlPrimes.class,
					MmlEmptyset.class,
					MmlExponentiale.class,
					MmlImaginaryi.class,
					MmlNotanumber.class,
					MmlTrue.class,
					MmlFalse.class,
					MmlPi.class,
					MmlEulergamma.class,
					MmlInfinity.class,
					MmlSemantics.class,
					MmlCn.class,
					MmlCi.class,
					MmlCsymbol.class,
					MmlApply.class,
					MmlBind.class,
					MmlShare.class,
					MmlCerror.class,
					MmlCbytes.class,
					MmlCs.class,
					GeneSymbolList.class,
					GeneSymbol.class,
					History.class,
					PubMedPubDate.class,
					NameOfSubstance.class,
					Month.class,
					PubmedBookArticle.class,
					BookDocument.class,
					PubmedBookData.class,
					Sections.class,
					Section.class,
					MmlMsrow.class,
					MmlMi.class,
					MmlMn.class,
					MmlMo.class,
					MmlMtext.class,
					MmlMspace.class,
					MmlMs.class,
					MmlMaligngroup.class,
					MmlMalignmark.class,
					MmlMrow.class,
					MmlMfrac.class,
					MmlMsqrt.class,
					MmlMroot.class,
					MmlMstyle.class,
					MmlMerror.class,
					MmlMpadded.class,
					MmlMphantom.class,
					MmlMfenced.class,
					MmlMenclose.class,
					MmlMsub.class,
					MmlMsup.class,
					MmlMsubsup.class,
					MmlMunder.class,
					MmlMover.class,
					MmlMunderover.class,
					MmlMmultiscripts.class,
					MmlMtable.class,
					MmlMstack.class,
					MmlMlongdiv.class,
					MmlMaction.class,
					MmlNone.class,
					MmlBvar.class,
					MmlDomainofapplication.class,
					MmlCondition.class,
					MmlLowlimit.class,
					MmlUplimit.class,
					PubmedArticleSet.class,
					PubmedArticle.class,
					DeleteCitation.class,
					MmlMglyph.class,
					ForeName.class,
					MmlMomentabout.class,
					AbstractText.class,
					CommentsCorrectionsList.class,
					CommentsCorrections.class,
					Initials.class,
					Chemical.class,
					PMID.class,
					Author.class,
					LastName.class,
					Suffix.class,
					CollectiveName.class,
					Identifier.class,
					AffiliationInfo.class,
					AccessionNumber.class,
					MedlineDate.class,
					AccessionNumberList.class,
					Sub.class,
					Sup.class,
					ArticleId.class,
					Season.class,
					MedlineCitation.class,
					PubmedData.class,
					MmlMscarries.class,
					MmlMsline.class,
					MmlMsgroup.class,
					MmlMath.class,
					PersonalNameSubjectList.class,
					PersonalNameSubject.class,
					B.class,
					I.class,
					Publisher.class,
					ArticleIdList.class,
					LocationLabel.class,
					SectionTitle.class,
					QualifierName.class,
					Year.class,
					U.class,
					ObjectList.class,
					ReferenceList.class,
					ItemList.class,
					Item.class,
					EndPage.class,
					EndingDate.class,
					Day.class,
					MmlMscarry.class,
					ArticleTitle.class,
					DateRevised.class,
					SpaceFlightMission.class,
					MmlSep.class,
					MmlPiece.class,
					MmlDegree.class,
					MmlLogbase.class,
					Language.class,
					MeshHeadingList.class,
					MeshHeading.class,
					Grant.class,
					OtherID.class,
					DataBankList.class,
					DataBank.class,
					Param.class,
					Object.class,
					MmlMlabeledtr.class,
					MmlMtd.class,
					MmlOtherwise.class,
					GrantList.class,
					MmlMprescripts.class,
					MedlineJournalInfo.class,
					BeginningDate.class,
					CollectionTitle.class,
					CitationSubset.class,
					GeneralNote.class,
					JournalIssue.class,
					PubDate.class,
					BookDocumentSet.class,
					DeleteDocument.class,
					Book.class,
					BookTitle.class,
					AuthorList.class,
					InvestigatorList.class,
					Isbn.class,
					ELocationID.class,
					PubmedBookArticleSet.class,
					PublicationType.class,
					ISSN.class,
					Article.class,
					Journal.class,
					Pagination.class,
					Abstract.class,
					PublicationTypeList.class,
					ArticleDate.class,
					URL.class,
					MmlAnnotationXml.class,
					Investigator.class,
					OtherAbstract.class,
					Keyword.class,
					Reference.class,
					DateCompleted.class,
					ChemicalList.class,
					SupplMeshList.class,
					KeywordList.class,
					StartPage.class,
					ContractNumber.class,
					SupplMeshName.class,
					ContributionDate.class,
					DescriptorName.class,
					DispFormula.class,
					MmlAnnotation.class,
					MmlMtr.class,
					ObjectFactory.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bean;
		
	}
		
	@Bean( name = XmlNames.NCBI_PUBMED_MARSHALLER )
	public Jaxb2Marshaller getPubMedJaxbMarshaller () {
		
		Map<String, java.lang.Object> map = new HashMap<String, java.lang.Object>();
		map.put("jaxb.formatted.output", Boolean.TRUE);

		Jaxb2Marshaller bean = new Jaxb2Marshaller();
		bean.setPackagesToScan( XmlNames.XML_PUBMED_PKG );
		bean.setMarshallerProperties(map);
		
		return bean;
		
	}
	
	@Bean( name = XmlNames.NCBI_PUBMED_UNMARSHALLER )
	public Unmarshaller getPubMedJaxbUnmarshaller () throws JAXBException {
	
		Unmarshaller bean = getJaxbPubmedContext().createUnmarshaller();
		
		return bean;
		
	}

}
