package es.rcs.tfm.srv.repository;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpRepository {

	private static final Logger LOG = LoggerFactory.getLogger(FtpRepository.class);

	public static final FTPFile[] getFiles(
			String server, 
			int port,
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

	public static boolean getFile(
			String server, 
			int port,
			String username,
			String password,
			String directory,
			String filename,
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
				boolean result = ftp.retrieveFile(directory + "/" + filename, out);
				if (!result) {
					error = true;
					errorMsg = toMessage("getFile: " + directory, -2, " retrieveFile devuelve FALSE");
				} else {
					reply = ftp.getReplyCode();
					if (!FTPReply.isPositiveCompletion(reply)) {
						error = true;
						errorMsg = toMessage("retrieveFile: " + directory, reply, ftp.getReplyString());
					}
				}
			} 
		} catch(IOException e) {
			error = true;
			errorMsg = toMessage("getFile: " + directory, -1, e.getLocalizedMessage());
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch(IOException ioe) {
					error = true;
					errorMsg = toMessage("getFile: " + directory, -2, ioe.getLocalizedMessage());
				}
			}
		}		
	    
	    if (error) LOG.info(errorMsg);
	    return !error;
	    
	}


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
