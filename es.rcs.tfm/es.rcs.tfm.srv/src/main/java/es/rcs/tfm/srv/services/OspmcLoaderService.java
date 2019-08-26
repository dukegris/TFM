package es.rcs.tfm.srv.services;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import es.rcs.tfm.srv.repository.CorpusRepository;
import es.rcs.tfm.srv.repository.FtpRepository;
import es.rcs.tfm.srv.setup.PmcProcessor;

@Service(value = SrvNames.PMC_LOADER_SRVC)
@DependsOn(value = SrvNames.CORPUS_SRVC)
@PropertySource(
		{"classpath:/META-INF/service.properties"} )
public class OspmcLoaderService {

	private static final Logger LOG = LoggerFactory.getLogger(OspmcLoaderService.class);

	private @Value("${tfm.ospmc.ftp.host}") String FPT_HOST = "ftp.ncbi.nlm.nih.gov";
	private @Value("${tfm.ospmc.ftp.port}") int FTP_PORT = 21;
	private @Value("${tfm.ospmc.ftp.username}") String FTP_USERNAME = "anonymous";
	private @Value("${tfm.ospmc.ftp.password}") String FTP_PASSWORD = "password";

	private @Value("${tfm.ospmc.ftp.baseline}") String FTP_BASELINE = "/pub/pmc";
	private @Value("${tfm.ospmc.ftp.files}") String FTP_LIST_FILE = "oa_file_list.csv";

	private @Value("${tfm.corpus.ospmc.gzip.directory}") String CORPUS_PMC_GZIP_DIRECTORY = "D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/data/pmc_gzip";
	private @Value("${tfm.corpus.ospmc.tar.directory}") String CORPUS_PMC_TAR_DIRECTORY = "D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/data/pmc_tar";
	private @Value("${tfm.corpus.ospmc.pmc.directory}") String CORPUS_PMC_DIRECTORY = "D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/data/pmc_dir";

	private static final Pattern SOLO_GZIP_PTRN = Pattern.compile(".*PMC(\\d+).tar.gz$");
	
	public void doLoadNewData() {
		
		// DESCARGAR FICHEROS DE FTP
		FTPFile[] ftpFiles = FtpRepository.getFiles(
				FPT_HOST, FTP_PORT, 
				FTP_USERNAME, FTP_PASSWORD, 
				FTP_BASELINE);
		
		if (ftpFiles != null) LOG.info("PROCESSING " + ftpFiles.length + " FILES");
		
		// LISTA DE FICHEROS A DESCARGAR
		Stream<Fichero> ficherosStream = Arrays.stream(ftpFiles).//.parallel()
			filter(f ->
					f.isValid() &&
					f.isFile() &&
					!f.isDirectory() &&
					!f.isUnknown() &&
					FTP_LIST_FILE.equals(f.getName()) && (
					CorpusRepository.checkDownloadNeeded(
							CORPUS_PMC_GZIP_DIRECTORY,
							f.getName(),
							f.getSize()) &&
					// Descargar ficheros indice del FTP en local
					FtpRepository.download(
							FPT_HOST, FTP_PORT, 
							FTP_USERNAME, FTP_PASSWORD, 
							FTP_BASELINE, 
							f.getName(),
							CORPUS_PMC_GZIP_DIRECTORY,
							f.getName()))).
			// Obtener todos los ficheros de los articulos del índice
			flatMap(f -> 
					new PmcProcessor(
							CORPUS_PMC_GZIP_DIRECTORY, 
							f.getName()).
						stream()).
			// Comprobar si se requiere descargar
			map (f->
					corpusSrvc.isProcessNeeded(f)).
			// Descargar ficheros
			map(f -> 
					SOLO_GZIP_PTRN.matcher(f.getGzFichero()).find() &&
					f.isHayCambiosEnDisco() 
						? CorpusService.download(
							FPT_HOST, FTP_PORT, 
							FTP_USERNAME, FTP_PASSWORD, 
							FTP_BASELINE, 
							CORPUS_PMC_GZIP_DIRECTORY,
							CORPUS_PMC_TAR_DIRECTORY,
							f) 
						: f).
			// Actualizar datos de los ficheros en DB
			map(f -> 
					f.isHayCambiosEnBD() ? corpusSrvc.updateDb(f) : f)
			;

		Stream<Articulo> articulosStream = ficherosStream.
				// Procesar XML con articulos
				flatMap(f -> 
						f.isHayCambiosEnDisco() && 
						(f.getArticulos() != null) &&
						(!f.getArticulos().isEmpty())
							? f.getArticulos().stream() 
							: null).
				//Comprobar si se requiere descargar
				map (a->
						corpusSrvc.isProcessNeeded(a)).
				// Actualizar datos de los ficheros en DB
				map(a -> 
						a.isHayCambiosEnBD() ? corpusSrvc.updateDb(a) : a).
				// Actualizar datos de los ficheros en el indice
				map(a -> 
						a.isHayCambiosEnBD() ? corpusSrvc.updateIdx(a) : a)
				;

//		List<Fichero> f = ficherosStream.collect(Collectors.toList());
		List<Articulo> a = articulosStream.collect(Collectors.toList());
//		System.out.println(f.size() + "\t" + a.size());
		System.out.println(a.size());
		
	}

	@Autowired
	@Qualifier( value = SrvNames.CORPUS_SRVC )
	CorpusService corpusSrvc;

}
