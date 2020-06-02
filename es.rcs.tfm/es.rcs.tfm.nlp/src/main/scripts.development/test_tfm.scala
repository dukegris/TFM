// spark-shell --packages com.johnsnowlabs.nlp:spark-nlp_2.11:2.4.3,org.slf4j:slf4j-log4j12:1.7.28,es.rcs.tfm:RCS-Nlp:0.0.4-SNAPSHOT --executor-memory=32g --executor-cores=6 --driver-memory=24g --conf "spark.executor.extraJavaOptions='-Dlog4j.configuration=C:/Java/spark-2.4.5/conf/log4j.properties'" --conf "spark.driver.maxResultSize=8g"
// > dl.txt
import es.rcs.tfm.nlp.model.TfmType
import es.rcs.tfm.nlp.util.TfmHelper
import com.johnsnowlabs.nlp.RecursivePipeline

val stages = TfmHelper.trainPipelineFromStages(
	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner_all",
	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/bert_base_cased_en_2.4.0_2.4_1580579557778",
	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/tensorflow",
	512,
	768,
	false,
	32,
	1,
	1,
	1e-3f,
	5e-3f,
	0.5f,
	0.20f,
	32)

val pipeline = new RecursivePipeline().setStages(stages)





import spark.implicits._
val emptyData = spark.emptyDataset[String].toDF(TfmType.TEXT)

val data = spark.createDataFrame(Seq(
	(18, "Ehlers-Danlos syndrome, vascular type (vEDS) (MIM #130050) is an autosomal dominant disorder caused by type III procollagen gene (COL3A1) mutations. Most COL3A1 mutations are detected y using total RNA from patient-derived fibroblasts, which requires an invasive skin biopsy. High-resolution melting curve analysis (hrMCA) has recently been developed as a post-PCR mutation scanning method which enables simple, rapid, cost-effective, and highly sensitive mutation screening of large genes. We established a hrMCA method to screen for COL3A1 mutations using genomic DNA. PCR primers pairs for COL3A1 (52 amplicons) were designed to cover all coding regions of the 52 exons, including the splicing sites. We used 15 DNA samples (8 validation samples and 7 samples of clinically suspected vEDS patients) in this study. The eight known COL3A1 mutations in validation samples were all successfully detected by the hrMCA. In addition, we identified five novel COL3A1 mutations, including one deletion (c.2187delA) and one nonsense mutation (c.2992C>T) that could not be determined by the conventional total RNA method. Furthermore, we established a small amplicon genotyping (SAG) method for detecting three high frequency coding-region SNPs (rs1800255:G>A, rs1801184:T>C, and rs2271683:A>G) in COL3A1 to differentiate mutations before sequencing. The use of hrMCA in combination with SAG from genomic DNA enables rapid detection of COL3A1 mutations with high efficiency and specificity. A better understanding of the genotype-phenotype correlation in COL3A1 using this method will lead to improve in diagnosis and treatment."),
	(99, "HGNC:37133")
)).toDF("id", "text")


val stagesProduction = TfmHelper.productionPipelineStages(
	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/pos/pos_anc_en_2.0.2_2.4_1556659930154",
	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert/bert_base_cased_en_2.4.0_2.4_1580579557778", 
	"file://D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/ner/ner_dl_en_2.4.0_2.4_1580251789753", 
	512, 
	768,
	false, 
	32)

val pipeline = new RecursivePipeline().setStages(stages)
pipeline.fit(emptyData)
pipeline.transform(data)