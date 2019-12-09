package es.rcs.tfm.srv.repository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.rcs.tfm.nlp.service.ConllWritter;
import es.rcs.tfm.nlp.service.NerPrepare;
import es.rcs.tfm.nlp.service.NerTrain;
import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.model.ArticuloTextoAnotado;
import es.rcs.tfm.srv.setup.ArticleProcessor;

public class TrainRepository {
	
	private static final Logger LOG = LoggerFactory.getLogger(TrainRepository.class);
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-hhmmss");
	private static final String HADOOP_FILE_PREFIX = "file:///";
	
	/**
	 * Construye un dataset a partir de los datos de entrenamiento en el modelo intermedio
	 * @param list lista de beans del modelo con los documentos de entrenamiento
	 * @return lista de filas con los documentos de entrenamiento
	 */
	public static final List<Row> generateDS(List<Articulo> list) {
		
		if ((list == null) || (list.isEmpty())) return null;

		List<Row> result = new ArrayList<>();
		try {
		
			list.forEach(doc -> {
				if ((doc != null) && (doc.getBlocks() != null)) {
					doc.getBlocks().forEach((b) -> {
						List<String> notes = null;
						if ((b != null) && (b.getNotes() != null) && (!b.getNotes().isEmpty())) {
							notes = b.getNotes().
									values().
									stream().
									filter(n -> (n != null)).
									flatMap(n -> n.getPos().
											stream().
											map(p -> {
												System.out.println ("NOTE :" + 
														n.getText().equals(b.getText().substring(p.getOffset(), p.getOffset() + p.getLength())) + 
														" -> \"" + 
														b.getText().substring(p.getOffset(), p.getOffset()+ p.getLength()) + 
														"\" is \"" + 
														n.getText() + 
														"\"");
												return String.format(
														"[%d, %d, %s, %s]", 
														p.getOffset(), 
														p.getEnd(), 
														n.getText(), 
														n.getType());
											}))
									.collect(Collectors.toList());
						}
						if (notes == null) notes = new ArrayList<String>();
						result.add(RowFactory.create(doc.getPmid(), b.getType(), b.getText(), notes));
					});
				}
			});
			
		} catch (Exception ex) {
			LOG.warn("generateDS FAIL " + ex.toString());
		}
		return result;
		
	}

	/**
	 * Genera una estructura compatible para Spark del conjunto de entrenamiento
	 * @return La estructura para el Dataset
	 */
	public static final StructType  generateStructType() {
		
	    StructField[] structFields = new StructField[]{
	            new StructField("id", DataTypes.StringType, true, Metadata.empty()),
	            new StructField("type", DataTypes.StringType, true, Metadata.empty()),
	            new StructField("text", DataTypes.StringType, true, Metadata.empty()),
	            new StructField("notes", DataTypes.createArrayType(DataTypes.StringType, true), true, Metadata.empty())
	    };

	    return new StructType(structFields);

	}
	
