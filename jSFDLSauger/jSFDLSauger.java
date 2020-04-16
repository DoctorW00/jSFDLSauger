package jSFDLSauger;

import java.util.concurrent.TimeUnit;

import org.fusesource.jansi.AnsiConsole;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class jSFDLSauger {
	
	public static class Globals {
		public static String appVersion = "1.0.1";
		public static boolean debug = false;
		public static int maxThreads = 3;
		public static int maxJobs = 1;
		public static String downloadRootPath = System.getProperty("user.dir") + "/downloads";
		public static int ftpTimeout = 15000;
		public static boolean ansiColors = true;
		public static boolean unrarFiles = true;
		public static boolean deleteAfterUnrar = true;
		public static String passwordFile = System.getProperty("user.dir") + "/passwords.txt";
	}

	public static void Debug(String logText) {
		Log log = new Log();
		log.set(logText);
		log.start();
	}

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) {
			return bytes + " B";
		}
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static String seconds2Time(long seconds) {
		int day = (int)TimeUnit.SECONDS.toDays(seconds);        
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day *24);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);
        
        String Hms = String.format("[%02dh%02dm%02ds]", hours, minute, second);

		return Hms;
    }
	
	public static String returnETA(long startTime, long total, long current) {
		long eta = current == 0 ? 0 : (total - current) * (System.currentTimeMillis() - startTime) / current;

		String etaHms = current == 0 ? "N/A"
				: String.format("[%02d:%02d:%02d]", TimeUnit.MILLISECONDS.toHours(eta),
						TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
						TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

		return etaHms;
	}
	
	private static String fromSFDLNet(String downloadURL) {
		
		if(!downloadURL.startsWith("https://download.sfdl.net")) {
			return new String();
		}
		
		Console.printText("SFDL.net", "Downloading SFDL file ...", false);
		
		// some random user agent
		String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0";
		
		// connect to the sfdl.net webserver
		URL url = null;
		try {
			url = new URL(downloadURL);
		} catch (MalformedURLException e) {
			Console.printText("SFDL.net", "(Error): " + e.getMessage(), false);
		}
		
		URLConnection con = null;
		try {
			con = url.openConnection();
			con.setRequestProperty("User-Agent", USER_AGENT);
		} catch (IOException e) {
			Console.printText("SFDL.net", "(Error): " + e.getMessage(), false);
		}
		
		HttpURLConnection http = (HttpURLConnection)con;
		HttpURLConnection.setFollowRedirects(true);
		try {
			http.setRequestMethod("POST");
		} catch (ProtocolException e) {
			Console.printText("SFDL.net", "(Error): " + e.getMessage(), false);
		}
		http.setDoOutput(true);
		
		// "simulate" click at sfdl net
		Map<String,String> arguments = new HashMap<>();
		arguments.put("download", "true");
		StringJoiner sj = new StringJoiner("&");
		for(Map.Entry<String,String> entry : arguments.entrySet())
			try {
				sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				Console.printText("SFDL.net", "(Error): " + e.getMessage(), false);
			}
		byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
		int length = out.length;
		
		http.setFixedLengthStreamingMode(length);
		http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		try {
			http.connect();
		} catch (IOException e) {
			Console.printText("SFDL.net", "(Error): " + e.getMessage(), false);
		}
		try(OutputStream os = http.getOutputStream()) {
		    os.write(out);
		}
		catch (IOException e) {
			Console.printText("SFDL.net", "(Error): " + e.getMessage(), false);
		}
		
		// get sfdl file name from header
		String SFDLHeaderInfo = http.getHeaderField("Content-Disposition");
		String SFDLFileName = SFDLHeaderInfo.split("filename=")[1];
		
		// download sfdl file
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
	    try {
	        byte[] chunk = new byte[4096];
	        int bytesRead;
	        
	        InputStream stream = con.getInputStream();

	        while ((bytesRead = stream.read(chunk)) > 0) {
	            outputStream.write(chunk, 0, bytesRead);
	        }
	        
	        outputStream.close();
	        
	    } catch (IOException e) {
	    	Console.printText("SFDL.net", "(Error): " + e.getMessage(), false);
	    }
	    
	    http.disconnect();
	    
	    // save sfdl file
	    ByteArrayOutputStream byteArrayOutputStream = outputStream;
	    String tmp = System.getProperty("java.io.tmpdir");
	    String tmpDir = null;
	    if(tmp.endsWith("/") || tmp.endsWith("\\")) {
	    	tmpDir = tmp;
	    } else {
	    	tmpDir = tmp + "/";
	    }
	    
	    try(OutputStream os = new FileOutputStream(tmpDir + SFDLFileName)) {
	        byteArrayOutputStream.writeTo(os);
	    } catch (FileNotFoundException e) {
	    	Console.printText("SFDL.net", "(Error): " + e.getMessage(), false);
		} catch (IOException e) {
			Console.printText("SFDL.net", "(Error): " + e.getMessage(), false);
		}
	    
	    return tmpDir + SFDLFileName;
	}
	
	public static void main(String[] args) {
		
		Options options = new Options();

        Option input = new Option("i", "sfdl", true, "SFDL file path or SFDL.net URL");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "downloadpath", true, "Path to store downloads (default: " + Globals.downloadRootPath + ")");
        output.setRequired(false);
        options.addOption(output);
        
        Option threads = new Option("t", "maxthreads", true, "Max. download threads (default: 3)");
        threads.setRequired(false);
        options.addOption(threads);
        
        Option unrar = new Option("r", "unrar", true, "Do not UnRAR files (default: false)");
        unrar.setRequired(false);
        options.addOption(unrar);
        
        Option delete = new Option("e", "delete", true, "Do not delete after UnRAR (default: false)");
        delete.setRequired(false);
        options.addOption(delete);
        
        Option debug = new Option("d", "debug", true, "Enable debug output (default: false)");
        debug.setRequired(false);
        options.addOption(debug);
        
        Option ansi = new Option("a", "ansi", true, "Disable Ansi color text (default: false)");
        ansi.setRequired(false);
        options.addOption(ansi);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
		
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("jSFDLSauger v" + Globals.appVersion, options);
            System.exit(1);
        }

        String iSFDL = cmd.getOptionValue("sfdl");
        String iDOWNLOADPATH = cmd.getOptionValue("downloadpath");
        int iMAXTHREADS = Integer.parseInt(cmd.getOptionValue("maxthreads"));
        boolean iDEBUG = Boolean.parseBoolean(cmd.getOptionValue("debug"));
        
        if(Boolean.parseBoolean(cmd.getOptionValue("ansi")) == true) {
        	Globals.ansiColors = false;
        }
        
        if(iDEBUG == true) {
        	Globals.debug = true;
        	Debug("########## > Log session start! < ##########");
        }
        
        if(Boolean.parseBoolean(cmd.getOptionValue("unrar")) == true) {
        	Globals.unrarFiles = false;
        }
        
        if(Boolean.parseBoolean(cmd.getOptionValue("delete")) == true) {
        	Globals.deleteAfterUnrar = false;
        }
        
        if(iMAXTHREADS != 0) {
        	Globals.maxThreads = iMAXTHREADS;
        }
        
        // download sfdl from sfdl.net
        if(iSFDL.startsWith("http") && !iSFDL.startsWith("https://download.sfdl.net")) {
        	System.out.println("Only downloads from sfdl.net are supported! Try again ...");
        	System.exit(1);
		}
        
        if(iSFDL.startsWith("https://download.sfdl.net")) {
			
        	AnsiConsole.systemInstall();
			Console.reset();
			Console.printHello();
			Console.printBanner();
			
			String sfdlFile = fromSFDLNet(iSFDL);
			
			SFDL s3 = new SFDL();
			s3.set(sfdlFile);
			s3.start();
			
		} else {
        
	        File sfdlFile = new File(iSFDL);
	        if(!sfdlFile.exists() || sfdlFile.isDirectory()) {
	        	System.out.println("SFDL file does not exist or is a directory! Try again ...");
	        	System.exit(1);
	        }
	        
	        if(!iSFDL.toLowerCase().endsWith(".sfdl")) {
	        	System.out.println("A valid SFDL file ends with .sfdl|.SFDL! Try again ...");
	        	System.exit(1);
	        }
	        
	        File dlPath = new File(iDOWNLOADPATH);
	        if(!iDOWNLOADPATH.isEmpty()) {
	        	if(!dlPath.exists()) {
	        		if(!dlPath.mkdirs()) {
	        			System.out.println("Unable to create download path: " + iDOWNLOADPATH);
	    	        	System.exit(1);
	        		}
	        	}
	        	Globals.downloadRootPath = iDOWNLOADPATH;
	        } else {
	        	System.out.println("Download path does not exists! Create first ...");
	        	System.exit(1);
	        }
	        
	        if(jSFDLSauger.Globals.ansiColors == true) {
	        	AnsiConsole.systemInstall();
	        }
	        
			Console.reset();
			Console.printHello();
			Console.printBanner();
			
			SFDL s3 = new SFDL();
			s3.set(iSFDL);
			s3.start();
		}
	}

}
