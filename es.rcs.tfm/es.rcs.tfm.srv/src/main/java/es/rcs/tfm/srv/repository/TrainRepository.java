package es.rcs.tfm.srv.repository;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FilenameUtils;
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

import es.rcs.tfm.nlp.service.CoNLL2003Generator;
import es.rcs.tfm.nlp.service.NerPipeline;
import es.rcs.tfm.nlp.service.NerTrain;
import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.setup.ArticleProcessor;

public class TrainRepository {
	
	private static final Logger LOG = LoggerFactory.getLogger(TrainRepository.class);
	private static final String SIMPLE_DATE_FORMAT = "yyyyMMdd-hhmmss";
	private static final String HADOOP_FILE_PREFIX = "file://";
	
	private static Map<Integer, NerPipeline> PIPELINES = new HashMap<Integer, NerPipeline>();

	private static Integer getHash(
			File posModelDirectory,
			File bertModelDirectory,
			File nerModelDirectory,
			Integer bertMaxSentenceLength,
			Integer bertDimension,
			Integer bertBatchSize,
			Boolean bertCaseSensitive,
			Integer bertPoolingLayer) {
		int hash = 17;
        hash = hash * 23 + ((posModelDirectory == null) ? 0 : posModelDirectory.hashCode());
        hash = hash * 23 + ((bertModelDirectory == null) ? 0 : bertModelDirectory.hashCode());
        hash = hash * 23 + ((nerModelDirectory == null) ? 0 : nerModelDirectory.hashCode());
        hash = hash * 23 + ((bertMaxSentenceLength == null) ? 0 : bertMaxSentenceLength.hashCode());
        hash = hash * 23 + ((bertDimension == null) ? 0 : bertDimension.hashCode());
        hash = hash * 23 + ((bertBatchSize == null) ? 0 : bertBatchSize.hashCode());
        hash = hash * 23 + ((bertCaseSensitive == null) ? 0 : bertCaseSensitive.hashCode());
        hash = hash * 23 + ((bertPoolingLayer == null) ? 0 : bertPoolingLayer.hashCode());
        return hash;
	}

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
												/*
												System.out.println ("NOTE :" + 
														n.getText().equals(b.getText().substring(p.getOffset(), p.getOffset() + p.getLength())) + 
														" -> \"" + 
														b.getText().substring(p.getOffset(), p.getOffset()+ p.getLength()) + 
														"\" is \"" + 
														n.getText() + 
														"\"");
												 */
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
	 * Construye un fichero conll en un directorio parquet con el conjunto de documentos de entrenamiento
	 * aplicandoles la localizaci�n de entidades del modelo 
	 * @param spark Sesion de Spark donde se ejecutar� la preparaci�n de datos
	 * @param processor Generador de anotaciones
	 * @param posModelDirectory Directorio con el modelo utilizado para el marcado de palabras
	 * @param bertModelDirectory Directorio con el modelo utilizado para el marcado de palabras
	 * @param nerModelDirectory Directorio con el modelo NER utilizado para la localizaci�n de entidades gen�ricas
	 * @param outFile Directorio parquet de salida de la preparaci�n de datos
	 * @param mantainNerFromGenericModel Mantener los IOB obtenidos del modelo gen�rico de NER
	 * @param bertMaxSentenceLength Maximo tama�o de una frase (defecto 512, suele ser 128)
	 * @param bertDimension Maximo n�mero de bertDimensiones (defecto 1024, suele ser 768)
	 * @return
	 */
	public static final boolean getConllFrom(
			SparkSession spark,
			ArticleProcessor processor,
			File inFile,
			File posModelDirectory,
			File bertModelDirectory,
			File nerModelDirectory,
			File outFile,
			Boolean mantainNerFromGenericModel,
			Integer bertMaxSentenceLength,
			Integer bertDimension,
			Integer bertBatchSize,
			Boolean bertCaseSensitive,
			Integer bertPoolingLayer) {

		if (spark == null) return false;
		if (processor == null) return false;

		if (outFile == null) return false;

		if (posModelDirectory == null) return false;
		if (!posModelDirectory.exists()) return false;
		if (!posModelDirectory.isDirectory()) return false;

		if (bertModelDirectory == null) return false;
		if (!bertModelDirectory.exists()) return false;
		if (!bertModelDirectory.isDirectory()) return false;

		if (nerModelDirectory == null) return false;
		if (!nerModelDirectory.exists()) return false;
		if (!nerModelDirectory.isDirectory()) return false;

		if (bertMaxSentenceLength == null) bertMaxSentenceLength = 512;
		if (bertDimension == null) bertDimension = 1024;
		if (bertBatchSize == null) bertBatchSize = 32;
		if (bertCaseSensitive == null) bertCaseSensitive = false;
		
		boolean resultado = true;
		try {

			String ini = LocalDateTime.now().format(DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT));
			
			Stream<Articulo> stream = StreamSupport.stream(
					Spliterators.spliteratorUnknownSize(
							processor, 
							Spliterator.DISTINCT), 
					false);
			//TODO List<Articulo> data = stream.collect(Collectors.toList());
			//TODO List<Articulo> data = stream.limit(2).collect(Collectors.toList());
			List<Articulo> data = stream.collect(Collectors.toList());
			
			if ((data == null) || (data.size() == 0)) {
				
				resultado = false;
				
			} else {

				List<Row> rows = TrainRepository.generateDS(data);
				StructType structType = TrainRepository.generateStructType();
				
				String build = LocalDateTime.now().format(DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT));

				NerPipeline pipeline = null;
				Integer key = getHash(
						posModelDirectory, 
						bertModelDirectory,
						nerModelDirectory,
						bertMaxSentenceLength, 
						bertDimension, 
						bertBatchSize,
						bertCaseSensitive,
						bertPoolingLayer);
				
				if (!PIPELINES.containsKey(key)) {
					pipeline = new NerPipeline(
						spark.sparkContext(),
						spark,
						HADOOP_FILE_PREFIX + posModelDirectory.getAbsolutePath(),
						HADOOP_FILE_PREFIX + bertModelDirectory.getAbsolutePath(),
						HADOOP_FILE_PREFIX + nerModelDirectory.getAbsolutePath(),
						bertMaxSentenceLength,
						bertDimension,
						bertBatchSize,
						bertCaseSensitive,
						bertPoolingLayer);
					PIPELINES.put(key, pipeline);
				} else {
					pipeline = PIPELINES.get(key);
				}

				String prepare = LocalDateTime.now().format(DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT));

