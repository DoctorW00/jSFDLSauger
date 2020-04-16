package jSFDLSauger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log extends Thread {

	private String logText;

	public void write() {

		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

		System.out.println("[DEBUG] " + logText);

		File logFile = new File(System.getProperty("user.dir") + "/debug.txt");

		if (!logFile.exists()) {
			try {
				logFile.getParentFile().mkdirs();
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try (PrintWriter output = new PrintWriter(new FileWriter(logFile, true))) {
			output.printf("%s\r\n", "[" + dateFormat.format(date) + "] " + logText);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void set(String text) {
		logText = text;
	}

	public void run() {
		write();
	}
}