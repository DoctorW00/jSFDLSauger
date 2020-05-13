function humanFileSize(bytes, si) {
    var thresh = si ? 1000 : 1024;
    if(Math.abs(bytes) < thresh) {
        return bytes + ' B';
    }
    var units = si
        ? ['kB','MB','GB','TB','PB','EB','ZB','YB']
        : ['KiB','MiB','GiB','TiB','PiB','EiB','ZiB','YiB'];
    var u = -1;
    do {
        bytes /= thresh;
        ++u;
    } while(Math.abs(bytes) >= thresh && u < units.length - 1);
    return bytes.toFixed(1)+' '+units[u];
}

updateUploadList = function() {
	var input = document.getElementById('file');
	var output = document.getElementById('sfdlFiles');
	var children = "";
	for (var i = 0; i < input.files.length; ++i) {
		children += '<div style="padding: 5px; background-color: #f7ff9e; border: 1px solid black; margin: 0 0 2px; 0">' + input.files.item(i).name + '</div>';
	}
	output.innerHTML = '<div>'+children+'</div>';
}

function startDownload(file) {
	var xhr = new XMLHttpRequest();
	xhr.open('GET', '/start&sfdl=' + file, true);
	xhr.responseType = 'json';
	xhr.send();
}

var getJSON = function(url, callback) {
	var xhr = new XMLHttpRequest();
	xhr.open('GET', url, true);
	xhr.responseType = 'json';
	xhr.onload = function() {
		var status = xhr.status;
		if (status === 200) {
			callback(null, xhr.response);
		} else {
			callback(status, xhr.response);
		}
	};
	xhr.send();
};

function updateData() {
	// log messages
	getJSON('/messages.json', function(err, data) {
		if (err !== null) {
			console.log("JSON error: " + err);
		} else {
			try {
				var messages = JSON.parse(JSON.stringify(data));
				
				document.getElementById("sauger-log").innerHTML = "";
				
				for(i = 0; i < messages.length; i++) {
					var msg = document.createElement("LI");
					var t = document.createTextNode(messages[i].title + " > " + messages[i].text);
					msg.appendChild(t);
					document.getElementById("sauger-log").appendChild(msg);
				}
				
				var objDiv = document.getElementById("sauger-log");
				objDiv.scrollTop = objDiv.scrollHeight;
				
			} catch (e) {
				console.log("JSON parse error: " + e.message);
			}
		}
	});
	
	// sfdl files
	getJSON('/sfdl.json', function(err, data) {
		if (err !== null) {
			console.log("JSON error: " + err);
		} else {
			try {
				var messages = JSON.parse(JSON.stringify(data));
				
				document.getElementById("sauger-sfdl").innerHTML = "";
				
				for(i = 0; i < messages.length; i++) {
					var startLink = document.createElement("A");
					startLink.innerHTML = messages[i].path;
					startLink.href = "#";
					startLink.id = "link_" + i;
					startLink.name = "link_" + i;
					startLink.onclick = function() { startDownload(this.innerHTML); return false; };
					
					var ele = document.createElement("LI");
					ele.appendChild(startLink);
					
					document.getElementById("sauger-sfdl").appendChild(ele);
				}
				
				
			} catch (e) {
				console.log("JSON parse error: " + e.message);
			}
		}
	});
	
	// server data
	getJSON('/server.json', function(err, data) {
		if (err !== null) {
			console.log("JSON error: " + err);
		} else {
			try {
				var messages = JSON.parse(JSON.stringify(data));
				
				document.getElementById("sauger-server").innerHTML = "";
				
				for(i = 0; i < messages.length; i++) {
					var description = document.createElement("TR");
					description.insertAdjacentHTML("beforeend", "<td>Description:</td>");
					description.insertAdjacentHTML("beforeend", "<td>" + messages[i].description + "</td>");
					document.getElementById("sauger-server").appendChild(description);
					
					var upper = document.createElement("TR");
					upper.insertAdjacentHTML("beforeend", "<td>Upper:</td>");
					upper.insertAdjacentHTML("beforeend", "<td>" + messages[i].upper + "</td>");
					document.getElementById("sauger-server").appendChild(upper);
					
					var download = document.createElement("TR");
					download.insertAdjacentHTML("beforeend", "<td>Download:</td>");
					download.insertAdjacentHTML("beforeend", "<td>" + humanFileSize(messages[i].downloadSize, true) + " <progress max=\"" + messages[i].downloadSize + "\" value=\"" + messages[i].downloadProgress + "\"></progress> " + humanFileSize(messages[i].downloadProgress, true) + "</td>");
					document.getElementById("sauger-server").appendChild(download);
				}
			} catch (e) {
				console.log("JSON parse error: " + e.message);
			}
		}
	});
	
	// file data & status updates
	getJSON('/files.json', function(err, data) {
		if (err !== null) {
			console.log("JSON error: " + err);
		} else {
			try {
				var messages = JSON.parse(JSON.stringify(data));
				
				document.getElementById("sauger-files").innerHTML = "";
				
				var newth = document.createElement("TR");
				newth.insertAdjacentHTML("beforeend", "<th>Filename</th>");
				newth.insertAdjacentHTML("beforeend", "<th>Size</th>");
				newth.insertAdjacentHTML("beforeend", "<th>Progress</th>");
				newth.insertAdjacentHTML("beforeend", "<th>Loaded</th>");
				document.getElementById("sauger-files").appendChild(newth);
				
				for(i = 0; i < messages.length; i++) {
					var newtr = document.createElement("TR");
					var sum = messages[i].fileSizeDownloaded + messages[i].resume;
					newtr.insertAdjacentHTML("beforeend", "<td>" + messages[i].filename + "</td>");
					newtr.insertAdjacentHTML("beforeend", "<td>" + humanFileSize(messages[i].fileSizeTotal, true) + "</td>");
					newtr.insertAdjacentHTML("beforeend", "<td>" + "<progress max=\"" + messages[i].fileSizeTotal + "\" value=\"" + sum + "\"></progress></td>");
					newtr.insertAdjacentHTML("beforeend", "<td>" + humanFileSize(messages[i].fileSizeDownloaded, true) + "</td>");
					document.getElementById("sauger-files").appendChild(newtr);
				}
			} catch (e) {
				console.log("JSON parse error: " + e.message);
			}
		}
	});
}

setInterval(updateData, 1000);

function uploadSFDLFiles() {
	var sfdl = document.getElementById("file").files;
	
	let req = new XMLHttpRequest();
	let formData = new FormData();

	for(var i = 0; i < sfdl.length; i++) {
		formData.append("sfdl" + i, sfdl[i]);
	}
	
	req.open("POST", '/upload');
	req.send(formData);
	
	document.getElementById("sfdlFiles").innerHTML = "";
	document.getElementById("sfdlForm").reset();
}
	