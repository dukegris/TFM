package es.rcs.tfm.srv.services.corpus;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.PubArticleEntity;
import es.rcs.tfm.db.model.PubFileEntity;
import es.rcs.tfm.db.repository.PubArticleRepository;
import es.rcs.tfm.db.repository.PubFileRepository;
import es.rcs.tfm.solr.IndexNames;
import es.rcs.tfm.solr.model.PubArticleIdx;
import es.rcs.tfm.solr.repository.PubArticleIdxRepository;
import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.srv.model.Articulo;
import es.rcs.tfm.srv.model.Fichero;
import es.rcs.tfm.srv.repository.CorpusRepository;
import es.rcs.tfm.srv.repository.FtpRepository;

@Service(value = SrvNames.CORPUS_SRVC)
@DependsOn(value = {
		DbNames.DB_FILE_REP,
		DbNames.DB_ARTICLE_FILE_REP,
		IndexNames.IDX_ALL_ARTICLES_REP})
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

		} catch (Exception ex) {
			LOG.warn("updateDb-FILE " + obj + " EX:" + ex.getMessage());
		}
		
		return obj;
		
	}
	
	/**
	 * Comprueba las modificaciones que un determinado fichero recuperado de FTP tiene
	 * tanto en la base de datos como en el sistema de ficheros
	 * @param obj Fichero con los datos de la carga
	 * @return Fichero actualizado con las operaciones de actualizaci�n requeridas
	 */
	public Fichero calculateIfTheProcessIsNeeded(Fichero obj) {
		
		if (obj == null) return null;
		
		// Buscar en la Base de datos el fichero
		PubFileEntity db = searchFicheroInDb(obj);
		
		boolean dbUpdateNeeded = (db.getId() == null);
		
		boolean fileUpdateNeeded = !CorpusRepository.checkFileAndSize(
				Paths.get(FilenameUtils.concat(obj.getGzDirectorio(), obj.getGzFichero())).toAbsolutePath().toString(),
				obj.getGzTamanio());		

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
		obj.setEntidad(db);
		
		return obj;

	}

	/**
	 * Actualiza la informaci�n de bases de datos de un fichero
	 * @param obj Fichero con los datos 
	 * @return Fichero actualizado con las operaciones de actualizaci�n de base de datos corregidas
	 */
	public Fichero updateDb(Fichero obj) {

		try {
			
			PubFileEntity db = searchFicheroInDb(obj);
			
			db.setType(obj.getTipo());
			db.setFileName(obj.getNombre());
			
			db.setGzDirectory(obj.getGzDirectorio());
			db.setGzFileName(obj.getGzFichero());
			db.setGzSize(obj.getGzTamanio());
			db.setGzTimeStamp(obj.getGzInstante());
			
			db.setMd5FileName(obj.getMd5Fichero());
			
			db.setUncompressedFileName(obj.getUncompressFichero());
			
			db = fileDB.saveAndFlush(db);
			
			obj.setEntidad(db);
			obj.setHayCambiosEnBD(false);

		} catch (Exception ex) {
			LOG.warn("updateDb-FILE " + obj + " EX:" + ex.getMessage());
		}
		
		return obj;
		
	}
	
	/**
	 * Busca un fichero en la base de datos. siempre devuelve un objeto
	 * @param obj Datos del fichero
	 * @return Fichero actualizado si se requiere la descarag 
	 */
	public PubFileEntity searchFicheroInDb(
			Fichero obj) {
		
		PubFileEntity db = null;
		
		try {

			db = obj.getEntidad();

			if (	(db == null) &&
					(StringUtils.isNotBlank(obj.getTipo())) &&
					(StringUtils.isNotBlank(obj.getNombre()))) {

				db = fileDB.findByTypeAndFileName(
					obj.getTipo(),
					obj.getNombre());

			}
			
		} catch (Exception ex) {
			LOG.warn("searchFicheroInDb-FILE " + obj + " EX:" + ex.getMessage());
		}

		if (db == null) {
			db = new PubFileEntity();
		}

		return db;		
		
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------
	// ARTICULOS
	
	/**
	 * Comprueba las modificaciones que un determinado articulo recuperado de FTP tiene
	 * tanto en la base de datos como en el indice documental
	 * @param obj El articulo
	 * @return el articulo con los requisitos de cambios
	 */
	public Articulo calculateIfTheProcessIsNeeded(Articulo obj) {
		
		// ---------------------------------------------------------------------
		// Search Article in database
		boolean dbFound = false;
		PubArticleEntity db = obj.getEntidad();

		try {
			
			if (!dbFound && (db != null)) {
				dbFound = true;
			}
			
			if (!dbFound && (StringUtils.isNotBlank(obj.getPmid()))) {
				db = articleDB.findByPmid(obj.getPmid());
				if (db != null) dbFound = true;
			}
			
			if (!dbFound && (obj.getIds() != null) && (!obj.getIds().isEmpty())) {
				for (Entry<String, String> id: obj.getIds().entrySet()) {
					List<PubArticleEntity> find = articleDB.findByIdentificador(id.getKey(), id.getValue());
					if ((find != null) && (!find.isEmpty())) {
						dbFound = true;
						db = find.get(0);
						break;
					}
				}
			}
			
			boolean dbUpdateNeeded = false;
			if (!dbFound) {
				if (db == null) {
					dbUpdateNeeded = true;
					db = new PubArticleEntity();
				}
				obj.setEntidad(db);
			}

			// Check Articulo changes vs Database. 
			// TODO Solo revisamos Titulo y resumen
			if (!dbUpdateNeeded) {
				if (	(StringUtils.isNotBlank(obj.getTitulo().getTitulo()))) {
					if 	(StringUtils.isBlank(db.getTitle())) {
						dbUpdateNeeded = true;
					} else if 	
						(obj.getTitulo().getTitulo().compareTo(db.getTitle()) != 0) {
						dbUpdateNeeded = true;
					} 
				} else if 
						(StringUtils.isNotBlank(obj.getResumen())) {
					if 	(StringUtils.isBlank(db.getSummary())) {
						dbUpdateNeeded = true;
					} else if 	
						(obj.getResumen().compareTo(db.getSummary()) != 0) {
						dbUpdateNeeded = true;
					}
				}
			}
			obj.setHayCambiosEnBD(dbUpdateNeeded);
			
			// ---------------------------------------------------------------------
			// Search Article in index
			PubArticleIdx idx = null;
			boolean idxFound = false;
			
			if (!idxFound && (StringUtils.isNotBlank(obj.getPmid()))) {
				List<PubArticleIdx> idxs = articleSOLR.findByPmid(obj.getPmid());
				if ((idxs != null) && (!idxs.isEmpty())) {
					idxFound = true;
					idx = idxs.get(0);
				}
			}
			
			boolean idxUpdateNeeded = false;
			if (!idxFound) {
				if (idx == null) {
					idxUpdateNeeded = true;
					idx = new PubArticleIdx();
				}
				obj.setIndice(idx);
			}

			// Check Articulo changes vs index
			// TODO Solo revisamos Titulo y resumen
			if (!idxUpdateNeeded) {
				if		(StringUtils.isNotBlank(obj.getTitulo().getTitulo())) {
					if 	(StringUtils.isBlank(idx.getTitle())) {
						idxUpdateNeeded = true;
					} else if 	
						(obj.getTitulo().getTitulo().compareTo(idx.getTitle()) != 0) {
						idxUpdateNeeded = true;
					} 
				} else if 
						(StringUtils.isNotBlank(obj.getResumen())) {
					if 	(StringUtils.isBlank(idx.getSummary())) {
						idxUpdateNeeded = true;
					} else if 	
						(obj.getResumen().compareTo(idx.getSummary()) != 0) {
						idxUpdateNeeded = true;
					}
				}
			}
			obj.setHayCambiosEnIDX(idxUpdateNeeded);
			
		} catch (Exception ex) {
			LOG.warn("isProcessNeeded " + obj + " EX:" + ex.getMessage());
		}
		
		return obj;
	}

	/**
	 * Actualiza la informaci�n de bases de datos de un articulo
	 * @param obj Articulo con los datos 
	 * @return Articulo actualizado con las operaciones de actualizaci�n de base de datos corregidas
	 */
	public Articulo updateDb(Articulo obj) {

		try {
			
			PubArticleEntity db = searchArticuloInDb(obj);
			
			obj.getAutores();
			obj.getBlocks();
			//obj.getBlocksOfType(type);
			obj.getDatos();
			obj.getDescriptores();
			obj.getEntidad();
			obj.getEstado();
			obj.getFarmacos();
			obj.getFasciculo();
			obj.getFechas();
			obj.getFicheroPmc();
			obj.getFicheroPubmed();
			obj.getGenes();
			obj.getIdioma();
			obj.getIds();
			obj.getKeywords();
			obj.getLibro();
			obj.getLocalizacion();
			obj.getMedio();
			obj.getObservaciones();
			obj.getPermisos();
			obj.getPmid();
			obj.getReferencias();
			obj.getResumen();
			obj.getResumenAlternativo();
			obj.getRevista();
			obj.getSecciones();
			obj.getTerminos();
			obj.getTitulo();
			obj.getTituloOriginal();
			obj.getVersion();
			obj.getVersionFecha();
			
/*
			db.setFilePmc(filePmc);
			db.setFilePubmed(filePubmed);
			db.setIdentifiers(identifiers);
			db.setKeywords(keywords);
			db.setLanguage(language);
			db.setMediaType(mediaType);
*/
			db.setPmid(obj.getPmid());
			db.setSummary(obj.getResumen());
			db.setTitle(obj.getTitulo().getTitulo());
			
			db = articleDB.saveAndFlush(db);

/*
			obj.setAutores(autores);
			obj.setCentros(centros);
			obj.setEntidad(entidad);
			obj.setFasciculo(fasciculo);
			obj.setFechasPublicacion(fechasPublicacion);
			obj.setFicheroPmc(ficheroPmc);
			obj.setFicheroPubmed(ficheroPubmed);
			obj.setGenes(genes);
			obj.setIdioma(idioma);
			obj.setIds(ids);
			obj.setKeywords(keywords);
			obj.setMedio(medio);
			obj.setMesh(mesh);
			obj.setQuimicos(quimicos);
 */
			obj.setPmid(db.getPmid());
			obj.setResumen(db.getSummary());
			obj.getTitulo().setTitulo(db.getTitle());
			
			obj.setEntidad(db);
			obj.setHayCambiosEnBD(false);
			
		} catch (Exception ex) {
			LOG.warn("updateDb-ARTICLE " + obj + " EX:" + ex.getMessage());
		}
		
		return obj;
		
	}
	
	/**
	 * Actualiza la informaci�n del indice de un articulo
	 * @param obj Articulo con los datos 
	 * @return Articulo actualizado con las operaciones de actualizaci�n del indice
	 */
	public Articulo updateIdx(Articulo obj) {

		try {
			
			PubArticleIdx idx = searchArticuloInIdx(obj);
			
			idx.setPmid(obj.getPmid());
			idx.setTitle(obj.getTitulo().getTitulo());
			idx.setSummary(obj.getResumen());
			idx = articleSOLR.save(idx);

			obj.setIndice(idx);
			obj.setHayCambiosEnIDX(false);
			
		} catch (Exception ex) {
			LOG.warn("updateIdx-ARTICLE " + obj + " EX:" + ex.getMessage());
		}
		
		return obj;
		
	}
	
	/**
	 * Busca un articulo en la base de datos. siempre devuelve un objeto
	 * @param obj Datos del articulo
	 * @return Articulo en la base de datos
	 */
	public PubArticleEntity searchArticuloInDb(
			Articulo obj) {
		
		PubArticleEntity db = obj.getEntidad();

		try {
			
			if ((db == null) && (StringUtils.isNotBlank(obj.getPmid()))) {
				db = articleDB.findByPmid(obj.getPmid());
			}
	
			if ((db == null) && (obj.getIds() != null) && (!obj.getIds().isEmpty())) {
				for (Entry<String, String> id: obj.getIds().entrySet()) {
					List<PubArticleEntity> find = articleDB.findByIdentificador(id.getKey(), id.getValue());
					if ((find != null) && (!find.isEmpty())) {
						db = find.get(0);
					}
				}
			}
			
		} catch (Exception ex) {
			LOG.warn("searchArticuloInDb " + obj + " EX:" + ex.getMessage());
		}

		if (db == null) {
			db = new PubArticleEntity();
		}

		return db;		
		
	}
	
	/**
	 * Busca un articulo en el indice. siempre devuelve un objeto
	 * @param obj Datos del articulo
	 * @return Articulo en la base de datos
	 */
	public PubArticleIdx searchArticuloInIdx(
			Articulo obj) {
		
		PubArticleIdx idx = obj.getIndice();
		
		try {
			
			if (	(idx == null) &&
					(StringUtils.isNotBlank(obj.getPmid()))) {
	
				List<PubArticleIdx> idxs = articleSOLR.findByPmid(obj.getPmid());
				if ((idxs != null) && (!idxs.isEmpty())) {
					idx = idxs.get(0);
				}
	
			}
			
		} catch (Exception ex) {
			LOG.warn("searchArticuloInIdx " + obj + " EX:" + ex.getMessage());
		}

		if (	(idx == null)) {
			idx = new PubArticleIdx();
		}

		return idx;		
		
	}

	@Autowired
	@Qualifier( value = DbNames.DB_FILE_REP )
	PubFileRepository fileDB;
	
	@Autowired
	@Qualifier( value = DbNames.DB_ARTICLE_FILE_REP )
	PubArticleRepository articleDB;
	
	@Autowired
	@Qualifier( value = IndexNames.IDX_ALL_ARTICLES_REP )
	PubArticleIdxRepository articleSOLR;

}