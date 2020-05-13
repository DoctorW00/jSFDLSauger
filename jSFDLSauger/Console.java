package jSFDLSauger;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

public class Console extends TimerTask {

	private int SrvID;

	public Console() {
		super();
		if(jSFDLSauger.Globals.ansiColors == true) {
			AnsiConsole.systemInstall();
		}
	}
	
	public void set(int id) {
		if(jSFDLSauger.Globals.ansiColors == true) {
			AnsiConsole.systemInstall();
		}
		
		SrvID = id;
	}
	
	public static void reset() {
		System.out.print(ansi().eraseScreen());
		System.out.print(ansi().cursor(0, 0));
	}
	
	public static void printHello() {
		System.out.println("jSFDLSauger v" + jSFDLSauger.Globals.appVersion + " (GrafSauger)");
		System.out.println("-- A JAVA SFDL/FTP downloader - https://mlcboard.com");
	}
	
	public static void printBanner() {
		if(jSFDLSauger.Globals.ansiColors == true) {
			System.out.println(ansi().fg(YELLOW).bold().a("   _  _____ ______ _____  _       _____                             ").reset());
			System.out.println(ansi().fg(YELLOW).bold().a("  (_)/ ____|  ____|  __ \\| |     / ____|                            ").reset());
			System.out.println(ansi().fg(YELLOW).bold().a("   _| (___ | |__  | |  | | |    | (___   __ _ _   _  __ _  ___ _ __ ").reset());
			System.out.println(ansi().fg(YELLOW).bold().a("  | |\\___ \\|  __| | |  | | |     \\___ \\ / _` | | | |/ _` |/ _ \\ '__|").reset());
			System.out.println(ansi().fg(YELLOW).bold().a("  | |____) | |    | |__| | |____ ____) | (_| | |_| | (_| |  __/ |   ").reset());
			System.out.println(ansi().fg(YELLOW).bold().a("  | |_____/|_|    |_____/|______|_____/ \\__,_|\\__,_|\\__, |\\___|_|   ").reset());
		    System.out.println(ansi().fg(YELLOW).bold().a(" _/ |                                                __/ |          ").reset());
		    System.out.println(ansi().fg(YELLOW).bold().a("|__/                                                |___/           ").reset());
		    System.out.println("");
		} else {
			System.out.println("   _  _____ ______ _____  _       _____                             ");
			System.out.println("  (_)/ ____|  ____|  __ \\| |     / ____|                            ");
			System.out.println("   _| (___ | |__  | |  | | |    | (___   __ _ _   _  __ _  ___ _ __ ");
			System.out.println("  | |\\___ \\|  __| | |  | | |     \\___ \\ / _` | | | |/ _` |/ _ \\ '__|");
			System.out.println("  | |____) | |    | |__| | |____ ____) | (_| | |_| | (_| |  __/ |   ");
			System.out.println("  | |_____/|_|    |_____/|______|_____/ \\__,_|\\__,_|\\__, |\\___|_|   ");
		    System.out.println(" _/ |                                                __/ |          ");
		    System.out.println("|__/                                                |___/           ");
		    System.out.println("");
		}
	}
	
	public static void printText(String text1, String text2, boolean reset) {
		// add new message to storage
		Data.addMessage(text1, text2);
		
		if(reset == true) {
			reset();
			printHello();
			printBanner();
		}
		
		if(jSFDLSauger.Globals.ansiColors == true) {
			System.out.println(ansi().fg(YELLOW).bold().a("[").fg(WHITE).bold().a(text1).fg(YELLOW).bold().a("] ").fg(GREEN).bold().a(text2).reset());
		} else {
			System.out.println("[" + text1 + "] " + text2);
		}
	}
	
	// print download progress consol output
	private static String progress(int progress, int divider) {
		
		String prog = "[";
		int i = 0;
		int j = 0;
					
		for(i = 0; i < progress/divider; i++) {
			prog += "#";
		}
		
		for(j = 0; j < (100/divider - i); j++) {
			prog += " ";
		}
		
		prog += "] " + progress + "%";
		
		return prog;
	}
	
