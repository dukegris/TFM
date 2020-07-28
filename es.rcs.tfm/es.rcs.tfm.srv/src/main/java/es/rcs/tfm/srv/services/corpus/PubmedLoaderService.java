package es.rcs.tfm.srv.services.corpus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.model.ArticuloBloque;
import es.rcs.tfm.srv.model.Fichero;
import es.rcs.tfm.srv.repository.DatabaseRepository;
import es.rcs.tfm.srv.repository.FtpRepository;
import es.rcs.tfm.srv.services.train.TrainService;
import es.rcs.tfm.srv.setup.PubmedXmlProcessor;

@Service(value = SrvNames.PUBMED_LOADER_SRVC)
@DependsOn(value = SrvNames.CORPUS_SRVC)
@PropertySource(
		{"classpath:/META-INF/service.properties"} )
public class PubmedLoaderService {

	private @Value("${tfm.pubmed.ftp.host}") String FPT_HOST = "ftp.ncbi.nlm.nih.gov";
	private @Value("${tfm.pubmed.ftp.port}") int FTP_PORT = 21;
	private @Value("${tfm.pubmed.ftp.username}") String FTP_USERNAME = "anonymous";
	private @Value("${tfm.pubmed.ftp.password}") String FTP_PASSWORD = "password";

	private @Value("${tfm.pubmed.ftp.baseline}") String FTP_BASELINE = "/pubmed/baseline";
	private @Value("${tfm.pubmed.ftp.update}") String FTP_UPDATE = "/pubmed/updatefiles";

	private @Value("${tfm.corpus.pubmed.gzip.directory}") String CORPUS_PUBMED_GZIP_DIRECTORY = "D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/data/pubmed_gzip";
	private @Value("${tfm.corpus.pubmed.xml.directory}") String CORPUS_PUBMED_XML_DIRECTORY = "D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/data/pubmed_xml";
	
	private static final Pattern SOLO_GZIP_PTRN = Pattern.compile("^pubmed(\\d+)n(\\d+).xml.gz$");

	private static final Logger LOG = LoggerFactory.getLogger(PubmedLoaderService.class);
	
	public void doLoadBaselineData() {
		doLoadData(FTP_BASELINE);
	}
	
	public void doLoadNewData() {
		doLoadData(FTP_UPDATE);
	}

	private final static Lock loadPubmedNewDataLock = new ReentrantLock();
	public void doLoadData(String FTP_DIRECTORY) {
		if (!loadPubmedNewDataLock.tryLock()) return;
		try {
			doLoadDataLocked(FTP_DIRECTORY);
		} catch (Exception ex) {
			LOG.error(ex.toString());
			ex.printStackTrace();
		} finally {
			loadPubmedNewDataLock.unlock();
		}
	}
	
	public void doLoadDataLocked(String FTP_DIRECTORY) {

		// DESCARGAR FICHEROS DE FTP
		FTPFile[] ftpFiles = FtpRepository.getFiles(
				FPT_HOST, FTP_PORT, 
				FTP_USERNAME, FTP_PASSWORD, 
				FTP_DIRECTORY);
		
		if ((ftpFiles != null) && (ftpFiles.length>0)) {
			LOG.info("PROCESSING " + ftpFiles.length + " FILES");
		} else {
			LOG.warn("PROCESSING NO FILES");
			return;
		}
		
		// MODELO DE FICHEROS 
		Stream<Fichero> ficherosStream = Arrays.
			stream(ftpFiles).
			//TODO
			//limit(10).
			//parallel().
			// Solo ficheros validos
			filter(f ->
					f.isValid() &&
					f.isFile() &&
					!f.isSymbolicLink() &&
					!f.isDirectory() &&
					!f.isUnknown() &&
					SOLO_GZIP_PTRN.matcher(f.getName()).find()).
			// Transformar ficheros fisicos del FTP en Ficheros del modelo junto a su base de datos
			map(f -> 
					Fichero.getInstance(
							f.getName(),
							f.getTimestamp(),
							f.getSize()) ).
			// Descarga ficheros y actualiza la Base de Datos
			peek(f-> 
					procesaFichero(f, FTP_DIRECTORY)).
			filter(f -> 
					!f.isProcessCompleted()).
			peek( f -> {
				
				PubmedXmlProcessor processor = new PubmedXmlProcessor(
						f,
						CORPUS_PUBMED_XML_DIRECTORY);
				
				Stream<Articulo> articulosStream = StreamSupport.
						stream(
								Spliterators.spliteratorUnknownSize(
										processor, 
										Spliterator.DISTINCT), 
								false).
						//TODO
						limit(16).
						//parallel().
						peek(a -> procesaArticulo(a));

				List<Articulo> articulos = articulosStream.
						collect(Collectors.toList());
				
				DatabaseRepository.saveStats(1);
				
				int articulosSize = articulos.size();
				f.setProcessCompleted(processor.getItemsSize()*0.9 <= articulosSize);
				f.setNumArticlesProcessed(f.getNumArticlesProcessed() + articulosSize);
				f.setNumArticlesTotal(processor.getItemsSize());
				corpusSrvc.updateDb(f);
				
			});

		// Puede ser null
		if (ficherosStream != null) {
			// Tirar del Streams
			// List<Fichero> items = 
			ficherosStream.collect(Collectors.toList());
		}
		
		

	}

