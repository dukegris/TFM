package es.rcs.tfm.xml.config;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.bioc.*;
import org.ncbi.pubmed.*;
import org.nlm.mesh.*;
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
					Annotation.class,
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
					AllowableQualifier.class,
					AllowableQualifiersList.class,
					Concept.class,
					ConceptList.class,
					ConceptName.class,
					ConceptRelation.class,
					ConceptRelationList.class,
					DateCreated.class,
					DateEstablished.class,
					org.nlm.mesh.DateRevised.class,
					org.nlm.mesh.DescriptorName.class,
					DescriptorRecord.class,
					DescriptorRecordSet.class,
					DescriptorReferredTo.class,
					ECIN.class,
					ECOUT.class,
					EntryCombination.class,
					EntryCombinationList.class,
					HeadingMappedTo.class,
					HeadingMappedToList.class,
					IndexingInformation.class,
					IndexingInformationList.class,
					PharmacologicalAction.class,
					PharmacologicalActionList.class,
					PharmacologicalActionSet.class,
					PharmacologicalActionSubstanceList.class,
					PreviousIndexing.class,
					PreviousIndexingList.class,
					org.nlm.mesh.QualifierName.class,
					QualifierRecord.class,
					QualifierRecordSet.class,			
					QualifierReferredTo.class,
					RecordName.class,
					org.nlm.mesh.RecordUI.class,
					RelatedRegistryNumber.class,
					RelatedRegistryNumberList.class,
					SeeRelatedDescriptor.class,
					SeeRelatedList.class,
					Source.class,
					SourceList.class,
					Substance.class,
					SupplementalRecord.class,
					SupplementalRecordName.class,
					SupplementalRecordSet.class,
					Term.class,
					TermList.class,
					ThesaurusID.class,
					ThesaurusIDlist.class,
					TreeNumber.class,
					TreeNumberList.class);
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
					Abstract.class,
					AbstractText.class,
					AccessionNumber.class,
					AccessionNumberList.class,
					AffiliationInfo.class,
					Article.class,
					ArticleDate.class,
					ArticleId.class,
					ArticleIdList.class,
					ArticleTitle.class,
					Author.class,
					AuthorList.class,
					B.class,
					BeginningDate.class,
					Book.class,
					BookDocument.class,
					BookDocumentSet.class,
					BookTitle.class,
					Chemical.class,
					ChemicalList.class,
					CitationSubset.class,
					CollectionTitle.class,
					CollectiveName.class,
					CommentsCorrections.class,
					CommentsCorrectionsList.class,
					ContractNumber.class,
					ContributionDate.class,
					DataBank.class,
					DataBankList.class,
					DateCompleted.class,
					org.ncbi.pubmed.DateRevised.class,
					Day.class,
					DeleteCitation.class,
					DeleteDocument.class,
					org.ncbi.pubmed.DescriptorName.class,
					DispFormula.class,
					ELocationID.class,
					EndingDate.class,
					EndPage.class,
					ForeName.class,
					GeneralNote.class,
					GeneSymbol.class,
					GeneSymbolList.class,
					Grant.class,
					GrantList.class,
					History.class,
					I.class,
					Identifier.class,
					Initials.class,
					Investigator.class,
					InvestigatorList.class,
					Isbn.class,
					ISSN.class,
					Item.class,
					ItemList.class,
					Journal.class,
					JournalIssue.class,
					Keyword.class,
					KeywordList.class,
					Language.class,
					LastName.class,
					LocationLabel.class,
					MedlineCitation.class,
					MedlineDate.class,
					MedlineJournalInfo.class,
					MedlinePgn.class,
					MeshHeading.class,
					MeshHeadingList.class,
