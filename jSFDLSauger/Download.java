package jSFDLSauger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import io.netty.util.CharsetUtil;

public class Download extends Thread {

	private String host;
	private int port;
	private String user;
	private String pass;
	private String filepath;
	private String filedest;
	private int id;

	public Download() {
		super();
	}

	public void set(String sfdl_host, int sfdl_port, String sfdl_user, String sfdl_pass, String sfdl_filepath, String sfdl_filedest, int fileID) {
		host = sfdl_host;
		port = sfdl_port;
		user = sfdl_user;
		pass = sfdl_pass;
		filepath = sfdl_filepath;
		filedest = sfdl_filedest;
		id = fileID;
	}

	public void fileDownload() {

		if (jSFDLSauger.Globals.debug == true) {
			jSFDLSauger.Debug("[DOWNLOAD] Thread " + Thread.currentThread().getId() + " is running");
		}

		FTPClient client = new FTPClient();

		client.setConnectTimeout(jSFDLSauger.Globals.ftpTimeout);
		client.setAutodetectUTF8(true);
		client.setCharset(CharsetUtil.UTF_8);
		client.setControlEncoding(CharsetUtil.UTF_8.name());

		// connect to server
		try {
			client.connect(host, port);
		} catch (SocketException e) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[DOWNLOAD] Error: " + e.getMessage());
			}
			Console.printText("DOWNLOAD", "(Error) " + e.getMessage(), false);
		} catch (IOException e) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[DOWNLOAD] Error: " + e.getMessage());
			}
			Console.printText("DOWNLOAD", "(Error) " + e.getMessage(), false);
		}

		// send login
		try {
			client.login(user, pass);
		} catch (IOException e) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[DOWNLOAD] Error: " + e.getMessage());
			}
			Console.printText("DOWNLOAD", "(Error) " + e.getMessage(), false);
		}

		client.enterLocalPassiveMode();
		
		// set file type to binary because default is ascii
		try {
			client.setFileType(FTP.BINARY_FILE_TYPE);
		} catch (IOException e2) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[DOWNLOAD] Error: " + e2.getMessage());
			}
			Console.printText("DOWNLOAD", "(Error) " + e2.getMessage(), false);
		}

		File filedownload = new File(filedest);
		// final String fileDestName = filedownload.getName();

		if (filedownload.exists()) {
			client.setRestartOffset(filedownload.length());
		} else {
			try {
				filedownload.getParentFile().mkdirs();
				filedownload.createNewFile();
			} catch (IOException e) {
				jSFDLSauger.Debug("[DOWNLOAD] Error: " + e.getMessage());
				Console.printText("DOWNLOAD", "(Error) " + e.getMessage(), false);
			}
		}

		OutputStream out = null;
		try {
			out = new CountingOutputStream(new FileOutputStream(filedownload, true)) {
				public void beforeWrite(int count) {
					super.beforeWrite(count);
					
					long downloadProgress = getCount();
					// System.out.println("Download: " + fileDestName + " [" + jSFDLSauger.humanReadableByteCount(downloadProgress, false) + "]");
					
					Data.updateFileDownloadProgressByID(id, downloadProgress);
				}
			};
		} catch (FileNotFoundException e1) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[DOWNLOAD] Error: " + e1.getMessage());
			}
			Console.printText("DOWNLOAD", "(Error) " + e1.getMessage(), false);
		}

		try {
			boolean success = client.retrieveFile(filepath, out);
			if (success) {
				// System.out.println(fileDestName + " downloaded successfully.");
				Data.fileDoneByID(id);
			}
		} catch (IOException e) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[DOWNLOAD] Error: " + e.getMessage());
			}
			Console.printText("DOWNLOAD", "(Error) " + e.getMessage(), false);
		}

		try {
			out.close();
		} catch (IOException e) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[DOWNLOAD] Error: " + e.getMessage());
			}
			Console.printText("DOWNLOAD", "(Error) " + e.getMessage(), false);
		}

		try {
			client.disconnect();
		} catch (IOException e) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[DOWNLOAD] Error: " + e.getMessage());
			}
			Console.printText("DOWNLOAD", "(Error) " + e.getMessage(), false);
		}
	}

	public void run() {
		fileDownload();
	}
}
