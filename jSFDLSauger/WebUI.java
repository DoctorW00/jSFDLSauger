package jSFDLSauger;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.fusesource.jansi.AnsiConsole;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class WebUI extends NanoHTTPD {
	
	public WebUI() {
		
		super(jSFDLSauger.Globals.webuiPort);
		
		if(jSFDLSauger.Globals.ansiColors == true) {
    		AnsiConsole.systemInstall();
    	}
		
		Console.reset();
		Console.printHello();
		Console.printBanner();
		
		try {
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
			Console.printText("WebUI", "Starting WebUI ... http://localhost:" + jSFDLSauger.Globals.webuiPort, false);
			
		} catch (IOException e) {
			Console.printText("WebUI", "(Error): " + e.getMessage(), false);
			// System.exit(1);
			this.stop();
		}
		
		// webui url
		URL webuiURL = null;
		try {
			webuiURL = new URL("http://localhost:" + jSFDLSauger.Globals.webuiPort);
		} catch (MalformedURLException e1) {
			Console.printText("WebUI", "(Error): " + e1.getMessage(), false);
		}
		
		// open webui in browser if we got a desktop
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(webuiURL.toURI());
	        } catch (Exception e) {
	        	Console.printText("WebUI", "(Error): " + e.getMessage(), false);
	        }
	    }
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		
		Response response = null;
	    String Endpoint = session.getUri();
	    
	    if(Endpoint.equals("/messages.json")) {
	    	response = newFixedLengthResponse(Status.OK, "application/json", Worker.JSONMessages());
    		return response;
	    }
	    
	    if(Endpoint.equals("/sfdl.json")) {
	    	response = newFixedLengthResponse(Status.OK, "application/json", Worker.JSONSFDLFiles());
    		return response;
	    }
	    
	    if(Endpoint.equals("/server.json")) {
	    	response = newFixedLengthResponse(Status.OK, "application/json", Worker.JSONServer());
    		return response;
	    }
	    
	    if(Endpoint.equals("/files.json")) {
	    	response = newFixedLengthResponse(Status.OK, "application/json", Worker.JSONFiles());
    		return response;
	    }
	    
	    if(Endpoint.startsWith("/start")) {
	    	String sfdlFile = Endpoint.split("sfdl=")[1];
	    	
	    	if(jSFDLSauger.Globals.runningJobs == 0) {
		    	if(sfdlFile != null) {
		    		SFDL s3 = new SFDL();
					s3.set(sfdlFile);
					s3.start();
		    	}
		    	
	    		response = newFixedLengthResponse(Status.OK, "application/json", "{'status':'ok'}");
	    	} else {
	    		response = newFixedLengthResponse(Status.OK, "application/json", "{'status':'fail', 'msg':'There is already an aktive download running!'}");
	    	}
	    	
    		return response;
	    }
	    
	    if(Endpoint.equals("/upload")) {
    		Map<String, String> files = new HashMap<String, String>();
	        try {
				session.parseBody(files);
			} catch (IOException | ResponseException e1) {
				System.out.println("parseBody error: " + e1.getMessage());
				
				response = newFixedLengthResponse(Status.OK, MIME_PLAINTEXT, "Parse body error!");
			    return response;	
			}
    		
    	    @SuppressWarnings("deprecation")
			Map<String, String> parms = session.getParms();
	        
	        Set<String> keys = files.keySet();
	        Iterator<String> sfdl = parms.values().iterator();
	        for(String key: keys) {
	            String name = sfdl.next();
	            String loaction = files.get(key);
	            try {
	            	File tempfile = new File(loaction);
	            	File destFile =  new File(jSFDLSauger.Globals.sfdlFilePath + "/" + name);
	            	destFile.getParentFile().mkdirs();
	            	Files.copy(tempfile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	            	Console.printText("SFDL", "File upload " + name + " was successful!", false);
	            } catch (Exception e) {
	                Console.printText("SFDL", "(Error) File upload: " + e.getMessage(), false);
	            }	
	        }
		    
		    response = newFixedLengthResponse(Status.OK, MIME_PLAINTEXT, "OK!");
		    return response;
	    }
	    
	    if(Endpoint.equals("/")) {
	    	Endpoint = "/index.html";
	    }
	    
	    String mimetype = getMimeTypeForFile(Endpoint);
	    
	    InputStream res = null;
	    res = jSFDLSauger.class.getResourceAsStream("/webui" + Endpoint);
    	if(res == null) {
    		response = newFixedLengthResponse(Status.NOT_FOUND, MIME_HTML, "404 Not Found");
    		return response;
		}
	    
    	try {
			response = newFixedLengthResponse(Status.OK, mimetype, res, res.available());
		} catch (IOException e) {
			Console.printText("WebUI", "(Error): " + e.getMessage(), false);
		}
	    
    	if(response == null) {
    		response = newFixedLengthResponse(Status.NOT_FOUND, MIME_HTML, "404 Not Found");
    	}
    	
	    return response;
	}
}
