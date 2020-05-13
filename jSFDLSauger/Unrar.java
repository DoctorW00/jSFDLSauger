package jSFDLSauger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.impl.FileVolumeManager;
import com.github.junrar.rarfile.FileHeader;

public class Unrar extends Thread {
	
	// fileName = full file path
	// fileDestPath = dest extraction path
	public static int unrarFile(String fileName, String fileDestPath) {
		int errors = 0;
		File f = new File(fileName);
		Archive a = null;
		try {
			a = new Archive(new FileVolumeManager(f));
		} catch (RarException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (a != null) {
			
			// a.getMainHeader().print();
			FileHeader fh = a.nextFileHeader();
			
			int i = 0;
			while (fh != null) {
				try {
					File out = new File(fileDestPath + fh.getFileNameString().trim());
					Console.printText("UnRAR", "Extract file: " + out.getName() + " ["+ jSFDLSauger.humanReadableByteCount(a.getFileHeaders().get(i).getFullUnpackSize(), true) + "]", false);
					i++;
					FileOutputStream os = new FileOutputStream(out);
					a.extractFile(fh, os);
					os.close();
				} catch (FileNotFoundException e) {
					errors++;
					Console.printText("UnRAR", "(Error) File not found: " + e.getLocalizedMessage(), false);
				} catch (RarException e) {
					errors++;
					Console.printText("UnRAR", "(Error): " + e.getLocalizedMessage(), false);
				} catch (IOException e) {
					errors++;
					Console.printText("UnRAR", "(Error): " + e.getLocalizedMessage(), false);
				}
				fh = a.nextFileHeader();
			}
		}
		
		return errors;
	}
	
	public static int unrarDownload(String downloadRootPath) {
		
		File dPath = new File(downloadRootPath);
		if(!dPath.exists()) {
			Console.printText("UnRAR", "(Error) Path does not exist!", false);
			return 1;
		}
		
		Console.printText("UnRAR", "Searching for RAR files ...", false);
		
		int errors = 0;
		String[] extensions = new String[] { "rar" };
		Iterator<File> it = FileUtils.iterateFiles(dPath, extensions, true);
		while (it.hasNext()) {
			
			File RARFile = it.next();
			String fileName = RARFile.getName();
			String filePath = RARFile.getAbsolutePath();
			
			if(filePath.matches(".*\\.rar") && !filePath.matches(".*part.*\\d\\.rar")) {
				if(unrarFile(filePath, filePath.split(fileName)[0]) != 0) {
					errors++;
				}

        	}
        	
        	if(filePath.matches(".*part.*1\\.rar")) {
        		if(unrarFile(filePath, filePath.split(fileName)[0]) != 0) {
					errors++;
				}
        	}
        }
		return errors;
	}
	
}
