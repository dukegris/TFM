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
		AppNames.APP_TRAIN_COMMAND )
public class TrainCommand {

    @ShellMethod("Genera un modelo NER a partir de ficheros CONLL usando un modelo BertEbedding.")
	private int train(
			@ShellOption({"-t", "--trainfile"})		String trainFileName, 
			@ShellOption({"-v", "--testfile"})		String testFileName, 
			@ShellOption({"-o", "--outdir"})		String nerModelDirectoryName, 
			@ShellOption({"-b", "--bert"})			String bertModelDirectoryName, 
			@ShellOption({"-tf", "--tensorflow"})	String nerTensorFlowGraphDirectory) {

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
