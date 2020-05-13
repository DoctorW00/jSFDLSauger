package jSFDLSauger;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;

import jSFDLSauger.Data.FileData;
import jSFDLSauger.Data.SrvData;
import jSFDLSauger.Data.SysMSG;

public class Worker {

	// job counter
	// private static int runningJobs = jSFDLSauger.Globals.runningJobs;
	private static int maxJobssimultaneouslyAllowed = jSFDLSauger.Globals.maxJobs;
	private static int maxSimultaneousAllowedThreads = jSFDLSauger.Globals.maxThreads;
	
	// timer
	public static Timer timer;
	public static Console con;
	
	// job list
	private static ArrayList<JobData> JobList = new ArrayList<JobData>();
	
	// some job data
	public static class JobData {
		public int jobID; // == SrvData ID
		public boolean isRunning = false;
		public boolean done = false;
		
		public JobData(int jobID, boolean isRunning, boolean done) {
			this.jobID = jobID;
			this.isRunning = isRunning;
			this.done = done;
		}
	}
	
	// add new job
	public static int addJob(int jobID, boolean isRunning, boolean done) {

		JobData newJob = new JobData(jobID, isRunning, done);

		JobList.add(newJob);

		int newJobID = JobList.size();
		if (newJobID > 0) {
			newJobID = newJobID - 1;
		}

		return newJobID;
	}
	
	private static void updateRunningJobs() {
		jSFDLSauger.Globals.runningJobs = 0;
		for(int i = 0; i < JobList.size(); i++) {
			if(JobList.get(i).isRunning) {
				jSFDLSauger.Globals.runningJobs++;
			}
		}
	}
	
	public static void resetJobs() {
		JobList.clear();
	}
	
	private static void startTimer(int ServerID, int time) {
		timer = new Timer();
		con = new Console();
		con.set(ServerID);
		timer.schedule(con, 0, time);
	}
	
	public static void stopTimer() {
		timer.cancel();
		timer.purge();
	}
	
	public static String JSONMessages() {
		
		ArrayList<SysMSG> MSGList = new ArrayList<SysMSG>();
		MSGList = Data.getAllMessages();
		
		Gson gson = new Gson();
		String json = gson.toJson(MSGList);
		
		return json;
	}
	
	public static String JSONServer() {
		
		ArrayList<SrvData> Serverlist = new ArrayList<SrvData>();
		Serverlist = Data.getServerList();
		
		Gson gson = new Gson();
		String json = gson.toJson(Serverlist);
		
		return json;
	}
	
	public static String JSONFiles() {
		
		ArrayList<FileData> Filelist = new ArrayList<FileData>();
		Filelist = Data.getFileList(0);
		
		Gson gson = new Gson();
		String json = gson.toJson(Filelist);
		
		return json;
	}
	
	public static String JSONSFDLFiles() {
		
		File folder = new File(jSFDLSauger.Globals.sfdlFilePath);
		
		if(!folder.exists() || folder.isFile()) {
			return "{}";
		}
		
		File[] listOfFiles = folder.listFiles();
		
		ArrayList<String> sfdlFiles = new ArrayList<String>();
		
		for(int i = 0; i < listOfFiles.length; i++) {
			if(listOfFiles[i].isFile()) {
				sfdlFiles.add(listOfFiles[i].getName());
			}
		}
		
		Gson gson = new Gson();
		String json = gson.toJson(listOfFiles);
		
		return json;
	}
	
	public static void startJob() {
		
		updateRunningJobs();
		
		if(jSFDLSauger.Globals.runningJobs >= maxJobssimultaneouslyAllowed) {
			return;
		}
		
		int ServerID = 0;
		
		ExecutorService executor = Executors.newFixedThreadPool(maxSimultaneousAllowedThreads);
		
		Console.printText("jSFDLSauger", "Downloading ...", false);
		
		for(int i = 0; i < JobList.size(); i++) {
			
			if(jSFDLSauger.Globals.runningJobs > maxJobssimultaneouslyAllowed) {
				break;
			}
			
			ServerID = i;
			
			JobData updateJob = new JobData(i, true, false);
			JobList.set(i, updateJob);
			
			// System.out.println("starting new job ... " + JobList.get(i).jobID);
			jSFDLSauger.Globals.runningJobs++;
			
			ArrayList<FileData> Filelist = new ArrayList<FileData>();
			Filelist = Data.getFileList(i);
			
			// check for existing files first
			// and set amount of downloaded size as progress
			long totalProgress = 0;
			for(int k = 0; k < Filelist.size(); k++) {
				
				String filePath = Filelist.get(k).remoteFilePath;
				String fileNameDwonload;
				String[] fileSplit = filePath.split(Data.getServer(i).description);
				if(fileSplit.length == 2) {
					fileNameDwonload = fileSplit[1];
				} else {
					fileNameDwonload = Filelist.get(k).filename;
				}
				
				String dlFile = jSFDLSauger.Globals.downloadRootPath + "/" + Data.getServer(i).description + "/" + fileNameDwonload;
				
				File f = new File(dlFile);
				if(f.exists()) {
					
					System.out.println("file exist!: " + dlFile);
					System.out.println("file length: " + f.length());
					
					totalProgress += f.length();
					Data.updateFileResumeByID(k, f.length());
				}
			}
			
			if(totalProgress != 0) {
				System.out.println("totalProgress: " + totalProgress);
				Data.setServerResume(i, totalProgress);
			}
			
			for(int j = 0; j < Filelist.size(); j++) {
				
				String filePath = Filelist.get(j).remoteFilePath;
				String fileNameDwonload;
				String[] fileSplit = filePath.split(Data.getServer(i).description);
				if(fileSplit.length == 2) {
					fileNameDwonload = fileSplit[1];
				} else {
					fileNameDwonload = Filelist.get(j).filename;
				}
				
				Download dl = new Download();
				dl.setName(Filelist.get(j).filename);
				
				dl.set(Data.getServer(i).host,
						Data.getServer(i).port,
						Data.getServer(i).username,
						Data.getServer(i).password,
						filePath,
						jSFDLSauger.Globals.downloadRootPath + "/" + Data.getServer(i).description + "/" + fileNameDwonload,
						j);
				
				executor.execute(dl);
			}
		}
		
		executor.shutdown();
		
		// Timer timer = new Timer();
		// Console con = new Console();
		// con.set(ServerID);
		// timer.schedule(con, 0, 1000);
		startTimer(ServerID, 1000);
	}	
}
