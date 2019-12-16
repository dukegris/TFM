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

@ShellComponent( value =  AppNames.APP_TRAIN_COMMAND )
public class TrainCommand {

    @ShellMethod("Genera un modelo NER a partir de ficheros CONLL usando un modelo BertEbedding.")
	private int train(
			@ShellOption({"-t", "--trainfile"})		String trainfile, 
			@ShellOption({"-v", "--testfile"})		String testfile, 
			@ShellOption({"-o", "--outdir"})		String outdir, 
			@ShellOption({"-p", "--pos"})			String posmodel, 
			@ShellOption({"-b", "--bert"})			String bertmodel, 
			@ShellOption({"-tf", "--tensorflow"})	String tensorflowmodel) {

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
			System.out.println(bertmodel);
			System.out.println(tensorflowmodel);

			try {
				
				train.trainModel(spark, trainfile, testfile, outdir, posmodel, bertmodel, tensorflowmodel);

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
