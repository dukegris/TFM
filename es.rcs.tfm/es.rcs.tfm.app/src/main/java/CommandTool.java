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
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.main.boot.CommandConfig;
import es.rcs.tfm.main.boot.DownloaderConfig;
import es.rcs.tfm.main.boot.WebserverConfig;
import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.services.train.TrainService;

public class CommandTool {

	public static void main(String[] args) {

		CommandTool tool = new CommandTool();
		int result = 0;
		
		Options options = new Options();

		Option help = new Option("h", "help", false, "Este mensaje");

		Option downloaderDaemon = Option.
				builder("d").
				longOpt("downloader").
				desc(	"Inicia el proceso de descarga de ficheros diario").
				hasArg(false).
				build();

		Option webServer = Option.
				builder("w").
				longOpt("web").
				desc(	"Inicia el servidor web de contenidos").
				hasArg(false).
				build();

		Option generate = Option.
				builder("g").
				longOpt("generate").
				desc(	"Genera un fichero <outfile> CoNLL2003 " + 
						"a partir del fichero <infile> de tipo " + 
						"<type> PUBTATOR o BIOC." +
						"betmodel y nermodel son opcionales").
				hasArg(true).
				numberOfArgs(6).
				optionalArg(true).
				argName("infile> <outfile> <type> <posmodel> <bertmodel> <nermodel").
				build();

		Option train = Option.
				builder("t").
				longOpt("train").
				desc(	"Genera un modelo NER en <outdir> " +
						"a partir del fichero de entrenamiento <trainfile> " +
						"evaluandolo contra <TESTFILE>." +
						"betmodel y nermodel son opcionales").
				hasArg(true).
				numberOfArgs(6).
				optionalArg(true).
				argName("trainfile> <testfile> <outdir> <bertmodel> <bertmodel> <tf_graph model").
				build();

		options.addOption(help);
		options.addOption(generate);
		options.addOption(train);
		options.addOption(downloaderDaemon);
		options.addOption(webServer);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
			if (cmd.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("CommandTool", options);
			} else if (cmd.hasOption("d")) {
				result = tool.downloader(args);
			} else if (cmd.hasOption("w")) {
				result = tool.webserver(args);
			} else if (cmd.hasOption("g")) {
				String[] data = cmd.getOptionValues("g");
				if (data.length>2) {
					String inFileName = data[0];
					String outFileName = data[1];
					String type = data[2];
					String posModelDirectoryName = "";
					String bertModelDirectoryName = "";
					String nerModelDirectoryName = "";
					if (data.length>3) {
						posModelDirectoryName = data[3];
					}
					if (data.length>4) {
						bertModelDirectoryName = data[4];
					}
					if (data.length>5) {
						nerModelDirectoryName = data[5];
					}
					result = tool.generate(inFileName, outFileName, type, posModelDirectoryName, bertModelDirectoryName, nerModelDirectoryName);
				}
			} else if (cmd.hasOption("t")) {
				String[] data = cmd.getOptionValues("t");
				if (data.length>2) {
					String trainFileName = data[0];
					String testFileName = data[1];
					String nerModelDirectoryName = data[2];
					String bertModelDirectoryName = "";
					String nerTensorFlowGraphDirectory = "";
					if (data.length>3) {
						bertModelDirectoryName = data[3];
					}
					if (data.length>4) {
						nerTensorFlowGraphDirectory = data[4];
					}
					result = tool.train(trainFileName, testFileName, nerModelDirectoryName, bertModelDirectoryName, nerTensorFlowGraphDirectory);
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

	private int webserver(String[] args) {

		int result = AppNames.OK;

		try {
			
			ConfigurableApplicationContext context = new SpringApplicationBuilder(WebserverConfig.class).
					logStartupInfo(true).
					build().
					run(args);
			
			Thread.currentThread().wait();

		} catch (Exception ex) {
			result = AppNames.WEBSERVER_START_FAILED;
			System.out.println("FAILED " + ex);
		}
	
		return result;

	}

	private int downloader(String[] args) {

		int result = AppNames.OK;

		try {
			
			ConfigurableApplicationContext context = new SpringApplicationBuilder(DownloaderConfig.class).
					web(WebApplicationType.NONE).
					logStartupInfo(true).
					build().
					run(args);
			
			Thread.currentThread().wait();

		} catch (Exception ex) {
			result = AppNames.DOWNLOADER_START_FAILED;
			System.out.println("FAILED " + ex);
		}
	
		return result;

	}

	private int train(String trainFileName, String testFileName, String nerModelDirectoryName, String bertModelDirectoryName, String nerTensorFlowGraphDirectory) {

		int result = AppNames.OK;

		File f = Paths.get(trainFileName).toFile();
		if (!f.exists() || !f.isFile()) result = AppNames.TRAIN_INVALID_TRAIN_FILE;
		f = Paths.get(testFileName).toFile();
		if (!f.exists() || !f.isFile()) result = AppNames.TRAIN_INVALID_TEST_FILE;
		f = Paths.get(nerModelDirectoryName).toFile();
		if (f.exists() && !f.isDirectory()) result = AppNames.TRAIN_INVALID_DIRECTORY;
		
		if (result == AppNames.OK) {
			
			System.out.println(trainFileName);
			System.out.println(testFileName);
			System.out.println(nerModelDirectoryName);
			System.out.println(bertModelDirectoryName);
			System.out.println(nerTensorFlowGraphDirectory);

			try {
				
				AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
				context.register(CommandConfig.class);
				context.refresh();
				
				SparkSession spark = context.getBean(SrvNames.SPARK_SESSION_TRAIN, SparkSession.class);
				TrainService train = context.getBean(SrvNames.TRAINING_SRVC, TrainService.class);
				
				train.trainModel(
						spark, 
						trainFileName, 
						testFileName, 
						bertModelDirectoryName, 
						null, 
						null, 
						null, 
						null, 
						null,
						nerModelDirectoryName, 
						null, 
						null, 
						null, 
						null, 
						null, 
						null, 
						null, 
						null);
				
				context.close();

			} catch (Exception ex) {
				result = AppNames.TRAIN_START_FAILED;
				System.out.println("FAILED " + ex);
			}

		}

		return result;
		
	}

	private int generate(String inFileName, String outFileName, String tmvarType, String posModelDirectoryName, String bertModelDirectoryName, String nerModelDirectoryName) {

		int result = AppNames.OK;
		
		if (!(AppNames.BIOC.equals(tmvarType.toUpperCase()) || AppNames.PUBTATOR.equals(tmvarType.toUpperCase()))) {
			System.out.println(String.format("El tipo debe ser: %s o %s", AppNames.BIOC, AppNames.PUBTATOR));
			result = AppNames.GENERATE_INVALID_TYPE;
		}
		
		if (result == AppNames.OK) {
			
			System.out.println(inFileName);
			System.out.println(tmvarType);
			System.out.println(outFileName);
			System.out.println(posModelDirectoryName);
			System.out.println(bertModelDirectoryName);
			System.out.println(nerModelDirectoryName);
			
			try {
				
				AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
				context.register(CommandConfig.class);
				context.refresh();
				
				TrainService train = context.getBean(SrvNames.TRAINING_SRVC, TrainService.class);

				if (!train.testPrepareConll(inFileName, outFileName, posModelDirectoryName, bertModelDirectoryName, nerModelDirectoryName)) {

					System.out.println(String.format("Alguno de los ficheros no existe"));
					result = AppNames.GENERATE_INVALID_TYPE;
					
				} else {

					SparkSession spark = context.getBean(SrvNames.SPARK_SESSION_TRAIN, SparkSession.class);
					if (AppNames.BIOC.equals(tmvarType.toUpperCase())) {
						train.prepareCoNLL2003DataForTrainingFromBioc(spark, inFileName, outFileName, posModelDirectoryName, bertModelDirectoryName, nerModelDirectoryName, false);
					} else if (AppNames.PUBTATOR.equals(tmvarType.toUpperCase())) {
						train.prepareCoNLL2003DataForTrainingFromPubtator(spark, inFileName, outFileName, posModelDirectoryName, bertModelDirectoryName, nerModelDirectoryName, false);
					}
					
				}
				
				context.close();

			} catch (Exception ex) {
				result = AppNames.GENERATE_START_FAILED;
				System.out.println("FAILED " + ex);
			}

		}

		return result;
		
	}

}
