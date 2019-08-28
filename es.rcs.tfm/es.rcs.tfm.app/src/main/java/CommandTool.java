import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.spark.sql.SparkSession;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.main.config.DatabaseConfig;
import es.rcs.tfm.main.config.SolrConfig;
import es.rcs.tfm.main.config.SparkConfig;
import es.rcs.tfm.nlp.NlpNames;
import es.rcs.tfm.solr.IndexNames;
import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.services.TrainService;
import es.rcs.tfm.xml.XmlNames;

@ComponentScan(basePackages = {
		XmlNames.XML_CONFIG_PKG,
		DbNames.DB_CONFIG_PKG,
		IndexNames.IDX_CONFIG_PKG,
		NlpNames.NLP_CONFIG_PKG,
		SrvNames.SRV_CONFIG_PKG})
@ImportAutoConfiguration(classes = { 
		DatabaseConfig.class,
		SolrConfig.class,
		SparkConfig.class })
@Configuration(
		AppNames.CMD_CONFIG)
public class CommandTool {

	private static final String BIOC =						"BIOC";
	private static final String PUBTATOR =					"PUBTATOR";
	
	private static final int OK =							0;
	
	private static final int GENERATE_INVALID_FILE =		1;
	private static final int GENERATE_INVALID_TYPE =		2;
	private static final int GENERATE_START_FAILED =		99;
	
	private static final int TRAIN_INVALID_TRAIN_FILE =		3;
	private static final int TRAIN_INVALID_TEST_FILE =		4;
	private static final int TRAIN_INVALID_DIRECTORY =		5;
	private static final int TRAIN_START_FAILED =			98;

	private static Class<CommandTool> applicationClass = CommandTool.class;
	private static AnnotationConfigApplicationContext context;

	public static void main(String[] args) {

		CommandTool tool = new CommandTool();
		int result = 0;
		
		Options options = new Options();

		Option help = new Option("h", "help", false, "Este mensaje");

		Option generate = Option.
				builder("g").
				longOpt("generate").
				desc(	"Genera un fichero <outfile> CONLL2003 " + 
						"a partir del fichero <infile> de tipo " + 
						"<type> PUBTATOR o BIOC." +
						"betmodel y nermodel son opcionales").
				hasArg(true).
				numberOfArgs(3).
				argName("infile> <outfile> <type> <bertmodel> <nermodel").
				build();

		Option train = Option.
				builder("t").
				longOpt("train").
				desc(	"Genera un modelo NER en <outdir> " +
						"a partir del fichero de entrenamiento <trainfile> " +
						"evaluandolo contra <TESTFILE>." +
						"betmodel y nermodel son opcionales").
				hasArg(true).
				numberOfArgs(3).
				argName("trainfile> <testfile> <outdir> <bertmodel> <nermodel").
				build();

		options.addOption(help);
		options.addOption(generate);
		options.addOption(train);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
			if (cmd.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("CommandTool", options);
			} else if (cmd.hasOption("g")) {
				String[] data = cmd.getOptionValues("g");
				if (data.length>2) {
					String infile = data[0];
					String outfile = data[1];
					String type = data[2];
					String bertmodel = "";
					String nermodel = "";
					if (data.length>4) {
						bertmodel = data[3];
						nermodel = data[4];
					}
					result = tool.generate(infile, outfile, type, bertmodel, nermodel);
				}
			} else if (cmd.hasOption("t")) {
				String[] data = cmd.getOptionValues("t");
				if (data.length>2) {
					String trainfile = data[0];
					String testfile = data[1];
					String outdir = data[2];
					String bertmodel = "";
					if (data.length>3) {
						bertmodel = data[3];
					}
					result = tool.train(trainfile, testfile, outdir, bertmodel);
				}
			} else {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("CommandTool", options);
			}
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("CommandTool", options);
		}
		
		System.exit(result);
		
	}

	private int train(String trainfile, String testfile, String outdir, String bertmodel) {

		int result = OK;

		File f = Paths.get(trainfile).toFile();
		if (!f.exists() || !f.isFile()) result = TRAIN_INVALID_TRAIN_FILE;
		f = Paths.get(testfile).toFile();
		if (!f.exists() || !f.isFile()) result = TRAIN_INVALID_TEST_FILE;
		f = Paths.get(outdir).toFile();
		if (f.exists() && !f.isDirectory()) result = TRAIN_INVALID_DIRECTORY;
		
		if (result == OK) {
			
			System.out.println(trainfile);
			System.out.println(testfile);
			System.out.println(outdir);

			try {
				
				context = new AnnotationConfigApplicationContext();
				context.register(applicationClass);
				context.refresh();
				
				SparkSession spark = context.getBean(SrvNames.SPARK_SESSION_TRAIN, SparkSession.class);
				TrainService train = context.getBean(SrvNames.TRAINING_SRVC, TrainService.class);
				
				train.trainModel(spark, trainfile, testfile, outdir, bertmodel);

			} catch (Exception ex) {
				result = TRAIN_START_FAILED;
				System.out.println("FAILED " + ex);
			}

		}

		return result;
		
	}

	private int generate(String infile, String outfile, String type, String bertmodel, String nermodel) {

		int result = OK;
		
		File f = Paths.get(infile).toFile();
		if (!f.exists() || !f.isFile()) result = GENERATE_INVALID_FILE;
		if (!(BIOC.equals(type.toUpperCase()) || PUBTATOR.equals(type.toUpperCase()))) result = GENERATE_INVALID_TYPE;
		
		if (result == OK) {
			
			System.out.println(infile);
			System.out.println(outfile);
			System.out.println(type);
			System.out.println(bertmodel);
			System.out.println(nermodel);
			
			try {
				
				context = new AnnotationConfigApplicationContext();
				context.register(applicationClass);
				context.refresh();
				
				SparkSession spark = context.getBean(SrvNames.SPARK_SESSION_TRAIN, SparkSession.class);
				TrainService train = context.getBean(SrvNames.TRAINING_SRVC, TrainService.class);

				if (BIOC.equals(type.toUpperCase())) {
					train.prepareDataForTrainingFromBioc(spark, infile, outfile, bertmodel, nermodel);
				} else if (PUBTATOR.equals(type.toUpperCase())) {
					train.prepareDataForTrainingFromPubtator(spark, infile, outfile, bertmodel, nermodel);
				}

			} catch (Exception ex) {
				result = GENERATE_START_FAILED;
				System.out.println("FAILED " + ex);
			}

		}

		return result;
		
	}

}