	public void doLoadDataEnBloques(String FTP_DIRECTORY) {

		// DESCARGAR FICHEROS DE FTP
		FTPFile[] ftpFiles = FtpRepository.getFiles(
				FPT_HOST, FTP_PORT, 
				FTP_USERNAME, FTP_PASSWORD, 
				FTP_DIRECTORY);
		
		if ((ftpFiles != null) && (ftpFiles.length>0)) {
			LOG.info("PROCESSING " + ftpFiles.length + " FILES");
		} else {
			LOG.warn("PROCESSING NO FILES");
			return;
		}
		
		// MODELO DE FICHEROS 
		//Stream<Fichero> ficherosStream = Arrays.
		Stream<Articulo> articulosStream = Arrays.		
			stream(ftpFiles).
			//TODO
			limit(1).
			parallel().
			// Solo ficheros validos
			filter(f ->
					f.isValid() &&
					f.isFile() &&
					!f.isSymbolicLink() &&
					!f.isDirectory() &&
					!f.isUnknown() &&
					SOLO_GZIP_PTRN.matcher(f.getName()).find()).
			// Transformar ficheros fisicos del FTP en Ficheros del modelo junto a su base de datos
			map(f -> 
					Fichero.getInstance(
							f.getName(),
							f.getTimestamp(),
							f.getSize()) ).
			// Descarga ficheros y actualiza la Base de Datos
			peek (f-> procesaFichero(f, FTP_DIRECTORY)).
			filter(f -> !f.isProcessCompleted()).
			flatMap(f -> {

				PubmedXmlProcessor processor = new PubmedXmlProcessor(
						f,
						CORPUS_PUBMED_XML_DIRECTORY);
				
				return StreamSupport.
						stream(
								Spliterators.spliteratorUnknownSize(
										processor, 
										Spliterator.DISTINCT), 
								false);
				
			}).
			peek(a -> procesaArticulo(a));
		
		// Procesar en lotes 
		Spliterator<Articulo> split = articulosStream.spliterator();
		int chunkSize = 10;
		
		
//		List<Map<Fichero, Long>> numArticulos = new ArrayList<>();
		while(true) {
		    List<Articulo> chunk = new ArrayList<>(chunkSize);
		    for (int i = 0; i < chunkSize && split.tryAdvance(chunk::add); i++){};
		    if (chunk.isEmpty()) break;
		    // Map<Fichero, Long> result = 
		    // List<ArticuloBloque> result = 
		    train.
		    	process(spark, chunk).
		    	stream().
		    	parallel().
				peek(b -> procesaBloque(b)).
				collect(Collectors.toList());
		    	/*
				// reduce bloque to f, numarticles o groupby f
		    	collect(Collectors.groupingBy(
		    				b -> b.get,
		    				Collectors.counting()
		    			));^
		    	*/
		    //bloques.addAll(chunkBlock);
		}					

		/*
		// Puede ser null
		if (ficherosStream != null) {
			ficherosStream.forEach(f -> {
//				if ("pubmed20n1093.xml.gz".equals(f.getGzFichero())) {
				if (!f.isProcesoArticulosCompletado()) {
					
					PubmedXmlProcessor processor = new PubmedXmlProcessor(
							f,
							CORPUS_PUBMED_XML_DIRECTORY);
					
					Stream<Articulo> articulosStream = StreamSupport.
							stream(
									Spliterators.spliteratorUnknownSize(
											processor, 
											Spliterator.DISTINCT), 
									false).
							//TODO 
							parallel().
							peek(a -> procesaArticulo(a));

					//List<Articulo> articulos = articulosStream.
					//		collect(Collectors.toList());
					
					int articulosSize = 0;
					
					Spliterator<Articulo> split = articulosStream.spliterator();
					int chunkSize = 10;
					
					List<BloqueAnotado> bloques = new ArrayList<BloqueAnotado>();
					while(true) {
					    List<Articulo> chunk = new ArrayList<>(chunkSize);
					    for (int i = 0; i < chunkSize && split.tryAdvance(chunk::add); i++){};
					    articulosSize += chunk.size();
					    if (chunk.isEmpty()) break;
					    List<BloqueAnotado> chunkBlock = train.
					    	process(spark, chunk).
					    	stream().
					    	parallel().
							peek(b -> procesaBloque(b)).
							// reduce bloque to f, numarticles o groupby f
					    	collect(Collectors.toList());
					    bloques.addAll(chunkBlock);
					}					
					
					DatabaseRepository.saveStats(1);
					
					f.setProcesoArticulosCompletado(processor.getItemsSize()*0.9 <= articulosSize);
					f.setNumArticlesProcessed(f.getNumArticlesProcessed() + articulosSize);
					f.setNumArticlesTotal(processor.getItemsSize());
					corpusSrvc.updateDb(f);

				} 
//				}
			});
		}
		*/
	}

