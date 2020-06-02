package es.rcs.tfm.srv.services.corpus;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import es.rcs.tfm.db.model.PubArticleEntity;
import es.rcs.tfm.db.model.PubFileEntity;
import es.rcs.tfm.solr.model.PubArticleIdx;
import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.model.BloqueAnotado;
import es.rcs.tfm.srv.model.Fichero;
import es.rcs.tfm.srv.repository.CorpusRepository;
import es.rcs.tfm.srv.repository.DatabaseRepository;
import es.rcs.tfm.srv.repository.FtpRepository;
import es.rcs.tfm.srv.repository.IndexRepository;

@Service(
		value = SrvNames.CORPUS_SRVC)
@PropertySource(
		{"classpath:/META-INF/service.properties"} )
public class CorpusService {

	private static final Logger LOG = LoggerFactory.getLogger(CorpusService.class);

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// FICHEROS
	
	/**
	 * Descarga un fichero de PMC y lo descomprime
	 * @param server Direccion del servidor
	 * @param port Puerto del servidor
	 * @param username Usuario
	 * @param password Clave de acceso
	 * @param sourceDirectory Directorio FTP con los ficheros
	 * @param targetDirectory Directorio local donde realizar la descarga
	 * @param obj Modelo con la informacion del fichero a descargar
	 * @return Modelo con la informacion del fichero a descargar
	 */
	public static Fichero download(
			String server, int port,
			String username, String password,
			String sourceDirectory,
			String dataDirectory,
			String uncompressDirectory,
			Fichero obj) {
		
		try {
			
			String sourceDir = FilenameUtils.normalize(FilenameUtils.concat(sourceDirectory, obj.getGzDirectorio()), true);
			
			Path pathGZ = Paths.get(FilenameUtils.concat(dataDirectory, obj.getGzDirectorio()));
			CorpusRepository.makeDirectoryIfNeeded (pathGZ);

			Path pathUNCOMPRESS = Paths.get(FilenameUtils.concat(uncompressDirectory, obj.getGzDirectorio()));
			CorpusRepository.makeDirectoryIfNeeded (pathUNCOMPRESS);
			
			boolean result = FtpRepository.download(
					server, port,
					username, password,
					sourceDir,
					obj.getGzFichero(),
					pathGZ.toFile().getAbsolutePath(),
					obj.getGzFichero());
			if (result && obj.isMd5()) {
				result = FtpRepository.download(
						server, port,
						username, password,
						sourceDir,
						obj.getMd5Fichero(),
						pathGZ.toFile().getAbsolutePath(),
						obj.getMd5Fichero());
			}
			if (result && obj.isMd5()) {
				result = CorpusRepository.checkDownload(
						pathGZ.toFile().getAbsolutePath(), 
						obj.getGzFichero(), 
						obj.getMd5Fichero());
			}
			if (result) {
				result = CorpusRepository.uncompress(
						pathGZ.toFile().getAbsolutePath(), 
						obj.getGzFichero(), 
						pathUNCOMPRESS.toFile().getAbsolutePath(), 
						obj.getUncompressFichero());
			}
			
			obj.setHayCambiosEnDisco(result);
			obj.setProcesoArticulosCompletado(!result);

		} catch (Exception ex) {
			LOG.warn("updateDb-FILE " + obj + " EX:" + ex.getMessage());
		}
		
		return obj;
		
	}
	
	/**
	 * Comprueba las modificaciones que un determinado fichero recuperado de FTP tiene
	 * tanto en la base de datos como en el sistema de ficheros
	 * @param data Directorio donde se descarga
	 * @param obj Fichero con los datos de la carga
	 * @return Fichero actualizado con las operaciones de actualizacion requeridas
	 */
//	@Transactional(
//			transactionManager = DbNames.DB_TX,
//			propagation = Propagation.REQUIRED)
	public Fichero calculateIfTheProcessIsNeeded(
			String dataDirectory,
			Fichero obj) {
		
		if (obj == null) return null;
		
		boolean fileUpdateNeeded = !CorpusRepository.checkFileAndSize(
				Paths.get(
						FilenameUtils.concat(dataDirectory, 
						FilenameUtils.concat(obj.getGzDirectorio(), obj.getGzFichero()))).
						toAbsolutePath().toString(),
				obj.getGzTamanio());		

		// Buscar en la Base de datos el fichero
		Optional<PubFileEntity> searchedDB = databaseRepository.searchFileInDB(obj);
		PubFileEntity db = null;
		if (searchedDB.isPresent()) {
			db = searchedDB.get();
		} else {
			db = new PubFileEntity();
		}
		
		boolean dbUpdateNeeded = !searchedDB.isPresent();

		boolean procesoArticulosCompletado = false;
		if (!PubFileEntity.PROCESSED.equals(db.getStatus())) {
			procesoArticulosCompletado = true;
		}

		if (	(!dbUpdateNeeded) &&
				(obj.getGzInstante() != null) &&
				(db.getGzTimeStamp() == null)) {
			dbUpdateNeeded = true;
		}

		if (	(!dbUpdateNeeded) &&
				(obj.getGzInstante() != null) &&
				(db.getGzTimeStamp() != null) &&
				(obj.getGzInstante().compareTo(db.getGzTimeStamp()) != 0)) {
			dbUpdateNeeded = true;
		}

		obj.setHayCambiosEnBD(dbUpdateNeeded);
		obj.setHayCambiosEnDisco(fileUpdateNeeded);
		obj.setProcesoArticulosCompletado(procesoArticulosCompletado);
		obj.setEntidad(db);
		
		return obj;

	}