	private static void showDownloadProgress(int id) {
		
		reset();
		printHello();
		printBanner();
		
		// calc progress
		long progCalc = 0;
		if(Data.getServer(id).downloadProgress != 0 && Data.getServer(id).downloadSize != 0) {
			progCalc = (Data.getServer(id).downloadProgress * 100) / Data.getServer(id).downloadSize;
		}
		
		if(jSFDLSauger.Globals.ansiColors == true) {
			System.out.println(ansi().fg(RED).bold().a("Download: ").fg(GREEN).bold().a(Data.getServer(id).description).reset());
			System.out.println(ansi().fg(RED).bold().a("Progress: ")
					.fg(GREEN).bold().a(jSFDLSauger.humanReadableByteCount(Data.getServer(id).downloadSize, true))
					.fg(YELLOW).bold().a(" " + progress((int) progCalc, 4) + " ")
					.fg(GREEN).bold().a(jSFDLSauger.humanReadableByteCount(Data.getServer(id).downloadProgress, true))
					.fg(YELLOW).bold().a(" " + jSFDLSauger.returnETA(Data.getServer(id).timeStart * 1000L, Data.getServer(id).downloadSize, Data.getServer(id).downloadProgress)).reset());
		} else {
			System.out.println("Download: " + Data.getServer(id).description);
			System.out.println("Progress: " + jSFDLSauger.humanReadableByteCount(Data.getServer(id).downloadSize, true) + 
					" " + progress((int) progCalc, 4) + " " +
					jSFDLSauger.humanReadableByteCount(Data.getServer(id).downloadProgress, true) +
					" " + jSFDLSauger.returnETA(Data.getServer(id).timeStart * 1000L, Data.getServer(id).downloadSize, Data.getServer(id).downloadProgress));
		}
		
		/*
		float downloadSpeed;
		long downloadSeconds = (System.currentTimeMillis() / 1000L) - Data.getServer(id).timeStart;
		if(Data.getServer(id).timeStart != 0 && Data.getServer(id).downloadSize != 0) {
			if(((System.currentTimeMillis() / 1000L) - Data.getServer(id).timeStart) > 0) {
				downloadSpeed = Data.getServer(id).downloadSize / ((System.currentTimeMillis() / 1000L) - Data.getServer(id).timeStart);
				System.out.println(ansi().fg(RED).bold().a("Speed: ")
						.fg(GREEN).bold().a(jSFDLSauger.humanReadableByteCount((long) downloadSpeed, true) + "/s")
						.fg(RED).bold().a(" Time: ").fg(GREEN).bold().a(jSFDLSauger.seconds2Time(downloadSeconds)).reset());
			}
		}
		*/
		
		if(jSFDLSauger.Globals.ansiColors == true) {
			System.out.println(ansi().fg(MAGENTA).bold().a("====================================================================").reset());
		} else {
			System.out.println("====================================================================");
		}
		
		for(int i = 0; i < Data.Filelist.size(); i++) {
			if(Data.Filelist.get(i).done == false 
					&& Data.Filelist.get(i).fileSizeDownloaded != Data.Filelist.get(i).fileSizeTotal
					&& Data.Filelist.get(i).fileSizeDownloaded != 0) {
				
				long progFile = 0;
				if(Data.Filelist.get(i).fileSizeDownloaded != 0 && Data.Filelist.get(i).fileSizeTotal != 0) {
					progFile = (Data.Filelist.get(i).fileSizeDownloaded * 100) / Data.Filelist.get(i).fileSizeTotal;
				}
				
				if(jSFDLSauger.Globals.ansiColors == true) {
					System.out.println(ansi().fg(RED).bold().a(Data.Filelist.get(i).filename + " ")
							.fg(YELLOW).bold().a(progress((int) progFile, 10) + " ")
							.fg(GREEN).bold().a(jSFDLSauger.humanReadableByteCount(Data.Filelist.get(i).fileSizeTotal, true) + " / ")
							.fg(GREEN).bold().a(jSFDLSauger.humanReadableByteCount(Data.Filelist.get(i).fileSizeDownloaded, true))
							.fg(YELLOW).bold().a(" " + jSFDLSauger.returnETA(Data.Filelist.get(i).startTime * 1000L, Data.Filelist.get(i).fileSizeTotal, Data.Filelist.get(i).fileSizeDownloaded)).reset());
				} else {
					System.out.println(Data.Filelist.get(i).filename + " " +
							progress((int) progFile, 10) + " " +
							jSFDLSauger.humanReadableByteCount(Data.Filelist.get(i).fileSizeTotal, true) + " / " +
							jSFDLSauger.humanReadableByteCount(Data.Filelist.get(i).fileSizeDownloaded, true) +
							" " + jSFDLSauger.returnETA(Data.Filelist.get(i).startTime * 1000L, Data.Filelist.get(i).fileSizeTotal, Data.Filelist.get(i).fileSizeDownloaded));
				}
			}
		}
		
		int filesDone = 0;
		int filesNotDone = 0;
		
		for(int i = 0; i < Data.Filelist.size(); i++) {
			if(Data.Filelist.get(i).done == true) {
				filesDone++;
			} else {
				filesNotDone++;
			}
		}
		
		if(filesDone != 0 && filesNotDone == 0) {
			reset();
			printHello();
			printBanner();
			
			// set server data to done
			Data.setServerDone(id);
			
			// crate a new speedreport
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

			File logFile = new File(jSFDLSauger.Globals.downloadRootPath + "/" + Data.getServer(id).description + "/speedreport.txt");

			if (!logFile.exists()) {
				try {
					logFile.getParentFile().mkdirs();
					logFile.createNewFile();
				} catch (IOException e) {
					Console.printText("Speedreport", "(Error): " + e.getLocalizedMessage(), false);
				}
			}

			// write speedreport
			try (PrintWriter output = new PrintWriter(new FileWriter(logFile, true))) {
				long downloadSeconds = (System.currentTimeMillis() / 1000L) - Data.getServer(id).timeStart;
				output.printf("%s\r\n", "[" + dateFormat.format(date) + "] jSFDLSauger v" + jSFDLSauger.Globals.appVersion + " (GrafSauger)");
				float downloadSpeed = Data.getServer(id).downloadSize / ((System.currentTimeMillis() / 1000L) - Data.getServer(id).timeStart);
				output.printf("%s\r\n", "Downloaded " + jSFDLSauger.humanReadableByteCount(Data.getServer(id).downloadSize, true) + " in " + jSFDLSauger.seconds2Time(downloadSeconds) + " (" + jSFDLSauger.humanReadableByteCount((long) downloadSpeed, true) + "/s)!");
				output.printf("%s\r\n", "A very big tank you goes to: " + Data.getServer(id).upper);
				
				// print speedreport to console
				Console.printText("Speedreport", "Downloaded " + jSFDLSauger.humanReadableByteCount(Data.getServer(id).downloadSize, true) + " in " + jSFDLSauger.seconds2Time(downloadSeconds) + " (" + jSFDLSauger.humanReadableByteCount((long) downloadSpeed, true) + "/s)!", false);
			} catch (Exception e) {
				// print error
				Console.printText("Speedreport", "(Error): " + e.getLocalizedMessage(), false);
			}
			
			// unrar files and if no error delete downloaded rar files
			if(jSFDLSauger.Globals.unrarFiles == true) {
				if(Unrar.unrarDownload(jSFDLSauger.Globals.downloadRootPath + "/" + Data.getServer(id).description) == 0) {
					Console.printText("UnRAR", "All files successfuly extracted!", false);
					
					if(jSFDLSauger.Globals.deleteAfterUnrar == true) {
						Console.printText("UnRAR", "Delete all RAR files ...", false);
						for(int i = 0; i < Data.Filelist.size(); i++) {
							String[] dlFile = Data.Filelist.get(i).remoteFilePath.split(Data.getServer(id).description);
							String fp = jSFDLSauger.Globals.downloadRootPath + "/" + Data.getServer(id).description  + dlFile[1];
							
							if(fp.matches(".*\\.rar|.*\\.[r-zR-Z][\\d]+")) {
								File f = new File(fp);
								if(!f.delete()) {
									Console.printText("UnRAR", "(Error) Unable to delete: " + fp, false);
								}
				        	}
						}
					} // delete after unrar
				}
			} // unrar
			
			jSFDLSauger.Globals.runningJobs = 0;
			Worker.resetJobs();
			Data.serverReset();
			Data.fileReset();
			
			if(jSFDLSauger.Globals.startWebUI == false) {
				Console.printText("jSFDLSauger", "All done, good bye!", false);
				Runtime.getRuntime().exit(0);
			} else {
				Console.printText("jSFDLSauger", "All done, ready for a new job!", false);
				Worker.stopTimer();
				return;
			}
		}
	}

	@Override
	public void run() {
		showDownloadProgress(SrvID);
	}
	
}
