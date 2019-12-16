package es.rcs.tfm.main.components;

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

@ShellComponent( value =  AppNames.APP_CONLL_COMMAND )
public class ConllCommand {

    @ShellMethod("Genera ficheros CONLL a partir de textos en PubTaor o BioC.")
	private int generate(
			@ShellOption({"-i", "--infile"})	String infile, 
			@ShellOption({"-o", "--outfile"})	String outfile, 
			@ShellOption({"-t", "--type (" + 
					AppNames.BIOC + " o " + 
					AppNames.PUBTATOR + ")"})	String type, 
			@ShellOption({"-b", "--bert"})		String bertmodel,
			@ShellOption({"-n", "--ner"}) 		String nermodel) {

		int result = AppNames.OK;
		
		File f = Paths.get(infile).toFile();
		if (!f.exists() || !f.isFile()) result = AppNames.GENERATE_INVALID_FILE;
		if (!(AppNames.BIOC.equals(type.toUpperCase()) || AppNames.PUBTATOR.equals(type.toUpperCase()))) result = AppNames.GENERATE_INVALID_TYPE;
		
		if (result == AppNames.OK) {
			
			System.out.println(infile);
			System.out.println(outfile);
			System.out.println(type);
			System.out.println(bertmodel);
			System.out.println(nermodel);

			try {
				
				if (AppNames.BIOC.equals(type.toUpperCase())) {
					train.prepareCoNLL2003DataForTrainingFromBioc(spark, infile, outfile, bertmodel, nermodel, false);
				} else if (AppNames.PUBTATOR.equals(type.toUpperCase())) {
					train.prepareCoNLL2003DataForTrainingFromPubtator(spark, infile, outfile, bertmodel, nermodel, false);
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
