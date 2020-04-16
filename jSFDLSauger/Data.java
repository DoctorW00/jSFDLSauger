package jSFDLSauger;

import java.util.ArrayList;

public class Data {
	
	static ArrayList<SrvData> Serverlist = new ArrayList<SrvData>();
	static ArrayList<FileData> Filelist = new ArrayList<FileData>();

	public static class SrvData {

		public String description;
		public String upper;
		public String host;
		public int port;
		public String username;
		public String password;
		public long downloadSize;
		public long downloadProgress;
		public long timeStart;
		public long timePause;
		public long timeEnd;
		public boolean done;

		public SrvData(String description, String upper, String host, int port, String username, String password,
				long downloadSize, long downloadProgress, long timeStart, long timePause, long timeEnd, boolean done) {

			this.description = description;
			this.upper = upper;
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
			this.downloadSize = downloadSize;
			this.downloadProgress = downloadProgress;
			this.timeStart = timeStart;
			this.timePause = timePause;
			this.timeEnd = timeEnd;
			this.done = done;
		}
	}

	// add new server and return new id
	public static int addServer(String description, String upper, String host, int port, String username,
			String password, long downloadSize, long downloadProgress, long timeStart, long timePause, long timeEnd,
			boolean done) {
		
		SrvData newServer = new SrvData(description, upper, host, port, username, password, downloadSize,
				downloadProgress, timeStart, timePause, timeEnd, done);

		Serverlist.add(newServer);

		int newServerID = Serverlist.size();
		if (newServerID > 0) {
			newServerID = newServerID - 1;
		}

		return newServerID;
	}

	public static SrvData getServer(int id) {

		/*
		int ListSize = Serverlist.size();

		if (ListSize <= id || id < 0 || ListSize == 0) {
			String description = "";
			String upper = "";
			String host = "";
			int port = 0;
			String username = "";
			String password = "";
			int downloadSize = 0;
			int downloadProgress = 0;
			long timeStart = 0;
			long timePause = 0;
			long timeEnd = 0;
			boolean done = false;
			
			SrvData newServer = new SrvData(description, upper, host, port, username, password, downloadSize,
					downloadProgress, timeStart, timePause, timeEnd, done);
			
			return newServer;
		}
		*/

		return Serverlist.get(id);
	}
	
	public static ArrayList<SrvData> getServerList() {
		
		ArrayList<SrvData> ReturnList = new ArrayList<SrvData>();
		
		int ListSize = Serverlist.size();
		
		for (int i = 0; i < ListSize; i++) {
			ReturnList.add(Serverlist.get(i));
		}
		
		// return Filelist.get(id);
		return ReturnList;
	}

	public static void setServerDownloadSize(int id, long downloadSize) {

		SrvData updateServer = new SrvData(
				Serverlist.get(id).description,
				Serverlist.get(id).upper,
				Serverlist.get(id).host,
				Serverlist.get(id).port,
				Serverlist.get(id).username,
				Serverlist.get(id).password,
				downloadSize,
				Serverlist.get(id).downloadProgress,
				Serverlist.get(id).timeStart,
				Serverlist.get(id).timePause,
				Serverlist.get(id).timeEnd,
				Serverlist.get(id).done);

		Serverlist.set(id, updateServer);

	}
	
	public static void setServerResume(int id, long progress) {

		boolean isDone = false;
		if(progress == Serverlist.get(id).downloadProgress) {
			isDone = true;
		}
		
		SrvData updateServer = new SrvData(
				Serverlist.get(id).description,
				Serverlist.get(id).upper,
				Serverlist.get(id).host,
				Serverlist.get(id).port,
				Serverlist.get(id).username,
				Serverlist.get(id).password,
				Serverlist.get(id).downloadSize,
				progress,
				Serverlist.get(id).timeStart,
				Serverlist.get(id).timePause,
				Serverlist.get(id).timeEnd,
				isDone);

		Serverlist.set(id, updateServer);

	}

	public static class FileData {

		public int id;
		public String filename;
		public String remoteFilePath;
		public long fileSizeTotal;
		public long fileSizeDownloaded;
		public long lastProgressUpdate;
		public long startTime;
		public long lastTime;
		public boolean done;

		public FileData(int id, String filename, String remoteFilePath, long fileSizeTotal, long fileSizeDownloaded, long lastProgressUpdate,
				long startTime, long lastTime, boolean done) {

			this.id = id;
			this.filename = filename;
			this.remoteFilePath = remoteFilePath;
			this.fileSizeTotal = fileSizeTotal;
			this.fileSizeDownloaded = fileSizeDownloaded;
			this.lastProgressUpdate = lastProgressUpdate;
			this.startTime = startTime;
			this.lastTime = lastTime;
			this.done = done;
		}
	}

