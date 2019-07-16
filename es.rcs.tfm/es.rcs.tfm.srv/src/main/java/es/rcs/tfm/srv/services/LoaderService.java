package es.rcs.tfm.srv.services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.PubArticleEntity;
import es.rcs.tfm.db.model.PubFileEntity;
import es.rcs.tfm.db.repository.PubArticleRepository;
import es.rcs.tfm.db.repository.PubFileRepository;
import es.rcs.tfm.solr.IndexNames;
import es.rcs.tfm.solr.model.IdxArticleSolr;
import es.rcs.tfm.solr.repository.IdxArticleRepository;
import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.model.Fichero;
import es.rcs.tfm.srv.repository.FtpRepository;
import es.rcs.tfm.srv.setup.PubmedXmlProcessor;

@Service(value = SrvNames.SRV_LOADER)
@PropertySource(
		{"classpath:/META-INF/service.properties"} )
public class LoaderService {

	private @Value("${tfm.pubmed.ftp.host}") String PUBMED_FPT_HOST = "ftp.ncbi.nlm.nih.gov";
	private @Value("${tfm.pubmed.ftp.port}") int PUBMED_FTP_PORT = 21;
	private @Value("${tfm.pubmed.ftp.username}") String PUBMED_FTP_USERNAME = "anonymous";
	private @Value("${tfm.pubmed.ftp.password}") String PUBMED_FTP_PASSWORD = "password";

	private @Value("${tfm.pubmed.ftp.baseline}") String PUBMED_FTP_BASELINE = "/pubmed/baseline";
	private @Value("${tfm.pubmed.ftp.update}") String PUBMED_FTP_UPDATE = "/pubmed/updatefiles";

	private @Value("${tfm.corpus.pubmed.gzip.directory}") String CORPUS_PUBMED_GZIP_DIRECTORY = "D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/data/pubmed";
	private @Value("${tfm.corpus.pubmed.xml.directory}") String CORPUS_PUBMED_XML_DIRECTORY = "D:/Workspace-TFM/TFM/es.rcs.tfm/es.rcs.tfm.corpus/data/xmlpubmed";
	
	private static final Pattern pubmedFileNamePattern = Pattern.compile("^pubmed(\\d+)n(\\d+).xml.gz$");

	private static final Logger LOG = LoggerFactory.getLogger(LoaderService.class);
	
