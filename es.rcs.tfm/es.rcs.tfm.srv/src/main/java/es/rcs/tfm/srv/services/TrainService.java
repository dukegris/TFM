package es.rcs.tfm.srv.services;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FilenameUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.model.MarkedText;
import es.rcs.tfm.srv.repository.TrainRepository;
import es.rcs.tfm.srv.setup.BiocXmlProcessor;
import es.rcs.tfm.srv.setup.Conll2003Writer;
import es.rcs.tfm.srv.setup.PubtatorTxtProcessor;
import es.rcs.tfm.xml.XmlNames;

@Service(value = SrvNames.TRAINING_SRVC)
@DependsOn(value = {
		SrvNames.SPARK_SESSION_TRAIN,
		XmlNames.BIOC_CONTEXT})
@PropertySource(
		{"classpath:/META-INF/service.properties"} )
public class TrainService {

	private static final Logger LOG = LoggerFactory.getLogger(TrainService.class);

	private @Value("${tfm.training.tmvar.bronco.texts}")	String TRAIN_BRONCO_TEXT =		"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/bronco/BRONCO-A_Abstractss.txt";
	private @Value("${tfm.training.tmvar.bronco.answers}")	String TRAIN_BRONCO_ANSWERS =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/bronco/BRONCO-A_Answers.txt";

	private @Value("${tfm.training.tmvar.train.texts}")		String TRAIN_TMVARS_TEXT =		"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/bronco/corpus[tmVar]_abstracts.txt";
	private @Value("${tfm.training.tmvar.train.answers}")	String TRAIN_TMVARS_ANSWERS =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/bronco/corpus[tmVar]_answers.txt";

	private @Value("${tfm.training.tmvar.train.pubtator}")	String TRAIN_TMVARS_TXT_TRAIN =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmVar/train.PubTator";
	private @Value("${tfm.training.tmvar.test.pubtator}")	String TRAIN_TMVARS_TXT_TEST =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmVar/test.PubTator";

	private @Value("${tfm.training.tmvar.train.bioc}")		String TRAIN_TMVARS_XML_TRAIN =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmVar/train.BioC.txt";
	private @Value("${tfm.training.tmvar.test.bioc}")		String TRAIN_TMVARS_XML_TEST =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/tmVar/test.Bioc.txt";
			
	private @Value("${tfm.training.ner.directory}")			String TRAINING_NER_DIRECTORY =	"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/training/ner";

	private @Value("${tfm.model.bert_uncased.directory}")	String BERT_UNCASED_MODEL =		"D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/models/bert_uncased_en_2.0.2_2.4_1556651478920";
	
	
	private static final String TMVAR_TXT_TRAIN =	"tmvar.txt.train" + Conll2003Writer.CONLL_EXT;
	private static final String TMVAR_TXT_TEST =	"tmvar.txt.test" + Conll2003Writer.CONLL_EXT;
	private static final String TMVAR_BIOC_TRAIN =	"tmvar.bioc.train" + Conll2003Writer.CONLL_EXT;
	private static final String TMVAR_BIOC_TEST =	"tmvar.bioc.test" + Conll2003Writer.CONLL_EXT;
	
	public void prepareDataForTraining(SparkSession spark) {
		
		TrainRepository.getConllFrom(
				spark, 
				new PubtatorTxtProcessor(Paths.get(TRAIN_TMVARS_TXT_TRAIN)), 
				BERT_UNCASED_MODEL, 
				FilenameUtils.concat(TRAINING_NER_DIRECTORY, TMVAR_TXT_TRAIN));

		/*
		Path path = null; 
		Stream<MarkedText> stream = null;
		List<MarkedText> a = null;

		path = Paths.get(TRAIN_TMVARS_TXT_TEST);
		stream = StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(
						new PubtatorTxtProcessor(path), 
						Spliterator.DISTINCT), 
				false);
		a = stream.collect(Collectors.toList());
		System.out.println(a.size());

		path = Paths.get(TRAIN_TMVARS_XML_TRAIN);
		stream = StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(
						new BiocXmlProcessor(path), 
						Spliterator.DISTINCT), 
				false);
		a = stream.collect(Collectors.toList());
		System.out.println("" + a.size());

		path = Paths.get(TRAIN_TMVARS_XML_TEST);
		stream = StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(
						new BiocXmlProcessor(path), 
						Spliterator.DISTINCT), 
				false);
		a = stream.collect(Collectors.toList());
		System.out.println(a.size());
		*/
	}
	
	/*
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hhmmss");
	private void process(Dataset<Row> result) {
		
		// List<Row> rowslist = result.collectAsList();
System.out.println("COLS: " + sdf.format(new Date()));	
		
		Arrays.asList(result.columns()).forEach(c -> {
			System.out.print(c + ", ");
		});
		System.out.println();
		
System.out.println("BEFORE: " + sdf.format(new Date()));
		try {
			System.out.println("C: " + result.count());
		} catch (Exception ex) {
			System.out.println("EX: " + ex.toString());
		}
System.out.println("COUNT: " + sdf.format(new Date()));
				
	}
	*/
}