	/**
	 * Actualiza la informacion de bases de datos de un fichero
	 * @param obj Fichero con los datos 
	 * @return Fichero actualizado con las operaciones de actualizacion de base de datos corregidas
	 */
//	@Transactional(
//			transactionManager = DbNames.DB_TX,
//			propagation = Propagation.REQUIRED)
	public Fichero updateDb(Fichero obj) {
		
		if (obj == null) return null;
		return databaseRepository.updateDB(obj);
		
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// ARTICULOS

	/**
	 * Comprueba las modificaciones que un determinado articulo recuperado de FTP tiene
	 * tanto en la base de datos como en el indice documental
	 * @param obj El articulo
	 * @return el articulo con los requisitos de cambios
	 */
//	@Transactional(
//			transactionManager = DbNames.DB_TX,
//			propagation = Propagation.REQUIRED)
	public Articulo calculateIfTheProcessIsNeeded(Articulo obj) {
		
		if (obj == null) return null;

		// ---------------------------------------------------------------------
		// Search Article in database
		try {
			
			Optional<PubArticleEntity> searchedDB = Optional.ofNullable(obj.getEntidad());
			if (!searchedDB.isPresent()) searchedDB = databaseRepository.searchArticleInDB(obj);
			boolean dbUpdateNeeded = !searchedDB.isPresent();
			if (searchedDB.isPresent()) {

				PubArticleEntity db = searchedDB.get();
				obj.setEntidad(db);

				// Check Articulo changes vs Database. 
				dbUpdateNeeded = databaseRepository.anyDifferences(obj, db);

			}
			obj.setHayCambiosEnBD(dbUpdateNeeded);
			
		} catch (Exception ex) {
			LOG.warn("isProcessNeeded DB " + obj + " EX:" + ex.getMessage());
		}
		
		// ---------------------------------------------------------------------
		// Search Article in index
		try {

			Optional<PubArticleIdx> searchedIDX = Optional.ofNullable(obj.getIndice());
			if (!searchedIDX.isPresent()) searchedIDX = indexRepository.searchArticuloInIdx(obj);
			boolean idxUpdateNeeded = !searchedIDX.isPresent();
			if (searchedIDX.isPresent()) {

				PubArticleIdx idx = searchedIDX.get();
				obj.setIndice(idx);

				// Check Articulo changes vs Database. 
				idxUpdateNeeded = indexRepository.anyDifferences(obj, idx);

			}
			obj.setHayCambiosEnIDX(idxUpdateNeeded);
			
		} catch (Exception ex) {
			LOG.warn("isProcessNeeded IDX " + obj + " EX:" + ex.getMessage());
		}
		
		return obj;
	}

	/**
	 * Actualiza la informacion de bases de datos de un articulo
	 * @param obj Articulo con los datos 
	 * @return Articulo actualizado con las operaciones de actualizacion de base de datos corregidas
	 */
//	@Transactional(
//			transactionManager = DbNames.DB_TX,
//			propagation = Propagation.REQUIRED)
	public Articulo updateDb(Articulo obj) {
		
		if (obj == null) return null;
		return databaseRepository.updateDB(obj);
		
	}

	/**
	 * Actualiza la informacion del indice de un articulo
	 * @param obj Articulo con los datos 
	 * @return Articulo actualizado con las operaciones de actualizacion del indice
	 */
//	@Transactional(
//			transactionManager = DbNames.DB_TX,
//			propagation = Propagation.REQUIRED)
	public Articulo updateIdx(Articulo obj) {
		
		if (obj == null) return null;
		return indexRepository.updateIdx(obj);
		
	}


	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// BLOQUES
	public BloqueAnotado updateDb(BloqueAnotado obj) {

		if (obj == null) return null;
		return databaseRepository.updateDB(obj);
		
	}
	
	@Autowired
	@Qualifier( value = SrvNames.DATABASE_REP )
	DatabaseRepository databaseRepository;

	@Autowired
	@Qualifier( value = SrvNames.INDEX_REP )
	IndexRepository indexRepository;

}
