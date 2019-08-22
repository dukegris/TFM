package es.rcs.tfm.srv.services;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.model.Fichero;
import es.rcs.tfm.srv.repository.FtpRepository;
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

	public void doLoadData(String FTP_DIRECTORY) {

		// DESCARGAR FICHEROS DE FTP
		FTPFile[] ftpFiles = FtpRepository.getFiles(
				FPT_HOST, FTP_PORT, 
				FTP_USERNAME, FTP_PASSWORD, 
				FTP_DIRECTORY);
		
		if (ftpFiles != null) LOG.info("PROCESSING " + ftpFiles.length + " FILES");
		
		// MODELO DE FICHEROS 
		Stream<Fichero> ficherosStream = Arrays.stream(ftpFiles).//.parallel()
				// Solo ficheros validos
				filter(f ->
						f.isValid() &&
						f.isFile() &&
						!f.isDirectory() &&
						!f.isUnknown() &&
						SOLO_GZIP_PTRN.matcher(f.getName()).find()).
				// Transformar ficheros fisicos del FTP en Ficheros del modelo junto a su base de datos
				map(f -> 
						Fichero.getInstance(
								f.getName(),
								f.getTimestamp(),
								f.getSize())).
				// Comprobar si el fichero debe ser procesado
				map (f->
					corpusSrvc.isProcessNeeded(f)).
				// Descargar ficheros
				map(f -> 
						f.isHayCambiosEnDisco() ? CorpusService.download(
								FPT_HOST, FTP_PORT, 
								FTP_USERNAME, FTP_PASSWORD, 
								FTP_DIRECTORY,
								CORPUS_PUBMED_GZIP_DIRECTORY,
								CORPUS_PUBMED_XML_DIRECTORY,
								f) : f).
				// Actualizar datos de los ficheros en DB
				map(f -> 
						f.isHayCambiosEnBD() ? corpusSrvc.updateDb(f) : f)
				;

		Stream<Articulo> articulosStream = ficherosStream.
				// Procesar XML con articulos
				flatMap(f -> 
						f.isHayCambiosEnDisco() ? StreamSupport.stream(
								Spliterators.spliteratorUnknownSize(
										new PubmedXmlProcessor(Paths.get(FilenameUtils.concat(
												CORPUS_PUBMED_XML_DIRECTORY, 
												f.getUncompressFichero()))), 
										Spliterator.DISTINCT), 
								false) : null).
				// Actualizar datos de los ficheros en DB
				map(a -> 
						a.isHayCambiosEnBD() ? corpusSrvc.updateDb(a) : a).
				// Actualizar datos de los ficheros en el indice
				map(a -> 
						a.isHayCambiosEnBD() ? corpusSrvc.updateIdx(a) : a)
				;
		
		List<Fichero> f = ficherosStream.collect(Collectors.toList());
		List<Articulo> a = articulosStream.collect(Collectors.toList());
		System.out.println(f.size() + "\t" + a.size());

	}

	@Autowired
	@Qualifier( value = SrvNames.CORPUS_SRVC )
	CorpusService corpusSrvc;

}
