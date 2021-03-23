package jSFDLSauger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fusesource.jansi.AnsiConsole;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SFDL extends Thread {

	private String sfdl;
	private static long sumDownloadSize = 0;

	public SFDL() {
		super();
	}

	public void set(String sfdlFile) {
		sfdl = sfdlFile;
	}

	private static final Pattern IPV4_PATTERN = Pattern
			.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

	private static boolean isIPv4Address(final String input) {
		return IPV4_PATTERN.matcher(input).matches();
	}

	private static String decrypt(String password, String encodedString) {
		byte[] data = null;
		try {
			data = Base64.getDecoder().decode(encodedString.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[SFDL] " + e.getMessage());
			}
			return null;
		}

		MessageDigest md5pass = null;
		try {
			md5pass = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[SFDL] " + e.getMessage());
			}
			return null;
		}

		byte[] pass = null;
		try {
			pass = md5pass.digest(password.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[SFDL] " + e.getMessage());
			}
			return null;
		}

		IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(data, 0, 16));
		SecretKeySpec keyspec = new SecretKeySpec(pass, "AES");

		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		} catch (NoSuchAlgorithmException e) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[SFDL] " + e.getMessage());
			}
			return null;
		} catch (NoSuchPaddingException e) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[SFDL] " + e.getMessage());
			}
			return null;
		}

		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec, iv);
		} catch (InvalidKeyException e) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[SFDL] " + e.getMessage());
			}
			return null;
		} catch (InvalidAlgorithmParameterException e) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[SFDL] " + e.getMessage());
			}
			return null;
		}

		byte[] decrypted = null;
		try {
			decrypted = cipher.doFinal(data);
		} catch (IllegalBlockSizeException e) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[SFDL] " + e.getMessage());
			}
			return null;
		} catch (BadPaddingException e) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[SFDL] " + e.getMessage());
			}
			return null;
		}

		if (decrypted.length < 17) {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[SFDL] Decrypted data to short: " + decrypted.length);
			}
			return null;
		}

		byte[] return_byte = Arrays.copyOfRange(decrypted, 16, decrypted.length);
		return new String(return_byte, StandardCharsets.UTF_8);
	}

	private static String getNode(String node, org.w3c.dom.Document document) {

		NodeList chk = document.getElementsByTagName(node);
		if (chk.getLength() > 0) {
			return new String(document.getElementsByTagName(node).item(0).getTextContent());
		}

		return null;
	}

	public static void readSFDL(String sfdl) {

		if (jSFDLSauger.Globals.debug == true) {
			jSFDLSauger.Debug("[SFDL] Thread readSFDL " + Thread.currentThread().getId() + " ["
					+ Thread.currentThread().getName() + "] is running");
		}

		File file = new File(sfdl);

		// get xml data from sfdl file
		if (file.exists() && !file.isDirectory()) {

			Console.printText("SFDL", "Get data from: " + sfdl, false);
			
			String sfdl_file = file.getName();
			String sfdl_file_clean = sfdl_file.replaceFirst("[.][^.]+$", ""); // file name without extension
			String sfdl_path = file.getAbsolutePath();

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

			DocumentBuilder documentBuilder = null;
			try {
				documentBuilder = documentBuilderFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e2) {
				jSFDLSauger.Debug(e2.getMessage());
			}

			org.w3c.dom.Document document = null;
			try {
				document = documentBuilder.parse(file);
			} catch (SAXException e1) {
				if (jSFDLSauger.Globals.debug == true) {
					jSFDLSauger.Debug(e1.getMessage());
				}
				
				Console.printText("SFDL", "(Error) " + e1.getMessage(), false);
			} catch (IOException e1) {
				if (jSFDLSauger.Globals.debug == true) {
					jSFDLSauger.Debug(e1.getMessage());
				}
				
				Console.printText("SFDL", "(Error) " + e1.getMessage(), false);
			}

			String sfdl_Encrypted = null;

			String sfdl_BulkFolderMode = null;
			List<String> sfdl_BulkFolderPathArray = new ArrayList<>();
			List<String> sfdl_FileListArray = new ArrayList<>();
			List<Long> sfdl_FileSizeArray = new ArrayList<>();

			String sfdl_Description = null;
			String sfdl_Uploader = null;
			String sfdl_Host = null;
			int sfdl_Port = 0;
			String sfdl_Username = null;
			String sfdl_Password = null;

			sfdl_Encrypted = getNode("Encrypted", document);
			sfdl_BulkFolderMode = getNode("BulkFolderMode", document);
			sfdl_Description = getNode("Description", document);
			sfdl_Uploader = getNode("Uploader", document);
			sfdl_Host = getNode("Host", document);
			sfdl_Port = Integer.parseInt(getNode("Port", document));
			sfdl_Username = getNode("Username", document);
			sfdl_Password = getNode("Password", document);

			if (sfdl_BulkFolderMode.equals("true")) {
				NodeList downloadFiles = document.getElementsByTagName("BulkFolderPath");

				for (int i = 0; i < downloadFiles.getLength(); i++) {
					sfdl_BulkFolderPathArray.add(downloadFiles.item(i).getTextContent());
				}
			} else {
				NodeList downloadFiles = document.getElementsByTagName("FileFullPath");
				NodeList fileSizes = document.getElementsByTagName("FileSize");

				for (int i = 0; i < downloadFiles.getLength(); i++) {
					sfdl_FileListArray.add(downloadFiles.item(i).getTextContent());
					sfdl_FileSizeArray.add(Long.valueOf(fileSizes.item(i).getTextContent()).longValue());
				}
			}

			// decrypt me hard
			String password = null;
			String decodeMe = null;
			if (sfdl_Encrypted.equals("true")) {
				
				Console.printText("SFDL", "Is password protected ...", false);
				
				String passwordFile = jSFDLSauger.Globals.passwordFile;

				try (BufferedReader br = new BufferedReader(new FileReader(passwordFile))) {
					for (String line; (line = br.readLine()) != null;) {
						password = line;
						decodeMe = decrypt(password, sfdl_Host);
						if (decodeMe != null) {
							break;
						}
					}
				} catch (FileNotFoundException e) {
					if (jSFDLSauger.Globals.debug == true) {
						jSFDLSauger.Debug("[SFDL] Password file not found: " + e.getMessage());
					}
					Console.printText("SFDL", "(Error) Password file not found: " + e.getMessage(), false);
					
					decodeMe = decrypt("mlcboard.com", sfdl_Host);
					if (decodeMe != null) {
						password = "mlcboard.com";
					}
				} catch (IOException e) {
					if (jSFDLSauger.Globals.debug == true) {
						jSFDLSauger.Debug("[SFDL] Password file I/O error: " + e.getMessage());
					}
					Console.printText("SFDL", "(Error) Password file I/O error: " + e.getMessage(), false);
				}

				// no valid password found
				if (password == null) {
					if (jSFDLSauger.Globals.debug == true) {
						jSFDLSauger.Debug("[SFDL] No valid password found!");
					}
					Console.printText("SFDL", "(Error) No valid password found!", false);
					return;
				}
				
				Console.printText("SFDL", "Valid password found: " + password, false);

				if (jSFDLSauger.Globals.debug == true) {
					jSFDLSauger.Debug("[SFDL] SFDL password: " + password);
				}

				sfdl_Description = decrypt(password, sfdl_Description);
				sfdl_Uploader = decrypt(password, sfdl_Uploader);
				sfdl_Host = decodeMe;
				sfdl_Username = decrypt(password, sfdl_Username);
				sfdl_Password = decrypt(password, sfdl_Password);
				
				if(sfdl_Username == null || sfdl_Password == null) {
					sfdl_Username = "anonymous";
					sfdl_Password = "anonymous@anonymous.com";
				}

				if (sfdl_BulkFolderMode.equals("true")) {
					for (int i = 0; i < sfdl_BulkFolderPathArray.size(); i++) {
						String decryptedString = decrypt(password, sfdl_BulkFolderPathArray.get(i));
						sfdl_BulkFolderPathArray.set(i, decryptedString);
					}
				} else {
					for (int i = 0; i < sfdl_FileListArray.size(); i++) {
						String decryptedString = decrypt(password, sfdl_FileListArray.get(i));
						sfdl_FileListArray.set(i, decryptedString);
					}
				}
			}

			if (sfdl_Host != null) {
				if (isIPv4Address(sfdl_Host) == false) {
					if (jSFDLSauger.Globals.debug == true) {
						jSFDLSauger.Debug("[SFDL] [IP CHECK] Host is not a real IP: " + sfdl_Host);
					}
					Console.printText("SFDL", "(Error) Host is not a real IP: " + sfdl_Host, false);
				}
			}

			// print some debug info
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[SFDL] SFDL (file): " + sfdl_file);
				jSFDLSauger.Debug("[SFDL] SFDL (file without extension): " + sfdl_file_clean);
				jSFDLSauger.Debug("[SFDL] SFDL (full path): " + sfdl_path);
				jSFDLSauger.Debug("[SFDL] sfdl_Encrypted: " + sfdl_Encrypted);
				jSFDLSauger.Debug("[SFDL] sfdl_BulkFolderMode: " + sfdl_BulkFolderMode);
				jSFDLSauger.Debug("[SFDL] Description: " + sfdl_Description);
				jSFDLSauger.Debug("[SFDL] Uploader: " + sfdl_Uploader);
				jSFDLSauger.Debug("[SFDL] Host: " + sfdl_Host);
				jSFDLSauger.Debug("[SFDL] Port: " + sfdl_Port);
				jSFDLSauger.Debug("[SFDL] Username: " + sfdl_Username);
				jSFDLSauger.Debug("[SFDL] Password: " + sfdl_Password);

				jSFDLSauger.Debug("[SFDL] sfdl_BulkFolderPathArray size: " + sfdl_BulkFolderPathArray.size());
				for (int i = 0; i < sfdl_BulkFolderPathArray.size(); i++) {
					jSFDLSauger.Debug("[SFDL] sfdl_BulkFolderPathArray: [" + i + "] "
							+ sfdl_BulkFolderPathArray.get(i));
				}

				jSFDLSauger.Debug("[SFDL] sfdl_FileListArray size: " + sfdl_FileListArray.size());
				for (int i = 0; i < sfdl_FileListArray.size(); i++) {
					jSFDLSauger.Debug("[SFDL] sfdl_FileListArray: [" + i + "] " + sfdl_FileListArray.get(i) + "["
							+ sfdl_FileSizeArray.get(i) + "]");
				}
			}

			if (sfdl_Description.isEmpty()) {
				sfdl_Description = sfdl_file_clean;
			}
			
			// add server data and return new id
			int newServer = Data.addServer(sfdl_Description, sfdl_Uploader, sfdl_Host, sfdl_Port, sfdl_Username,
					sfdl_Password, 0, 0, 0, 0, 0, false);

			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[SFDL] New Server ID: " + newServer);
			}
			
			// move sfdl file to download path
			String sfdlDest = jSFDLSauger.Globals.downloadRootPath + "/" + sfdl_Description + "/" + sfdl_file;
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[SFDL] Move SFDL file to: " + sfdlDest);
			}
			File sfdlDestPath = new File(jSFDLSauger.Globals.downloadRootPath + "/" + sfdl_Description);
			if(!sfdlDestPath.exists()) {
				sfdlDestPath.mkdirs();
			}
			
			file.renameTo(new File(sfdlDest));

			if (sfdl_BulkFolderMode.equals("true")) {
				for (int i = 0; i < sfdl_BulkFolderPathArray.size(); i++) {

					if (jSFDLSauger.Globals.debug == true) {
						jSFDLSauger.Debug("[SFDL] INDEX -> sfdl_BulkFolderPathArray: [" + i + "] "
								+ sfdl_BulkFolderPathArray.get(i));
					}

					if (jSFDLSauger.Globals.debug == true) {
						jSFDLSauger.Debug("[SFDL] Get file index from FTP server ...");
					}

					ListFiles fList = new ListFiles();
					fList.set(newServer, sfdl_Host, sfdl_Port, sfdl_Username, sfdl_Password, sfdl_BulkFolderPathArray.get(i), 0);
					fList.start();

				}
			} else {
				// add file to download
				for (int i = 0; i < sfdl_FileListArray.size(); i++) {

					File f = new File(sfdl_FileListArray.get(i));
					String currentFileName = f.getName();

					if (sfdl_FileSizeArray.get(i) > 0) {
						Data.addFile(newServer, currentFileName, sfdl_FileListArray.get(i) + currentFileName,
								sfdl_FileSizeArray.get(i), 0, 0, 0, 0, false, 0);
					
						sumDownloadSize += sfdl_FileSizeArray.get(i);
					}
				}
				
				Data.setServerDownloadSize(newServer, sumDownloadSize);
				sumDownloadSize = 0;
				
				Worker.addJob(newServer, false, false);
				Worker.startJob();
			}
		} else {
			if (jSFDLSauger.Globals.debug == true) {
				jSFDLSauger.Debug("[SFDL] SFDL file " + sfdl + " not found!");
			}
			
			Console.printText("SFDL", "(Error) File not found: " + sfdl, false);
		}
	}

	public void run() {
		if(jSFDLSauger.Globals.ansiColors == true) {
			AnsiConsole.systemInstall();
		}
		readSFDL(sfdl);
	}
}

