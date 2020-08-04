package es.rcs.tfm.main.shell;

import java.io.File;
import java.nio.file.Paths;

import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import es.rcs.tfm.main.AppNames;
import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.services.train.TrainService;

@ShellComponent( value =  
		AppNames.APP_CONLL_COMMAND )
public class ConllCommand {

    @ShellMethod("Genera ficheros CONLL a partir de textos en PubTaor o BioC.")
	private int generate(
			@ShellOption({"-i", "--infile"})	String inFileName, 
			@ShellOption({"-o", "--outfile"})	String outFileName, 
			@ShellOption({"-t", "--type (" + 
					AppNames.BIOC + " o " + 
					AppNames.PUBTATOR + ")"})	String type, 
			@ShellOption({"-p", "--pos"})		String posModelDirectoryName, 
			@ShellOption({"-b", "--bert"})		String bertModelDirectoryName,
			@ShellOption({"-n", "--ner"}) 		String nerModelDirectoryName) {

		int result = AppNames.OK;
		
		File f = Paths.get(inFileName).toFile();
		if (!f.exists() || !f.isFile()) result = AppNames.GENERATE_INVALID_FILE;
		if (!(AppNames.BIOC.equals(type.toUpperCase()) || AppNames.PUBTATOR.equals(type.toUpperCase()))) result = AppNames.GENERATE_INVALID_TYPE;
		
		if (result == AppNames.OK) {
			
			System.out.println(inFileName);
			System.out.println(outFileName);
			System.out.println(type);
			System.out.println(posModelDirectoryName);
			System.out.println(bertModelDirectoryName);
			System.out.println(nerModelDirectoryName);

			try {
				
				if (AppNames.BIOC.equals(type.toUpperCase())) {
					train.prepareCoNLL2003DataForTrainingFromBioc(spark, inFileName, outFileName, posModelDirectoryName, bertModelDirectoryName, nerModelDirectoryName, false);
				} else if (AppNames.PUBTATOR.equals(type.toUpperCase())) {
					train.prepareCoNLL2003DataForTrainingFromPubtator(spark, inFileName, outFileName, posModelDirectoryName, bertModelDirectoryName, nerModelDirectoryName, false);
				}

			} catch (Exception ex) {
				result = AppNames.TRAIN_START_FAILED;
				System.out.println("FAILED " + ex);
			}

		}

		return result;
		
	}

	@Autowired
	@Qualifier(value = SrvNames.SPARK_SESSION_TRAIN )
	public SparkSession spark;

	@Autowired
	@Qualifier(value = SrvNames.TRAINING_SRVC)
	private TrainService train;
	
}