	@Transactional(
		transactionManager = DbNames.DB_TX,
		propagation = Propagation.REQUIRES_NEW)
	private Fichero procesaFichero(Fichero f, String FTP_DIRECTORY) {
				
		// Calcular el fichero debe ser procesado
		f = corpusSrvc.calculateIfTheProcessIsNeeded(
				CORPUS_PUBMED_GZIP_DIRECTORY,
				f);
		
		// Descargar ficheros
		if (f.isChangesInDisk()) {
				CorpusService.download(
						FPT_HOST, FTP_PORT, 
						FTP_USERNAME, FTP_PASSWORD, 
						FTP_DIRECTORY,
						CORPUS_PUBMED_GZIP_DIRECTORY,
						CORPUS_PUBMED_XML_DIRECTORY,
						f);
		}
		
		// Actualizar datos de los ficheros en DB
		if (f.isChangesInDb()) {
			f = corpusSrvc.updateDb(f);
		}
		
		return f;

	}

	@Transactional(
		transactionManager = DbNames.DB_TX,
		propagation = Propagation.REQUIRES_NEW)
	private Articulo procesaArticulo(Articulo a) {

		// Comprobar si se requiere descargar
		a = corpusSrvc.calculateIfTheProcessIsNeeded(a);
		// Actualizar datos de los ficheros en DB
		if (a.isChangesInDb()) a = corpusSrvc.updateDb(a);
		// Actualizar datos de los ficheros en el indice
		if (a.isChangesInIdx()) a = corpusSrvc.updateIdx(a);

		return a;

	}

	@Transactional(
			transactionManager = DbNames.DB_TX,
			propagation = Propagation.REQUIRES_NEW)
	private ArticuloBloque procesaBloque(ArticuloBloque b) {

		b = corpusSrvc.updateDb(b);
		
		return b;
		
	}

	@Autowired
	@Qualifier( value = SrvNames.CORPUS_SRVC )
	CorpusService corpusSrvc;

	@Autowired
	@Qualifier( value = SrvNames.SPARK_SESSION_TRAIN )
	public SparkSession spark;
	
	@Autowired
	@Qualifier(value = SrvNames.TRAINING_SRVC)
	private TrainService train;

}