/*
					MmlAbs.class,
					MmlAnd.class,
					MmlAnnotation.class,
					MmlAnnotationXml.class,
					MmlApply.class,
					MmlApprox.class,
					MmlArccos.class,
					MmlArccosh.class,
					MmlArccot.class,
					MmlArccoth.class,
					MmlArccsc.class,
					MmlArccsch.class,
					MmlArcsec.class,
					MmlArcsech.class,
					MmlArcsin.class,
					MmlArcsinh.class,
					MmlArctan.class,
					MmlArctanh.class,
					MmlArg.class,
					MmlBind.class,
					MmlBvar.class,
					MmlCard.class,
					MmlCartesianproduct.class,
					MmlCbytes.class,
					MmlCeiling.class,
					MmlCerror.class,
					MmlCi.class,
					MmlCn.class,
					MmlCodomain.class,
					MmlComplexes.class,
					MmlCompose.class,
					MmlCondition.class,
					MmlConjugate.class,
					MmlCos.class,
					MmlCosh.class,
					MmlCot.class,
					MmlCoth.class,
					MmlCs.class,
					MmlCsc.class,
					MmlCsch.class,
					MmlCsymbol.class,
					MmlCurl.class,
					MmlDeclare.class,
					MmlDegree.class,
					MmlDeterminant.class,
					MmlDiff.class,
					MmlDivergence.class,
					MmlDivide.class,
					MmlDomain.class,
					MmlDomainofapplication.class,
					MmlEmptyset.class,
					MmlEq.class,
					MmlEquivalent.class,
					MmlEulergamma.class,
					MmlExists.class,
					MmlExp.class,
					MmlExponentiale.class,
					MmlFactorial.class,
					MmlFactorof.class,
					MmlFalse.class,
					MmlFloor.class,
					MmlFn.class,
					MmlForall.class,
					MmlGcd.class,
					MmlGeq.class,
					MmlGrad.class,
					MmlGt.class,
					MmlIdent.class,
					MmlImage.class,
					MmlImaginary.class,
					MmlImaginaryi.class,
					MmlImplies.class,
					MmlIn.class,
					MmlInfinity.class,
					MmlInt.class,
					MmlIntegers.class,
					MmlIntersect.class,
					MmlInterval.class,
					MmlInverse.class,
					MmlLambda.class,
					MmlLaplacian.class,
					MmlLcm.class,
					MmlLeq.class,
					MmlLimit.class,
					MmlList.class,
					MmlLn.class,
					MmlLog.class,
					MmlLogbase.class,
					MmlLowlimit.class,
					MmlLt.class,
					MmlMaction.class,
					MmlMaligngroup.class,
					MmlMalignmark.class,
					MmlMath.class,
					MmlMatrix.class,
					MmlMatrixrow.class,
					MmlMax.class,
					MmlMean.class,
					MmlMedian.class,
					MmlMenclose.class,
					MmlMerror.class,
					MmlMfenced.class,
					MmlMfrac.class,
					MmlMglyph.class,
					MmlMi.class,
					MmlMin.class,
					MmlMinus.class,
					MmlMlabeledtr.class,
					MmlMlongdiv.class,
					MmlMmultiscripts.class,
					MmlMn.class,
					MmlMo.class,
					MmlMode.class,
					MmlMoment.class,
					MmlMomentabout.class,
					MmlMover.class,
					MmlMpadded.class,
					MmlMphantom.class,
					MmlMprescripts.class,
					MmlMroot.class,
					MmlMrow.class,
					MmlMs.class,
					MmlMscarries.class,
					MmlMscarry.class,
					MmlMsgroup.class,
					MmlMsline.class,
					MmlMspace.class,
					MmlMsqrt.class,
					MmlMsrow.class,
					MmlMstack.class,
					MmlMstyle.class,
					MmlMsub.class,
					MmlMsubsup.class,
					MmlMsup.class,
					MmlMtable.class,
					MmlMtd.class,
					MmlMtext.class,
					MmlMtr.class
					MmlMunder.class,
					MmlMunderover.class,
					MmlNaturalnumbers.class,
					MmlNeq.class,
					MmlNone.class,
					MmlNot.class,
					MmlNotanumber.class,
					MmlNotin.class,
					MmlNotprsubset.class,
					MmlNotsubset.class,
					MmlOr.class,
					MmlOtherwise.class,
					MmlOuterproduct.class,
					MmlPartialdiff.class,
					MmlPi.class,
					MmlPiece.class,
					MmlPiecewise.class,
					MmlPlus.class,
					MmlPower.class,
					MmlPrimes.class,
					MmlProduct.class,
					MmlPrsubset.class,
					MmlQuotient.class,
					MmlRationals.class,
					MmlReal.class,
					MmlReals.class,
					MmlReln.class,
					MmlRem.class,
					MmlRoot.class,
					MmlScalarproduct.class,
					MmlSdev.class,
					MmlSec.class,
					MmlSech.class,
					MmlSelector.class,
					MmlSemantics.class,
					MmlSep.class,
					MmlSet.class,
					MmlSetdiff.class,
					MmlShare.class,
					MmlSin.class,
					MmlSinh.class,
					MmlSubset.class,
					MmlSum.class,
					MmlTan.class,
					MmlTanh.class,
					MmlTendsto.class,
					MmlTimes.class,
					MmlTranspose.class,
					MmlTrue.class,
					MmlUnion.class,
					MmlUplimit.class,
					MmlVariance.class,
					MmlVector.class,
					MmlVectorproduct.class,
					MmlXor.class,
*/					
					Month.class,
					NameOfSubstance.class,
					org.ncbi.pubmed.Object.class,
					ObjectList.class,
					OtherAbstract.class,
					OtherID.class,
					Pagination.class,
					Param.class,
					PersonalNameSubject.class,
					PersonalNameSubjectList.class,
					PMID.class,
					PubDate.class,
					PublicationType.class,
					PublicationTypeList.class,
					Publisher.class,
					PubmedArticle.class,
					PubmedArticleSet.class,
					PubmedBookArticle.class,
					PubmedBookArticleSet.class,
					PubmedBookData.class,
					PubmedData.class,
					PubMedPubDate.class,
					org.ncbi.pubmed.QualifierName.class,
					Reference.class,
					ReferenceList.class,
					Season.class,
					Section.class,
					Sections.class,
					SectionTitle.class,
					SpaceFlightMission.class,
					StartPage.class,
					Sub.class,
					Suffix.class,
					Sup.class,
					SupplMeshList.class,
					SupplMeshName.class,
					U.class,
					URL.class,
					Year.class);
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
