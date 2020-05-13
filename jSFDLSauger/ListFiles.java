package jSFDLSauger;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.fusesource.jansi.AnsiConsole;

public class ListFiles extends Thread {

	private int id;
	private String host;
	private int port;
	private String user;
	private String pass;
	private String filepath;
	private static int downloadId;
	private static long sumDownloadSize = 0;

	public ListFiles() {
		super();
	}

	public void set(int srvID, String sfdl_host, int sfdl_port, String sfdl_user, String sfdl_pass, String sfdl_filepath,
			int sfdl_downloadId) {
		id = srvID;
		host = sfdl_host;
		port = sfdl_port;
		user = sfdl_user;
		pass = sfdl_pass;
		filepath = sfdl_filepath;
		downloadId = sfdl_downloadId;
		
		AnsiConsole.systemInstall();
	}

	static void listDirectory(FTPClient ftpClient, String parentDir, String currentDir, int level) 
			throws IOException {
		String dirToList = parentDir;

		if (!dirToList.startsWith("/")) {
			dirToList = "/" + dirToList;
		}

		if (!dirToList.endsWith("/")) {
			dirToList = dirToList + "/";
		}

		if (!currentDir.equals("")) {
			dirToList += currentDir;
		}

		FTPFile[] subFiles = ftpClient.listFiles(dirToList);

		if (jSFDLSauger.Globals.debug == true) {
			jSFDLSauger.Debug("[FTP SERVER] [" + ftpClient.getReplyCode() + "] " + ftpClient.getReplyString());
		}

		if (subFiles != null && subFiles.length > 0) {
			for (FTPFile aFile : subFiles) {

				String currentFileName = aFile.getName();
				long currentFileSize = aFile.getSize();

				if (currentFileName.equals(".") || currentFileName.equals("..")) {
					continue;
				}

				if (aFile.isDirectory()) {
					if (jSFDLSauger.Globals.debug == true) {
						jSFDLSauger.Debug("[FTP] Change directory: " + dirToList + currentFileName + "/");
					}
					listDirectory(ftpClient, dirToList, currentFileName + "/", level + 1);
					Console.printText("FTP", "Get index: " + currentFileName, false);
				} else {
					if (jSFDLSauger.Globals.debug == true) {
						jSFDLSauger.Debug("[FTP] " + dirToList + currentFileName + "[" + currentFileSize + "]");
					}

					// add file to download
					if (currentFileSize > 0) {
						Data.addFile(downloadId, currentFileName, dirToList + currentFileName, currentFileSize, 0, 0, 0, 0, false, 0);
						sumDownloadSize += currentFileSize;
					}
				}
			}
		}
	}

	@Override
	public void run() {
		Console.printText("FTP", "Get file index from FTP server ...", false);
		
		FTPClient ftpClient = new FTPClient();

		try {
			ftpClient.connect(host, port);
			ftpClient.enterLocalPassiveMode();

			int replyCode = ftpClient.getReplyCode();

			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[FTP SERVER] [" + ftpClient.getReplyCode() + "] " + ftpClient.getReplyString());
			}

			if (!FTPReply.isPositiveCompletion(replyCode)) {
				if (jSFDLSauger.Globals.debug == true) {
					jSFDLSauger.Debug("[FTP] Can't connect to FTP server!");
				}
				Console.printText("FTP", "(Error) Can't connect to FTP server!", false);
				return;
			}

			boolean success = ftpClient.login(user, pass);

			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[FTP SERVER] [" + ftpClient.getReplyCode() + "] " + ftpClient.getReplyString());
			}

			if (!success) {
				if (jSFDLSauger.Globals.debug == true) {
					jSFDLSauger.Debug("[FTP] Can't log in to the FTP Server!");
				}
				Console.printText("FTP", "(Error) Can't log in to the FTP Server!", false);
				return;
			}

			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[FTP] Change directory: " + filepath);
			}
			
			Console.printText("FTP", "Get index: " + filepath, false);
			listDirectory(ftpClient, filepath, "", 0);

		} catch (IOException ex) {

			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[FTP] ERROR: " + ex.getMessage());
			}
			Console.printText("FTP", "(Error) " + ex.getMessage(), false);
		} finally {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.logout();

					if (jSFDLSauger.Globals.debug == true) {
						jSFDLSauger.Debug("[FTP SERVER] [" + ftpClient.getReplyCode() + "] " + ftpClient.getReplyString());
					}
					ftpClient.disconnect();
				}
			} catch (IOException ex) {
				if (jSFDLSauger.Globals.debug == true) {
					jSFDLSauger.Debug("[FTP] ERROR: " + ex.getMessage());
				}
				Console.printText("FTP", "(Error) " + ex.getMessage(), false);
			}

			Data.setServerDownloadSize(id, sumDownloadSize);
			sumDownloadSize = 0;
			
			Worker.addJob(id, false, false);
			Worker.startJob();
		}
	}

}