	public void doLoadPubmed() {

		// DESCARGAR FICHEROS DE FTP
		FTPFile[] ftpFiles = FtpRepository.getFiles(
				PUBMED_FPT_HOST, PUBMED_FTP_PORT, 
				PUBMED_FTP_USERNAME, PUBMED_FTP_PASSWORD, 
				PUBMED_FTP_UPDATE);
		
		if (ftpFiles != null) LOG.info("PROCESSING " + ftpFiles.length + " FILES");
		

		// MODELO DE FICHEROS 
		// LOS DESCARGA Y ALMACENA LA INFORMACION EN LA BASE DE DATOS
		// Stream<Fichero> ficherosStream = null;
		// Stream<Articulo> articulosStream = 
		List<Fichero> ficheros = Arrays.stream(ftpFiles).//.parallel()
			// Solo ficheros validos
			filter(f ->
					f.isValid() &&
					f.isFile() &&
					!f.isDirectory() &&
					!f.isUnknown() &&
					pubmedFileNamePattern.matcher(f.getName()).find()).
			// Transformar ficheros fisicos del FTP en Ficheros del modelo junto a su base de datos
			map(f -> 
					mapFtpFileToFichero(f, ftpFiles)).
			// Comprobar si hay cambios frente a la Base de datos
			map(f ->
					searchFicheroInDb(f)).
			// Comprobar si hay cambios frente a la Base de datos
			map(f ->
					checkFicheroInDb(f)).
			// Transformar ficheros fisicos del FTP en Ficheros del modelo junto a su base de datos
			map(f -> 
					checkDownloadNeeded(
							CORPUS_PUBMED_GZIP_DIRECTORY,
							f)).
			// Descargar ficheros
			map(f -> 
					f.isDescargaRequerida() ? download(
							PUBMED_FPT_HOST, PUBMED_FTP_PORT, 
							PUBMED_FTP_USERNAME, PUBMED_FTP_PASSWORD, 
							PUBMED_FTP_UPDATE, 
							CORPUS_PUBMED_GZIP_DIRECTORY,
							f) : f).
			// Actualizar la Base de datos
			map(f -> 
					f.isHayCambiosEnBD() ? updateFileDB(f) : f).
			// Descomprimir y obtener los articulos
			map(f -> 
					f.isProcesarRequerido() ? uncompress(
							CORPUS_PUBMED_GZIP_DIRECTORY,
							CORPUS_PUBMED_XML_DIRECTORY,
							f) : f).
			// Procesar XML con articulos
			flatMap(f -> 
					f.isProcesarRequerido() ? Stream.generate(
						new PubmedXmlProcessor(
								CORPUS_PUBMED_XML_DIRECTORY, 
								f)) : null).
			// Transformar ficheros del modelo en sus componentes de BD
			map(a -> 
					searchArticuloInDb(a)).
			// Comprobar si hay cambios fente a la Base de datos
			map(a -> 
					checkArticuloInDb(a)).
			// Transformar ficheros del modelo en sus componentes de SOLR
			map(a -> 
					searchArticuloInSolr(a)).
			// Actualizar el indice
			map(a -> 
					a.isHayCambiosEnIDX() ? updateArticleIDX(a) : a).
			// Actualizar la base de datos
			map(a -> 
					a.isHayCambiosEnBD() ? updateArticleDB(a) : a).
			// Recoger ficheros procesados
			map(a -> a.getFichero()).
			distinct().
			map(f ->
					f.isBorrarXml() ? borrarXml(
							CORPUS_PUBMED_XML_DIRECTORY,
							f) : f).
			map(f -> 
					f.isHayCambiosEnBD() ? updateFileDB(f) : f).
			collect(Collectors.toList());

		long count = ficheros.size();
		LOG.info("PROCESED " + count + " FICHEROS");

		//long articulos = articulosStream.count();
		//LOG.info("PROCESED " + articulos + " ARTICULOS");

	}


	/**
	 * @param file Fichero a procesar
	 * @param files Lista con todos los ficheros a procesar
	 * @return Un bean con la información de cada fichero
	 */
	private Fichero mapFtpFileToFichero(
			FTPFile file, 
			FTPFile[] files) {
		
		//String group = file.getGroup();
		//String rawListing = file.getRawListing();
		//String user = file.getUser();
		//int hardLinkCount = file.getHardLinkCount();
		//String link = file.getLink();
		String xmlfilename = FilenameUtils.removeExtension(file.getName());
		String filename = FilenameUtils.removeExtension(xmlfilename);
		String gzfilename = xmlfilename + ".gz";
		String md5filename = gzfilename + ".md5";
		
		Calendar cal = file.getTimestamp();
		ZonedDateTime gztimestamp = null;
		if (cal != null) gztimestamp = ZonedDateTime.ofInstant(
				cal.toInstant(), 
				ZoneId.systemDefault());
		
		Fichero fichero = new Fichero(
				filename,
				file.getType(),
				gzfilename,
				gztimestamp,
				file.getSize(),
				xmlfilename,
				Arrays.stream(files).anyMatch(x -> md5filename.equals(x.getName())),
				md5filename);
		
		return fichero;		

	}

	/**
	 * Comprueba si el fichero existe en la base de datos y si hay cambios que
	 * requieran actualizar datos
	 * @param obj Datos del fichero
	 * @return Fichero actualizado si se requiere la descarag 
	 */
	private Fichero searchFicheroInDb(
			Fichero obj) {
		
		PubFileEntity db = fileDB.findBySourceAndFileName(
				PubFileEntity.FTP_PUBMED,
				obj.getNombre());

		boolean updateNeeded = false;
		if (db == null) {
			updateNeeded = true;
			db = new PubFileEntity();
			db.setSource(PubFileEntity.FTP_PUBMED);
		}
		obj.setEntidad(db);
		obj.setHayCambiosEnBD(updateNeeded);
		obj.setDescargaRequerida(updateNeeded);

		return obj;		
		
	}

