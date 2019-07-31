package es.rcs.tfm.srv.repository;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpRepository {

	private static final Logger LOG = LoggerFactory.getLogger(FtpRepository.class);

	/**
	 * Listado de ficheros en un directorio de un servidor FTP
	 * @param server nombre dns del servidor
	 * @param port Puerto de conexión
	 * @param username Usuario de conexión
	 * @param password Palabra de paso
	 * @param directory Directorio del que obtener el listado
	 * @return Lista de ficheros
	 */
	public static final FTPFile[] getFiles(
			String server, int port,
			String username,
			String password,
			String directory) {
		
		FTPFile[] files = null;
		
		FTPClient ftp = new FTPClient();
		ftp.setControlKeepAliveTimeout(300);

		FTPClientConfig config = new FTPClientConfig();
	    ftp.configure(config );
	    
	    boolean error = false;
	    String errorMsg = null;
	    try {
			int reply;
			ftp.connect(server, port);
			ftp.login(username, password);
			ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				error = true;
				errorMsg = toMessage("login: " + username + " in " + server, reply, ftp.getReplyString());
			} else {
				FTPFile[] ftpFiles = ftp.listFiles(directory);
				reply = ftp.getReplyCode();
				if (!FTPReply.isPositiveCompletion(reply)) {
					error = true;
					errorMsg = toMessage("list: " + directory, reply, ftp.getReplyString());
				} else if ((ftpFiles != null) && (ftpFiles.length > 0)) {
					files = ftpFiles;
				}
			} 
		} catch(IOException e) {
			error = true;
			errorMsg = toMessage("getFiles: " + directory, -1, e.getLocalizedMessage());
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch(IOException ioe) {
					error = true;
					errorMsg = toMessage("getFiles: " + directory, -2, ioe.getLocalizedMessage());
				}
			}
		}		
	    
	    if (error) {
	    	files = null;
	    	LOG.warn(errorMsg);
	    }
	    return files;
		
	}

	/**
	 * Descarga un fichero de un servidor FTP en un streame dado
	 * @param server nombre dns del servidor
	 * @param port Puerto de conexión
	 * @param username Usuario de conexión
	 * @param password Palabra de paso
	 * @param directory Directorio del que obtener el listado
	 * @param filename Fichero a descargar
	 * @param out Stream donde colocar el fichero
	 * @return resultado de la operación en el servidor FTP
	 */
	public static boolean getFile(
			String server, int port,
			String username,
			String password,
			String fullFilename,
			OutputStream out) {

		FTPClient ftp = new FTPClient();
		ftp.setControlKeepAliveTimeout(300);

		FTPClientConfig config = new FTPClientConfig();
	    ftp.configure(config );
	    
	    boolean error = false;
	    String errorMsg = null;
	    try {
			int reply;
			ftp.connect(server, port);
			ftp.login(username, password);
			ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				error = true;
				errorMsg = toMessage("login: " + username + " in " + server, reply, ftp.getReplyString());
			} else {
				boolean result = ftp.retrieveFile(fullFilename, out);
				if (!result) {
					error = true;
					errorMsg = toMessage("getFile: " + fullFilename, -2, " retrieveFile devuelve FALSE");
				} else {
					reply = ftp.getReplyCode();
					if (!FTPReply.isPositiveCompletion(reply)) {
						error = true;
						errorMsg = toMessage("retrieveFile: " + fullFilename, reply, ftp.getReplyString());
					}
				}
			} 
		} catch(IOException e) {
			error = true;
			errorMsg = toMessage("getFile: " + fullFilename, -1, e.getLocalizedMessage());
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch(IOException ioe) {
					error = true;
					errorMsg = toMessage("getFile: " + fullFilename, -2, ioe.getLocalizedMessage());
				}
			}
		}		
	    
	    if (error) LOG.info(errorMsg);
	    return !error;
	    
	}

	/**
	 * Descarga un fichero
	 * @param server Direccion del servidor
	 * @param port Puerto del servidor
	 * @param username Usuario
	 * @param password Clave de acceso
	 * @param sourceDirectory Directorio FTP con los ficheros
	 * @param sourceFilename Nombre del fichero a descargar
	 * @param targetDirectory Directorio local donde realizar la descarga
	 * @param targetFilename Nombre del fichero descargado
	 * @return Resultado de la descarga
	 */
	public static boolean download(
			String server, int port,
			String username, String password,
			String sourceDirectory,
			String sourceFilename,
			String targetDirectory,
			String targetFilename) {
		
		String sourceFullFilename = FilenameUtils.normalize(FilenameUtils.concat(sourceDirectory, sourceFilename), true);
		String targetFullFilename = FilenameUtils.concat(targetDirectory, targetFilename);
		
		LOG.info("DOWNLOADING -> " + server + ":" + port + sourceFullFilename + " TO " + targetFullFilename);
		Path fileInTargetDirectory = Paths.get(targetFullFilename);
		OutputStream out;
		boolean result = true;
		try {
			out = new BufferedOutputStream(new FileOutputStream(fileInTargetDirectory.toFile().getAbsolutePath()));
			result = FtpRepository.getFile(
					server, port, 
					username, password, 
					sourceFullFilename,
					out);
			out.close();
		} catch (FileNotFoundException e) {
			result = false;
		} catch (IOException e) {
			result = false;
		}
		
		if (result) 
			LOG.info("DOWNLOAD OK -> " + server + ":" + port + sourceFullFilename + " TO " + targetFullFilename);
		else
			LOG.info("DOWNLOAD FAILED -> " + server + ":" + port + sourceFullFilename + " TO " + targetFullFilename);

		return result;

	}

	/**
	 * Plantilla para los mensajes de fallo del servidor
	 * @param str Directorio
	 * @param replyCode Codigo de respuesta
	 * @param replyMessage Mensaje de respuesta
	 * @return Cadena deacuerdo a la plantilla
	 */
	private static String toMessage(String str, int replyCode, String replyMessage) {

		StringBuffer sb = new StringBuffer();
		sb.append("FALLO: ");
		sb.append(str);
		sb.append(" - CODE: ");
		sb.append(replyCode);
		sb.append(", MSG: ");
		sb.append(replyMessage);
		
		String msg = sb.toString();
		
		return msg;
		
	}	
}