	// add new file
	public static void addFile(int id, String filename, String remoteFilePath, long fileSizeTotal,
			long fileSizeDownloaded, long lastProgressUpdate, long startTime, long lastTime, boolean done) {
		FileData newFile = new FileData(id, filename, remoteFilePath, fileSizeTotal, fileSizeDownloaded, lastProgressUpdate, startTime, lastTime, done);
		Filelist.add(newFile);
	}
	
	// get file data by id
	// public static FileData getFile(int id) {
	public static ArrayList<FileData> getFileList(int id) {
		
		ArrayList<FileData> ReturnList = new ArrayList<FileData>();
		
		int ListSize = Filelist.size();
		
		for (int i = 0; i < ListSize; i++) {
			if(Filelist.get(i).id == id) {
				ReturnList.add(Filelist.get(i));
			}
		}
		
		// return Filelist.get(id);
		return ReturnList;
	}
	
	public static int fileListZize(int id) {
		
		ArrayList<FileData> ReturnList = new ArrayList<FileData>();
		
		int ListSize = Filelist.size();
		
		for (int i = 0; i < ListSize; i++) {
			if(Filelist.get(i).id == id) {
				ReturnList.add(Filelist.get(i));
			}
		}
		
		return ReturnList.size();
	}

	public static void updateFileDownloadProgressByID(int id, long progress) {
		
		// download done?
		boolean isDone = false;
		if (Filelist.get(id).fileSizeTotal == progress) {
			isDone = true;
		}
		
		long lastProgress = 0;
		if (Filelist.get(id).lastProgressUpdate == 0) {
			lastProgress = progress;
		} else {
			lastProgress = Filelist.get(id).fileSizeDownloaded;
		}
		
		long startTime = 0;
		long unixTimestamp = System.currentTimeMillis() / 1000L;
		
		if (Filelist.get(id).startTime == 0) {
			startTime = unixTimestamp;
		} else {
			startTime = Filelist.get(id).startTime;
		}
		
		// set new file data
		FileData updateFile = new FileData(
				Filelist.get(id).id,
				Filelist.get(id).filename,
				Filelist.get(id).remoteFilePath,
				Filelist.get(id).fileSizeTotal,
				progress,
				lastProgress,
				startTime,
				unixTimestamp,
				isDone);

		// update file data
		Filelist.set(id, updateFile);
		
		long serverStartTime = 0;
		if (Serverlist.get(Filelist.get(id).id).timeStart == 0) {
			serverStartTime = unixTimestamp;
		} else {
			serverStartTime = Serverlist.get(Filelist.get(id).id).timeStart;
		}
		
		long serverDownloadProgress = 0;
		if (Serverlist.get(Filelist.get(id).id).downloadProgress == 0) {
			serverDownloadProgress = progress;
		} else {
			serverDownloadProgress = (Serverlist.get(Filelist.get(id).id).downloadProgress - lastProgress) + progress;
		}
		
		boolean isDownloadDone = false;
		if(serverDownloadProgress == Serverlist.get(Filelist.get(id).id).downloadSize) {
			isDownloadDone = true;
		}
		
		// update server data
		SrvData updateServer = new SrvData(
				Serverlist.get(Filelist.get(id).id).description,
				Serverlist.get(Filelist.get(id).id).upper,
				Serverlist.get(Filelist.get(id).id).host,
				Serverlist.get(Filelist.get(id).id).port,
				Serverlist.get(Filelist.get(id).id).username,
				Serverlist.get(Filelist.get(id).id).password,
				Serverlist.get(Filelist.get(id).id).downloadSize,
				serverDownloadProgress,
				serverStartTime,
				Serverlist.get(Filelist.get(id).id).timePause,
				Serverlist.get(Filelist.get(id).id).timeEnd,
				isDownloadDone);

		Serverlist.set(Filelist.get(id).id, updateServer);
	}
	
	public static void updateFileResumeByID(int id, long progress) {
		// download done?
		boolean isDone = false;
		if (Filelist.get(id).fileSizeTotal == progress) {
			isDone = true;
		}
		
		// set new file data
		FileData updateFile = new FileData(
				Filelist.get(id).id,
				Filelist.get(id).filename,
				Filelist.get(id).remoteFilePath,
				Filelist.get(id).fileSizeTotal,
				0,
				progress,
				0,
				0,
				isDone);

		// update file data
		Filelist.set(id, updateFile);
	}
	
	public static void fileDoneByID(int id) {
		// set new file data
		FileData updateFile = new FileData(
				Filelist.get(id).id,
				Filelist.get(id).filename,
				Filelist.get(id).remoteFilePath,
				Filelist.get(id).fileSizeTotal,
				Filelist.get(id).fileSizeTotal,
				Filelist.get(id).fileSizeTotal,
				0,
				0,
				true);

		// update file data
		Filelist.set(id, updateFile);
	}

}