	/**
	 * Comprueba si hay cambios que requieran actualizar datos
	 * @param obj
	 * @return Datos del fichero
	 * @return Fichero actualizado si se requiere la descarag 
	 */
	private Fichero checkFicheroInDb(
			Fichero obj) {
		
		PubFileEntity db = obj.getEntidad();
		
		boolean updateNeeded = false;
		if (db != null) {
			// Check File changes
			if		(obj.getGzInstante() != null) {
				if 	(db.getGzTimeStamp() == null) {
					updateNeeded = true;
				} else if 	
					(obj.getGzInstante().compareTo(db.getGzTimeStamp()) != 0) {
					updateNeeded = true;
				}
			}
		} else {
			updateNeeded = true;
		}
		obj.setHayCambiosEnBD(updateNeeded);
		obj.setDescargaRequerida(updateNeeded);

		return obj;		
		
	}
	
	/**
	 * Comprueba que un fichero del tamaño y nombre del que se quiere descarga
	 * ya existe en el directorio
	 * @param directory Directorio donde se debe descargarel fichero
	 * @param obj Datos del fichero
	 * @return Fichero actualizado si se requiere la descarag 
	 */
	private Fichero checkDownloadNeeded(
			String directory,
			Fichero obj) {
		
		boolean isTheSame = checkFileAndSize(
				Paths.get(directory + "\\" + obj.getGzFichero()).toAbsolutePath().toString(),
				obj.getGzTamanio());
		obj.setDescargaRequerida(!isTheSame);
		
		return obj;
		
	}

	/**
	 * @param file Path completo a un fichero en local
	 * @param size Tamaño del fichero a comprobar
	 * @return coincide el fichero y el tamanio
	 */
	private boolean checkFileAndSize(
			String file,
			long size) {
		
		boolean result = false;
		Path path = Paths.get(file);
		File f = path.toFile();
		if (	(f != null) &&  
				(f.exists()) &&
				(f.isFile()) &&
				(f.length() == size) ) {
			result = true;
		}
		return result;

	}
	
	/**
	 * Descarga un fichero de pubmed, su MD5 y lo comprueba
	 * @param server Direccion del servidor
	 * @param port Puerto del servidor
	 * @param username Usuario
	 * @param password Clave de acceso
	 * @param sourceDirectory Directorio FTP con los ficheros
	 * @param targetDirectory Directorio local donde realizar la descarga
	 * @param obj Modelo con la informacion del fichero a descargar
	 * @return Modelo con la informacion del fichero a descargar
	 */
	private Fichero download(
			String server, int port,
			String username, String password,
			String sourceDirectory,
			String targetDirectory,
			Fichero obj) {

		boolean result = download(
				server, port,
				username, password,
				sourceDirectory,
				targetDirectory,
				obj.getGzFichero());
		if (result && obj.isTieneMd5()) {
			result = download(
					server, port,
					username, password,
					sourceDirectory,
					targetDirectory,
					obj.getMd5Fichero());
		}
		if (result) {
			result = checkDownload(
					targetDirectory, 
					obj.getGzFichero(), 
					obj.getMd5Fichero());
		}
		obj.setHayCambiosEnDisco(!result);
		obj.setDescargaRequerida(result);
		obj.setProcesarRequerido(result);
		
		return obj;
		
	}

