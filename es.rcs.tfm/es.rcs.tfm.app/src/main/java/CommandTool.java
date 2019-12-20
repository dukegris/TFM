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

import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.main.config.SparkConfig;
import es.rcs.tfm.nlp.NlpNames;
import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.services.train.TrainService;
import es.rcs.tfm.xml.XmlNames;

@ComponentScan(basePackages = {
		XmlNames.XML_CONFIG_PKG,
		NlpNames.NLP_CONFIG_PKG,
		SrvNames.SRV_TRAIN_SERVICES_PKG})
@ImportAutoConfiguration(classes = { 
		SparkConfig.class })
@Configuration(
		AppNames.CMD_CONFIG)
public class CommandTool {

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
				desc(	"Genera un fichero <outfile> CoNLL2003 " + 
						"a partir del fichero <infile> de tipo " + 
						"<type> PUBTATOR o BIOC." +
						"betmodel y nermodel son opcionales").
				hasArg(true).
				numberOfArgs(5).
				optionalArg(true).
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
				numberOfArgs(5).
				optionalArg(true).
				argName("trainfile> <testfile> <outdir> <bertmodel> <bertmodel> <tf_graph model").
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
					String posmodel = "";
					String bertmodel = "";
					String nermodel = "";
					if (data.length>3) {
						posmodel = data[3];
					}
					if (data.length>4) {
						bertmodel = data[4];
					}
					if (data.length>5) {
						nermodel = data[5];
					}
					result = tool.generate(infile, outfile, type, posmodel, bertmodel, nermodel);
				}
			} else if (cmd.hasOption("t")) {
				String[] data = cmd.getOptionValues("t");
				if (data.length>2) {
					String trainfile = data[0];
					String testfile = data[1];
					String outdir = data[2];
					String posmodel = "";
					String bertmodel = "";
					String tfmodel = "";
					if (data.length>3) {
						posmodel = data[3];
					}
					if (data.length>4) {
						bertmodel = data[4];
					}
					if (data.length>5) {
						tfmodel = data[5];
					}
					result = tool.train(trainfile, testfile, outdir, posmodel, bertmodel, tfmodel);
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

	private int train(String trainfile, String testfile, String outdir, String posmodel, String bertmodel, String tfmodel) {

		int result = AppNames.OK;

		File f = Paths.get(trainfile).toFile();
		if (!f.exists() || !f.isFile()) result = AppNames.TRAIN_INVALID_TRAIN_FILE;
		f = Paths.get(testfile).toFile();
		if (!f.exists() || !f.isFile()) result = AppNames.TRAIN_INVALID_TEST_FILE;
		f = Paths.get(outdir).toFile();
		if (f.exists() && !f.isDirectory()) result = AppNames.TRAIN_INVALID_DIRECTORY;
		
		if (result == AppNames.OK) {
			
			System.out.println(trainfile);
			System.out.println(testfile);
			System.out.println(outdir);
			System.out.println(posmodel);
			System.out.println(bertmodel);
			System.out.println(tfmodel);

			try {
				
				context = new AnnotationConfigApplicationContext();
				context.register(applicationClass);
				context.refresh();
				
				SparkSession spark = context.getBean(SrvNames.SPARK_SESSION_TRAIN, SparkSession.class);
				TrainService train = context.getBean(SrvNames.TRAINING_SRVC, TrainService.class);
				
				train.trainModel(spark, trainfile, testfile, outdir, posmodel, bertmodel, tfmodel);

			} catch (Exception ex) {
				result = AppNames.TRAIN_START_FAILED;
				System.out.println("FAILED " + ex);
			}

		}

		return result;
		
	}

	private int generate(String infile, String outfile, String type, String posmodel, String bertmodel, String nermodel) {

		int result = AppNames.OK;
		
		File f = Paths.get(infile).toFile();
		if (!f.exists() || !f.isFile()) result = AppNames.GENERATE_INVALID_FILE;
		if (!(AppNames.BIOC.equals(type.toUpperCase()) || AppNames.PUBTATOR.equals(type.toUpperCase()))) result = AppNames.GENERATE_INVALID_TYPE;
		
		if (result == AppNames.OK) {
			
			System.out.println(infile);
			System.out.println(outfile);
			System.out.println(type);
			System.out.println(posmodel);
			System.out.println(bertmodel);
			System.out.println(nermodel);
			
			try {
				
				context = new AnnotationConfigApplicationContext();
				context.register(applicationClass);
				context.refresh();
				
				SparkSession spark = context.getBean(SrvNames.SPARK_SESSION_TRAIN, SparkSession.class);
				TrainService train = context.getBean(SrvNames.TRAINING_SRVC, TrainService.class);

				if (AppNames.BIOC.equals(type.toUpperCase())) {
					train.prepareCoNLL2003DataForTrainingFromBioc(spark, infile, outfile, posmodel, bertmodel, nermodel, false);
				} else if (AppNames.PUBTATOR.equals(type.toUpperCase())) {
					train.prepareCoNLL2003DataForTrainingFromPubtator(spark, infile, outfile, posmodel, bertmodel, nermodel, false);
				}

			} catch (Exception ex) {
				result = AppNames.GENERATE_START_FAILED;
				System.out.println("FAILED " + ex);
			}

		}

		return result;
		
	}

}