	/**
	 * @param spark Sesion de Spark donde se ejecutar� la preparaci�n de datos
	 * @param trainFilename
	 * @param testFilename
	 * @param resultsFilename
	 * @param resultsDirectory
	 * @param posModelDirectory Directorio con el modelo utilizado para el marcado de palabras
	 * @param bertModelDirectory Directorio con el modelo utilizado para el marcado de palabras
	 * @param targetModelDirectory Directorio de salida de los fichero CONLL
	 * @param maxSentence Maximo tama�o de una frase (defecto 512, suele ser 128)
	 * @param dimension Maximo n�mero de dimensiones (defecto 1024, suele ser 768)
	 * @return
	 */
	public static final boolean trainFromConll(
			SparkSession spark,
			String trainFilename,
			String testFilename,
			String resultsFilename,
			String resultsDirectory,
			String posModelDirectory,
			String bertModelDirectory,
			String targetModelDirectory,
			Integer maxSentence,
			Integer dimension,
			Integer batchSize,
			Boolean caseSensitive) {

		if (spark == null) return false;
		if (StringUtils.isBlank(resultsDirectory)) return false;
		if (StringUtils.isBlank(resultsFilename)) return false;
		if (StringUtils.isBlank(targetModelDirectory)) return false;
		if (StringUtils.isBlank(trainFilename)) return false;
		if (StringUtils.isBlank(testFilename)) return false;
		if (StringUtils.isBlank(posModelDirectory)) return false;
		if (StringUtils.isBlank(bertModelDirectory)) return false;

		Path results = Paths.get(resultsDirectory);
		if (results.toFile() == null) return false;
		if (results.toFile().exists() && (!results.toFile().isDirectory())) return false;

		//Path target = Paths.get(targetModelDirectory);

		Path posModel = Paths.get(posModelDirectory);
		if (posModel.toFile() == null) return false;
		if (!posModel.toFile().exists()) return false;
		if (!posModel.toFile().isDirectory()) return false;

		Path bertModel = Paths.get(bertModelDirectory);
		if (bertModel.toFile() == null) return false;
		if (!bertModel.toFile().exists()) return false;
		if (!bertModel.toFile().isDirectory()) return false;

		Path train = Paths.get(trainFilename);
		if (train.toFile() == null) return false;
		if (!train.toFile().exists()) return false;
		if (!train.toFile().isFile()) return false;

		Path test = Paths.get(testFilename);
		if (test.toFile() == null) return false;
		if (!test.toFile().exists()) return false;
		if (!test.toFile().isFile()) return false;

		if (maxSentence == null) maxSentence = 512;
		if (dimension == null) dimension = 1024;
		if (batchSize == null) batchSize = 32;
		if (caseSensitive == null) caseSensitive = false;
		
		boolean resultado = true;
		try {
			
			String ini = SIMPLE_DATE_FORMAT.format(new Date());
			
			NerTrain nerTrainer = new NerTrain(
					spark.sparkContext(),
					spark,
					HADOOP_FILE_PREFIX + posModelDirectory,
					HADOOP_FILE_PREFIX + bertModelDirectory,
					maxSentence,
					dimension,
					batchSize,
					caseSensitive);
			
			nerTrainer.
				saveNerModel (
					nerTrainer.
						measureNerTraining(
							trainFilename,
							testFilename,
							resultsFilename,
							resultsDirectory),
						HADOOP_FILE_PREFIX + targetModelDirectory);
			
			LOG.info(
					"\r\nTRAIN TIME for [" + trainFilename + "] "  +
					"\r\n\tINI:" + ini +
 					"\r\n\tEND:" + SIMPLE_DATE_FORMAT.format(new Date()) );

		} catch (Exception ex) {
			resultado = false;
			LOG.warn("TRAIN FAIL " + ex.toString());
		}

		return resultado;
		
	}