	/**
	 * Descarga un fichero
	 * @param server Direccion del servidor
	 * @param port Puerto del servidor
	 * @param username Usuario
	 * @param password Clave de acceso
	 * @param sourceDirectory Directorio FTP con los ficheros
	 * @param targetDirectory Directorio local donde realizar la descarga
	 * @param fichero Nombre del fichero a descargar
	 * @return Resultado de la descarga
	 */
	private static boolean download(
			String server, int port,
			String username, String password,
			String sourceDirectory,
			String targetDirectory,
			String filename) {
		
		LOG.info("DOWNLOADING -> " + server + ":" + port + sourceDirectory + "/" + filename + " TO " + targetDirectory);
		Path fileInTargetDirectory = Paths.get(targetDirectory + "\\" + filename);
		OutputStream out;
		boolean result = true;
		try {
			out = new BufferedOutputStream(new FileOutputStream(fileInTargetDirectory.toFile().getAbsolutePath()));
			result = FtpRepository.getFile(
					server, port, 
					username, password, 
					sourceDirectory, 
					filename, 
					out);
			out.close();
		} catch (FileNotFoundException e) {
			result = false;
		} catch (IOException e) {
			result = false;
		}
		
		if (result) 
			LOG.info("DOWNLOAD OK -> " + server + ":" + port + sourceDirectory + "/" + filename + " TO " + targetDirectory);
		else
			LOG.info("DOWNLOAD FAILED -> " + server + ":" + port + sourceDirectory + "/" + filename + " TO " + targetDirectory);

		return result;
	}

	/**
	 * Comprueba el MD5 de un fichero y el contenido del fichero md5 publicado por pubmed
	 * @param directory Directorio con los ficheros
	 * @param fileGZ Fichero origen
	 * @param fileMD5 Fichero con la comprobacion
	 * @return Resultado de la comparacion
	 */
	private static boolean checkDownload(
			String directory, 
			String fileGZ, 
			String fileMD5) {
		
		boolean result = true;

		Path pathGZ = Paths.get(directory + "\\" + fileGZ);
		Path pathMD5 = Paths.get(directory + "\\" + fileMD5);
		
		String md5loaded = null;
		String md5Calculated = null;

		StringBuilder sb = new StringBuilder();
		try (Stream<String> stream = Files.lines( pathMD5, StandardCharsets.UTF_8)) {
			stream.forEach(s -> sb.append(s));
		} catch (IOException e) {
			result = false;
		}

		if (result) {
			
			if (sb.length()>0) {
				String[] strs = sb.toString().split("\\=\\s*");
				if (strs.length == 2) {
					md5loaded = strs[1];
				}
			}
			
			try (InputStream is = Files.newInputStream(pathGZ)) {
			    md5Calculated = DigestUtils.md5Hex(is);
			} catch (IOException e) {
				result = false;
			}
			
		}
		
		if (result) {
			
			if (StringUtils.isNotBlank(md5loaded)) {
				if (!md5loaded.equals(md5Calculated)) {
					result = false;
				}
			}
			
		}

		LOG.info("CHECK MD5 FILE -> " + fileGZ + "/" + fileMD5 + " result: " + result);

		return result;
		
	}

	/**
	 * Descomprime un fichero GZip
	 * @param sourceDirectory Directorio con el fichero a descomprimir
	 * @param targetDirectory Directorio donde depositar el XML
	 * @param obj Modelo con la informacion del fichero a descomprimi
	 * @return Modelo con la informacion del fichero actualizada
	 */
	private Fichero uncompress(
			String sourceDirectory,
			String targetDirectory,
			Fichero obj) {
		
		String fileGZ = obj.getGzFichero();
		String fileXML = FilenameUtils.removeExtension(fileGZ);

		Path pathGZ = Paths.get(sourceDirectory + "\\" + fileGZ);
		Path pathXML = Paths.get(targetDirectory + "\\" + fileXML);
		
		boolean todoOk = false;
		
		FileInputStream fis = null;
		FileOutputStream fos = null;
		GZIPInputStream gis = null;
		try {

			// Uncompress
			fis = new FileInputStream(pathGZ.toFile());
            fos = new FileOutputStream(pathXML.toFile());

			gis = new GZIPInputStream(fis);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = gis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			
			todoOk = true;
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (gis != null) try {gis.close();} catch (IOException e) {};
			if (fos != null) try {fos.close();} catch (IOException e) {};
			if (fis != null) try {fis.close();} catch (IOException e) {};
		}

		if (todoOk) {
			try {
				File file = pathXML.toFile();
				if ((	file != null) &&
						file.exists() &&
						file.isFile()) {
					// Update fichero
					obj.setTieneXml(true);
					obj.setXmlTamanio(file.length());
					obj.setXmlInstante(ZonedDateTime.ofInstant(
						            Instant.ofEpochMilli(file.lastModified()), 
						            ZoneId.systemDefault()));
				}
			} catch (Exception e) {
				todoOk = false;
			}
		}
		
		obj.setDescargaRequerida(!todoOk);
		obj.setProcesarRequerido(todoOk);
		obj.setBorrarXml(todoOk);

		return obj;
		
	}