				CoNLL2003Generator generator = new CoNLL2003Generator(spark);
				
				Dataset<Row> ds = pipeline.
						execute(
								rows, 
								structType, 
								HADOOP_FILE_PREFIX + FilenameUtils.separatorsToUnix(outFile.getAbsolutePath()+"_ALL"));
				
				@SuppressWarnings("unchecked")
				Dataset<Row> conllDs = generator.
						generate(
								ds,
								mantainNerFromGenericModel);
				
				double tasa = generator.
						save(
								conllDs, 
								outFile.getAbsolutePath());

				LOG.info(
						"\r\nCONLL2003 TIME for [" + rows.size() + "] documents. PRECISSION = " + tasa * 100 +
						"\r\n\tINI:" + ini +
						"\r\n\tDAT:" + build +
						"\r\n\tPRE:" + prepare +
						"\r\n\tEND:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT)) );

			}
		} catch (Exception ex) {
			resultado = false;
			LOG.warn("CONLL2003 FAIL " + ex.toString());
		}

		return resultado;
		
	}

	/**
	 * @param spark Sesion de Spark donde se ejecutar� la preparaci�n de datos
	 * @param conllTrainDirectory
	 * @param bertModelDirectory Directorio con el modelo utilizado para el marcado de palabras
	 * @param bertMaxSentenceLength
	 * @param bertDimension
	 * @param bertBatchSize
	 * @param bertCaseSensitive
	 * @param nerModelDirectory Directorio de salida del modelo NER entrenado
	 * @param nerTensorFlowGraphDirectory Directorio de grafos de TensorFlow
	 * @param nerTfMinEpochs
	 * @param nerTfMaxEpochs
	 * @param nerTfLr
	 * @param nerTfPo
	 * @param nerTfDropOut
	 * @param nerTfValidationSplit
	 * @param nerTfBatchSize
	 * @param pipelineModelDirectory
	 * @return
	 */
	public static boolean trainModelFromConll(
			SparkSession spark, 
			File conllTrainDirectory,
			File bertModelDirectory,
			Integer bertMaxSentenceLength,
			Integer bertDimension,
			Integer bertBatchSize,
			Boolean bertCaseSensitive,
			Integer bertPoolingLayer,
			File nerModelDirectory,
			File nerTensorFlowGraphDirectory,
			Integer nerTfMinEpochs,
			Integer nerTfMaxEpochs,
			Float nerTfLr, 
			Float nerTfPo,
			Float nerTfDropOut,
			Float nerTfValidationSplit,
			Integer nerTfBatchSize) {
		
		if (spark == null) return false;
		
		if (conllTrainDirectory == null) return false;
		if (!conllTrainDirectory.exists()) return false;
		if (!conllTrainDirectory.isDirectory()) return false;
		
		if (bertModelDirectory == null) return false;
		if (!bertModelDirectory.exists()) return false;
		if (!bertModelDirectory.isDirectory()) return false;
		
		if (nerTensorFlowGraphDirectory == null) return false;
		if (!nerTensorFlowGraphDirectory.exists()) return false;
		if (!nerTensorFlowGraphDirectory.isDirectory()) return false;

		if (nerModelDirectory == null) return false;
		if (nerModelDirectory.exists() && !nerModelDirectory.isDirectory()) return false;

		boolean resultado = true;
		try {
			
			String ini = LocalDateTime.now().format(DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT));
			
			NerTrain nerTrainer = new NerTrain(
					spark.sparkContext(),
					spark,
					bertModelDirectory.getAbsolutePath(),
					bertMaxSentenceLength,
					bertDimension,
					bertBatchSize,
					bertCaseSensitive,
					bertPoolingLayer);
			
			nerTrainer.train(
					conllTrainDirectory.getAbsolutePath(),
					nerModelDirectory.getAbsolutePath(),
					nerTensorFlowGraphDirectory.getAbsolutePath(),
					nerTfMinEpochs,
					nerTfMaxEpochs,
					nerTfLr,
					nerTfPo,
					nerTfDropOut,
					nerTfValidationSplit,
					nerTfBatchSize);
			
			LOG.info(
					"\r\nTRAIN TIME for [" + conllTrainDirectory.getAbsolutePath() + "] "  +
					"\r\n\tINI:" + ini +
 					"\r\n\tEND:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT)) );

		} catch (Exception ex) {
			resultado = false;
			LOG.warn("TRAIN FAIL " + ex.toString());
		}

		return resultado;
		
	}

	/**
	 * @param spark Sesion de Spark donde se ejecutar� la preparaci�n de datos
	 * @param trainFile
	 * @param testFile
	 * @param bertModelDirectory Directorio con el modelo utilizado para el marcado de palabras
	 * @param bertMaxSentenceLength
	 * @param bertDimension
	 * @param bertBatchSize
	 * @param bertCaseSensitive
	 * @param nerModelDirectory Directorio de salida del modelo NER entrenado
	 * @param nerTensorFlowGraphDirectory Directorio de grafos de TensorFlow
	 * @param nerTfMinEpochs
	 * @param nerTfMaxEpochs
	 * @param nerTfLr
	 * @param nerTfPo
	 * @param nerTfDropOut
	 * @param nerTfValidationSplit
	 * @param nerTfBatchSize
	 * @param pipelineModelDirectory
	 * @return
	 */
	public static boolean trainModelFromConll(
			SparkSession spark, 
			File trainFile,
			File testFile,
			File bertModelDirectory,
			Integer bertMaxSentenceLength,
			Integer bertDimension,
			Integer bertBatchSize,
			Boolean bertCaseSensitive,
			Integer bertPoolingLayer,
			File nerModelDirectory,
			File nerTensorFlowGraphDirectory,
			Integer nerTfMinEpochs,
			Integer nerTfMaxEpochs,
			Float nerTfLr, Float nerTfPo,
			Float nerTfDropOut,
			Float nerTfValidationSplit,
			Integer nerTfBatchSize) {
		
		if (spark == null) return false;
		
		if (trainFile == null) return false;
		if (!trainFile.exists()) return false;
		if (!trainFile.isFile()) return false;
		
		if (testFile == null) return false;
		if (!testFile.exists()) return false;
		if (!testFile.isFile()) return false;
		
		if (bertModelDirectory == null) return false;
		if (!bertModelDirectory.exists()) return false;
		if (!bertModelDirectory.isDirectory()) return false;
		
		if (nerTensorFlowGraphDirectory == null) return false;
		if (!nerTensorFlowGraphDirectory.exists()) return false;
		if (!nerTensorFlowGraphDirectory.isDirectory()) return false;

		if (nerModelDirectory == null) return false;
		if (nerModelDirectory.exists() && !nerModelDirectory.isDirectory()) return false;

		boolean resultado = true;
		try {
			
			String ini = LocalDateTime.now().format(DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT));
			
			NerTrain nerTrainer = new NerTrain(
					spark.sparkContext(),
					spark,
					bertModelDirectory.getAbsolutePath(),
					bertMaxSentenceLength,
					bertDimension,
					bertBatchSize,
					bertCaseSensitive,
					bertPoolingLayer);
			
			nerTrainer.trainTest(
					trainFile.getAbsolutePath(),
					testFile.getAbsolutePath(),
					nerModelDirectory.getAbsolutePath(),
					nerTensorFlowGraphDirectory.getAbsolutePath(),
					nerTfMinEpochs,
					nerTfMaxEpochs,
					nerTfLr,
					nerTfPo,
					nerTfDropOut,
					nerTfValidationSplit,
					nerTfBatchSize);
			
			LOG.info(
					"\r\nTRAIN TIME for [" + trainFile.getAbsolutePath() + "] "  +
					"\r\n\tINI:" + ini +
 					"\r\n\tEND:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT)) );

		} catch (Exception ex) {
			resultado = false;
			LOG.warn("TRAIN FAIL " + ex.toString());
		}

		return resultado;
		
	}

}
