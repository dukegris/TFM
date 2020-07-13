package es.rcs.tfm.srv.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorpusRepository {

	private static final Logger LOG = LoggerFactory.getLogger(CorpusRepository.class);

	/**
	 * Comprueba si el fichero indice descargado coincide con el publicado para ver si 
	 * es necesaria la descarga y procesado
	 * @param directory Directorio donde se almacena el fichero indice
	 * @param obj Nombre del fichero indice
	 * @param size Tama�o del fichero a comprobar
	 * @return Si el fichero local es el mismo
	 */
	public static Boolean checkDownloadNeeded(
			String directory,
			String filename,
			Long size) {
		
		boolean isTheSame = checkFileAndSize(
				Paths.get(FilenameUtils.concat(directory, filename)).toAbsolutePath().toString(),
				size);
		
		return !isTheSame;
		
	}

	/**
	 * @param file Path completo a un fichero en local
	 * @param size Tama�o del fichero a comprobar
	 * @return coincide el fichero y el tamanio
	 */
	public static boolean checkFileAndSize(
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
	 * Comprueba el MD5 de un fichero y el contenido del fichero md5 publicado por pubmed
	 * @param directory Directorio con los ficheros
	 * @param fileGZ Fichero origen
	 * @param fileMD5 Fichero con la comprobacion
	 * @return Resultado de la comparacion
	 */
	public static boolean checkDownload(
			String directory, 
			String fileGZ, 
			String fileMD5) {
		
		boolean result = true;

		Path pathGZ = Paths.get(FilenameUtils.concat(directory, fileGZ));
		Path pathMD5 = Paths.get(FilenameUtils.concat(directory, fileMD5));
		
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

		LOG.info("CHECK MD5 FILE -> " + FilenameUtils.concat(fileGZ, fileMD5) + " result: " + result);

		return result;
		
	}

	/**
	 * Descomprime un fichero GZip
	 * @param sourceDirectory Directorio con el fichero a descomprimir
	 * @param sourceFilename Fichero a descomprimir
	 * @param targetDirectory Directorio donde depositar el XML
	 * @param targetFilename Fichero descomprimido
	 * @return Resultado correcto de la operaci�n
	 */
	public static boolean uncompress(
			String sourceDirectory,
			String sourceFilename,
			String targetDirectory,
			String targetFilename) {

		String sourceFullFilename = FilenameUtils.normalize(FilenameUtils.concat(sourceDirectory, sourceFilename), true);
		String targetFullFilename = FilenameUtils.concat(targetDirectory, targetFilename);
		
		boolean todoOk = false;
		
		FileInputStream fis = null;
		FileOutputStream fos = null;
		GZIPInputStream gis = null;

		String result = "";
		try {

			Path pathGZ = Paths.get(sourceFullFilename);
			Path pathUNCOMPRESS = Paths.get(targetFullFilename);

			LOG.info("UNCOMPRESS -> " + pathGZ.toFile().getAbsolutePath() + " TO " + pathUNCOMPRESS.toFile().getAbsolutePath());

			// Uncompress
			fis = new FileInputStream(pathGZ.toFile());
            fos = new FileOutputStream(pathUNCOMPRESS.toFile());

			gis = new GZIPInputStream(fis);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = gis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			
			todoOk = true;
			
			LOG.info("UNCOMPRESS OK -> " + pathGZ.toFile().getAbsolutePath() + " TO " + pathUNCOMPRESS.toFile().getAbsolutePath());

		} catch (IOException ex) {
			todoOk = false;
			result = ex.getMessage();

		} finally {
			if (gis != null) try {gis.close();} catch (IOException ex) {todoOk = false; result = ex.getMessage();};
			if (fos != null) try {fos.close();} catch (IOException ex) {todoOk = false; result = ex.getMessage();};
			if (fis != null) try {fis.close();} catch (IOException ex) {todoOk = false; result = ex.getMessage();};
		}

		if (!todoOk) {
			LOG.warn("UNCOMPRESS FAIL -> " + sourceFullFilename + " IN " + targetFullFilename + " result: " + result);
		}

		return todoOk;
		
	}

	public static void makeDirectoryIfNeeded(Path path) {
		File file = path.toFile();
		if (file.exists() && file.isDirectory()) return;
		file.mkdirs();
	}

}