	/**
	 * Borrar un fichero GZ descomprimido
	 * @param targetDirectory Directorio donde depositar el XML
	 * @param obj Modelo con la informacion del fichero a borrar
	 * @return Modelo con la informacion del fichero actualizada
	 */
	private Fichero borrarXml(
			String targetDirectory,
			Fichero obj) {
		
		String fileGZ = obj.getGzFichero();
		String fileXML = FilenameUtils.removeExtension(fileGZ);

		Path pathXML = Paths.get(targetDirectory + "\\" + fileXML);

		boolean todoOk = true;
		try {
			todoOk = pathXML.toFile().delete();
		} catch (Exception e) {
			todoOk = false;
		}
		obj.setBorrarXml(!todoOk);

		return obj;
		
	}

	/**
	 * Descomprime un fichero GZip
	 * @param sourceDirectory Directorio con el fichero a descomprimir
	 * @param targetDirectory Directorio donde depositar el XML
	 * @param obj Modelo con la informacion del fichero a descomprimi
	 * @return Modelo con la informacion del fichero actualizada
	 */
	private Fichero updateFileDB(
			Fichero obj) {
		
		if (obj == null) return obj;
		
		PubFileEntity db = null;
		if (obj.getEntidad() == null) {
			db = new PubFileEntity();
		} else {
			db = obj.getEntidad();
		}
		
		db.setSource(PubFileEntity.FTP_PUBMED);
		db.setFileName(obj.getNombre());
		db.setGzFileName(obj.getGzFichero());
		db.setGzSize(obj.getGzTamanio());
		db.setGzTimeStamp(obj.getGzInstante());
		db.setMd5FileName(obj.getMd5Fichero());
		db.setXmlFilename(obj.getXmlFichero());
		db.setXmlSize(obj.getXmlTamanio());
		db.setXmlTimeStamp(obj.getXmlInstante());
		db.setType(obj.getTipo());
		db = fileDB.saveAndFlush(db);
		
		obj.setEntidad(db);
		obj.setHayCambiosEnBD(false);
		
		return obj;
		
	}

	/**
	 * Comprueba si el articulo existe en la base de datos 
	 * @param obj Datos del articulo en la base de datos
	 * @return Articulo actualizado con cambios en la base de datos
	 */
	private Articulo searchArticuloInDb(Articulo obj) {
		
		boolean encontrado = false;
		PubArticleEntity db = null;
		for (Entry<String, String> id: obj.getIds().entrySet()) {
			List<PubArticleEntity> find = articleDB.findByIdentificador(id.getKey(), id.getValue());
			if ((find != null) && (!find.isEmpty())) {
				encontrado = true;
				db = find.get(0);
				break;
			}
		}
		
		if (!encontrado) {
			boolean updateNeeded = false;
			if (db == null) {
				updateNeeded = true;
				db = new PubArticleEntity();
				db.setFile(obj.getFichero().getEntidad());
			}
			obj.setEntidad(db);
			obj.setHayCambiosEnBD(updateNeeded);
		}

		return obj;

	}