	/**
	 * Construye un fichero conll en un directorio parquet con el conjunto de documentos de entrenamiento
	 * aplicandoles la localizaci�n de entidades del modelo 
	 * @param spark Sesion de Spark donde se ejecutar� la preparaci�n de datos
	 * @param processor Generador de anotaciones
	 * @param resultsDirectory Directorio donde se deja el modelo con los resultados
	 * @param posModelDirectory Directorio con el modelo utilizado para el marcado de palabras
	 * @param bertModelDirectory Directorio con el modelo utilizado para el marcado de palabras
	 * @param nerModelDirectory Directorio con el modelo NER utilizado para la localizaci�n de entidades gen�ricas
	 * @param targetFilename Directorio parquet de salida de la preparaci�n de datos
	 * @param maxSentence Maximo tama�o de una frase (defecto 512, suele ser 128)
	 * @param dimension Maximo n�mero de dimensiones (defecto 1024, suele ser 768)
	 * @return
	 */
	public static final boolean getConllFrom(
			SparkSession spark,
			ArticleProcessor processor,
			String resultsDirectory,
			String posModelDirectory,
			String bertModelDirectory,
			String nerModelDirectory,
			String targetFilename,
			Integer maxSentence,
			Integer dimension,
			Integer batchSize,
			Boolean caseSensitive) {

		if (spark == null) return false;
		if (processor == null) return false;
		if (StringUtils.isBlank(resultsDirectory)) return false;
		if (StringUtils.isBlank(targetFilename)) return false;
		if (StringUtils.isBlank(posModelDirectory)) return false;
		if (StringUtils.isBlank(bertModelDirectory)) return false;
		if (StringUtils.isBlank(nerModelDirectory)) return false;

		Path posModel = Paths.get(posModelDirectory);
		if (posModel.toFile() == null) return false;
		if (!posModel.toFile().exists()) return false;
		if (!posModel.toFile().isDirectory()) return false;

		Path bertModel = Paths.get(bertModelDirectory);
		if (bertModel.toFile() == null) return false;
		if (!bertModel.toFile().exists()) return false;
		if (!bertModel.toFile().isDirectory()) return false;

		Path nerBertModel = Paths.get(bertModelDirectory);
		if (nerBertModel.toFile() == null) return false;
		if (!nerBertModel.toFile().exists()) return false;
		if (!nerBertModel.toFile().isDirectory()) return false;

		Path target = Paths.get(targetFilename);
		if (target.toFile() == null) return false;
		//if (!target.toFile().exists()) return false;
		//if (!target.toFile().isFile()) return false;

		if (maxSentence == null) maxSentence = 512;
		if (dimension == null) dimension = 1024;
		if (batchSize == null) batchSize = 32;
		if (caseSensitive == null) caseSensitive = false;
		
		boolean resultado = true;
		try {

			String ini = SIMPLE_DATE_FORMAT.format(new Date());
			
			Stream<Articulo> stream = StreamSupport.stream(
					Spliterators.spliteratorUnknownSize(
							processor, 
							Spliterator.DISTINCT), 
					false);
			List<Articulo> data = stream.collect(Collectors.toList());
			
			if ((data == null) || (data.size() == 0)) {
				
				resultado = false;
				
			} else {

				List<Row> rows = TrainRepository.generateDS(data);
				StructType structType = TrainRepository.generateStructType();
				
				String build = SIMPLE_DATE_FORMAT.format(new Date());

				NerPrepare generator = new NerPrepare(
						spark.sparkContext(),
						spark,
						HADOOP_FILE_PREFIX + posModelDirectory,
						HADOOP_FILE_PREFIX + bertModelDirectory,
						HADOOP_FILE_PREFIX + nerModelDirectory,
						maxSentence,
						dimension,
						batchSize,
						caseSensitive);

				String prepare = SIMPLE_DATE_FORMAT.format(new Date());

				ConllWritter writter = new ConllWritter(spark);
				
				Dataset<Row> ds = generator.
						execute(
								rows, 
								structType, 
								HADOOP_FILE_PREFIX + FilenameUtils.separatorsToUnix(resultsDirectory));
				
				@SuppressWarnings("unchecked")
				Dataset<Row> conllDs = writter.
						generateConll(
								ds);
				
				double tasa = writter.
						saveConll(
								conllDs, 
								targetFilename);

				LOG.info(
						"\r\nCONLL2003 TIME for [" + rows.size() + "] documents. PRECISSION = " + tasa * 100 +
						"\r\n\tINI:" + ini +
						"\r\n\tDAT:" + build +
						"\r\n\tPRE:" + prepare +
						"\r\n\tEND:" + SIMPLE_DATE_FORMAT.format(new Date()) );

			}
		} catch (Exception ex) {
			resultado = false;
			LOG.warn("CONLL2003 FAIL " + ex.toString());
		}

		return resultado;
		
	}
	    
}