	/**
	 * Comprueba si hay cambios que requieran actualizar datos
	 * @param obj Datos del articulo
	 * @return Articulo actualizado que se requiere procesar
	 */
	private Articulo checkArticuloInDb(
			Articulo obj) {
		
		PubArticleEntity db = obj.getEntidad();
		
		boolean updateNeeded = false;
			
		if (db != null) {
			// Check Articulo changes
			if		(StringUtils.isNotBlank(obj.getTitulo())) {
				if 	(StringUtils.isBlank(db.getTitle())) {
					updateNeeded = true;
				} else if 	
					(obj.getTitulo().compareTo(db.getTitle()) != 0) {
					updateNeeded = true;
				} 
			} else if 
					(StringUtils.isNotBlank(obj.getResumen())) {
				if 	(StringUtils.isBlank(db.getSummary())) {
					updateNeeded = true;
				} else if 	
					(obj.getResumen().compareTo(db.getSummary()) != 0) {
					updateNeeded = true;
				} 
			}
		} else {
			updateNeeded = true;
		}
		obj.setHayCambiosEnBD(updateNeeded);
		obj.setProcesarRequerido(updateNeeded);

		return obj;		
		
	}
	
	/**
	 * Guarda los datos en la base de datod
	 * @param obj Datos del articulo
	 * @return Articulo actualizado en la Base de Datos
	 */
	private Articulo updateArticleDB(
			Articulo obj) {
		
		if (obj == null) return obj;
		
		PubArticleEntity db = null;
		if (obj.getEntidad() == null) {
			db = new PubArticleEntity();
			db.setFile(obj.getFichero().getEntidad());
		} else {
			db = obj.getEntidad();
		}
		
		db.setPmid(obj.getPmid());
		db.setTitle(obj.getTitulo());
		db.setSummary(obj.getResumen());
		db = articleDB.saveAndFlush(db);

		obj.setEntidad(db);
		obj.setActualizarDbRequerido(false);
		obj.setProcesarRequerido(false);
		obj.setHayCambiosEnBD(false);
		
		return obj;
		
	}

	/**
	 * Comprueba si el articulo existe en el indice SolR
	 * @param obj Datos del articulo en la indice
	 * @return Articulo actualizado con cambios en el indice
	 */
	private Articulo searchArticuloInSolr(Articulo obj) {
		
		if (StringUtils.isNotBlank(obj.getPmid())) {
			String pmid = obj.getPmid();

			boolean encontrado = false;
			IdxArticleSolr idx = null;
			List<IdxArticleSolr> idxs = articleSOLR.findByPmid(obj.getPmid());
			if ((idxs != null) && (!idxs.isEmpty())) {
				encontrado = true;
				idx = idxs.get(0);
			}

			if (!encontrado) {
				boolean updateNeeded = false;
				if (idx == null) {
					updateNeeded = true;
					idx = new IdxArticleSolr();
					idx.setPmid(pmid);
				}
				obj.setIndice(idx);
				obj.setHayCambiosEnIDX(updateNeeded);
			}

		}

		return obj;

	}
	
	/**
	 * Guarda los datos en el indice
	 * @param obj Datos del articulo
	 * @return Articulo actualizado en el indice
	 */
	private Articulo updateArticleIDX(
			Articulo obj) {
		
		if (obj == null) return obj;
		
		IdxArticleSolr idx = null;
		if (obj.getIndice() == null) {
			idx = new IdxArticleSolr();
		} else {
			idx = obj.getIndice();
		}
		
		idx.setPmid(obj.getPmid());
		idx.setTitle(obj.getTitulo());
		idx.setSummary(obj.getResumen());
		idx = articleSOLR.save(idx);

		obj.setIndice(idx);
		obj.setActualizarIdxRequerido(false);
		obj.setProcesarRequerido(false);
		obj.setHayCambiosEnBD(false);
		
		return obj;
		
	}
	
	
	
	@Autowired
	@Qualifier( value = DbNames.DB_FILE_REP )
	PubFileRepository fileDB;
	
	@Autowired
	@Qualifier( value = DbNames.DB_ARTICLE_FILE_REP )
	PubArticleRepository articleDB;
	
	@Autowired
	@Qualifier( value = IndexNames.IDX_ALL_ARTICLES_REP )
	IdxArticleRepository articleSOLR;

}
