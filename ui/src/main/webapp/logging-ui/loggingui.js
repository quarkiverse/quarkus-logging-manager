var webSocket;
var messages = document.getElementById("messages");
var loggersUrl = "/loggers";
var tab = "&nbsp;&nbsp;&nbsp;&nbsp";
var started = false;
var logScrolling = false;
        
$('document').ready(function () {
    $("#logTerminal").hide();
    $("#menuPlayIcon").hide();
    $("#menuStopIcon").hide();
    $("#menuTrashIcon").hide();
    openSocket();

    // Make sure we stop the connection when the browser close
    window.onbeforeunload = function () {
        closeSocket();
    };
    
    populateLoggerScreen();
    $('table').tablesort();
    $('.ui.dropdown').dropdown({
        onChange: function(val, text) {
            changeLogLevel(val,text);
        }
    });
    
    // Add listener to stop
    var ctrlDown = false,
        ctrlKey = 17,
        cmdKey = 91,
        cKey = 67;
    
    $(document).keydown(function(e) {
        if (e.keyCode === ctrlKey || e.keyCode === cmdKey) ctrlDown = true;
    }).keyup(function(e) {
        if (e.keyCode === ctrlKey || e.keyCode === cmdKey) ctrlDown = false;
    });
    
    $(document).keydown(function(e) {
        if (ctrlDown && (e.keyCode === cKey)) stopLog();
    });
    
    
});

function populateLoggerScreen(){
    // Get all logger names 
    
    var loggerNames = httpGet(loggersUrl);
    var loggerNamesArray = JSON.parse(loggerNames);
    
    var levelsUrl = loggersUrl + "/levels";
    var levelNames = httpGet(levelsUrl);
    var levelNamesArray = JSON.parse(levelNames);
    
    var tbodyLevels = $('#tbodyLevels');
    
    // Populate the dropdown
    for (var i = 0; i < loggerNamesArray.length; i++) {
        var row = "<tr><td>" + loggerNamesArray[i].name + "</td><td>" + createDropdown(loggerNamesArray[i].name, loggerNamesArray[i].effectiveLevel,levelNamesArray) + "</td></tr>";
        tbodyLevels.append(row);
    }
}

function changeLogLevel(val,text){
    var xhr = new XMLHttpRequest();
    xhr.open("POST", loggersUrl, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    var data = JSON.stringify({"name": val, "configuredLevel": text});
    xhr.send(data);
}

function createDropdown(name, level, levelNamesArray){
    var dd = "<div class='ui dropdown'>\n\
                <div class='text'>" + level + "</div><i class='dropdown icon'></i>\n\
                <div class='menu'>";
    // Populate the dropdown
    for (var i = 0; i < levelNamesArray.length; i++) {
        dd = dd + "<div class='item' data-value='" + name + "'>" + levelNamesArray[i] +"</div>";
    }
    
    dd = dd + "</div></div>";
    
    return dd;
}

function openSocket() {
    // Ensures only one connection is open at a time
    if (webSocket !== undefined && webSocket.readyState !== WebSocket.CLOSED) {
        writeResponse("Already connected...");
        return;
    }
    // Create a new instance of the websocket
    var loc = window.location, new_uri;
    if (loc.protocol === "https:") {
        new_uri = "wss:";
    } else {
        new_uri = "ws:";
    }
    new_uri += "//" + loc.host;
    new_uri += "/logstream"; // TODO: configure this ?
    webSocket = new WebSocket(new_uri);

    webSocket.onopen = function (event) {
        // For reasons I can't determine, onopen gets called twice
        // and the first time event.data is undefined.
        // Leave a comment if you know the answer.
        if (event.data === undefined)
            return;

        writeResponse(event.data);
    };

    webSocket.onmessage = function (event) {
        var json = JSON.parse(event.data);
        messageLog(json);
    };

    webSocket.onclose = function () {
        if(started){
            stopLog();
        }
        writeResponse("Connection closed<br/>");
    };

    function messageLog(json) {
        var timestamp = new Date(json.timestamp);
        var timestring = timestamp.toLocaleTimeString();
        var datestring = timestamp.toLocaleDateString();
        var level = json.level;
        
        writeResponse(getLevelIcon(level) + tab 
                + datestring + tab 
                + timestring + tab 
                + getLevelText(level) + tab 
                + getClassName(json.sourceClassNameFull, json.sourceClassNameFullShort, json.sourceMethodName) + tab
                + getThread(json.threadName,json.threadId) + tab
                + json.message + "</br>");
                            

        if (json.stacktrace) {
            for (var i in json.stacktrace) {
                var stacktrace = enhanceStacktrace(json.loggerName, json.stacktrace[i]);
                writeResponse(stacktrace);
            }
        }
        $('.ui.dark')
          .popup()
        ;
    }
}

function getLevelIcon(level){
    level = level.toUpperCase();
    if (level === "WARNING" || level === "WARN")
        return "<i class='yellow exclamation triangle icon'></i>";
    if (level === "SEVERE" || level === "ERROR")
        return "<i class='red exclamation circle icon'></i>";
    if (level === "INFO")
        return "<i class='blue info circle icon'></i>";
    if (level === "DEBUG")
        return "<i class='grey bug icon'></i>";
    
    return "<i class='black circle icon'></i>";    
}

function getLevelText(level){
    level = level.toUpperCase();
    if (level === "WARNING" || level === "WARN")
        return "<span class='ui inverted orange text'>WARN </span>";
    if (level === "SEVERE" || level === "ERROR")
        return "<span class='ui inverted red text'>ERROR</span>";
    if (level === "INFO")
        return "<span class='ui inverted blue text'>INFO </span>";
    if (level === "DEBUG")
        return "<span class='ui inverted dark grey text'>DEBUG</span>";
    
    return level;    
}

function getClassName(sourceClassNameFull, sourceClassNameFullShort, sourceMethodName){
    return "<span class='ui dark blue text' title='" + sourceClassNameFull + "'>[" + sourceClassNameFullShort + "]</span> " + sourceMethodName;
}

function getThread(threadName, threadId){
    return "<span class='ui dark green text' title='Thread Id: " + threadId + "'>(" + threadName + ")</span>";
}

function showLevels(){
    $("#menuLevels").addClass("active");
    $("#menuLog").removeClass("active");
    $("#segmentLevels").show();
    $("#logTerminal").hide();
    $("#menuPlayIcon").hide();
    $("#menuStopIcon").hide();
    $("#menuTrashIcon").hide();
    logScrolling = false;
    var element = document.getElementById("segmentLevels");
    element.scrollIntoView();
}

function showLog(){
    $("#menuLog").addClass("active");
    $("#menuLevels").removeClass("active");
    $("#logTerminal").show();
    $("#menuPlayIcon").show();
    $("#menuStopIcon").show();
    $("#menuTrashIcon").show();
    $("#segmentLevels").hide();
    logScrolling = true;
}

function closeSocket() {
    webSocket.close();
}

function startLog() {
    $("#menuPlayIcon").addClass("disabled");
    $("#menuPlayIcon").prop("disabled", true);
    webSocket.send("start");
    started = true;
    $("#menuStopIcon").removeClass("disabled");
    $("#menuStopIcon").prop("disabled", false);
}

function stopLog() {
    started = false;
    writeResponse("^C</br>");
    $("#menuStopIcon").addClass("disabled");
    $("#menuStopIcon").prop("disabled", true);
    webSocket.send("stop");
    $("#menuPlayIcon").removeClass("disabled");
    $("#menuPlayIcon").prop("disabled", false);
}

function clearScreen() {
    segmentLog.innerHTML = "";
}

function getClassLogLevel(level) {

    if (level === "WARNING" || level === "WARN")
        return "warning";
    if (level === "SEVERE" || level === "ERROR")
        return "error";
    if (level === "INFO")
        return "positive";
    if (level === "FINE")
        return "blue";
    if (level === "FINER")
        return "blue"; // TODO: Find better colors
    if (level === "FINEST")
        return "blue"; // TODO: Find better colors
    return level;
}

function getLogLevelColor(level) {
    
    if (level === "WARNING" || level === "WARN")
        return "orange";
    if (level === "SEVERE" || level === "ERROR")
        return "red";
    if (level === "INFO")
        return "green";
    if (level === "FINE")
        return "teal";
    if (level === "FINER")
        return "blue";
    if (level === "FINEST")
        return "violet";
    return level;
}

function enhanceStacktrace(loggerName, stacktrace) {
    var enhanceStacktrace = [];
    var lines = stacktrace.split('\n');
    for (var i = 0; i < lines.length; i++) {
        var line = lines[i].trim();
        if (line) {
            var startWithAt = line.startsWith("at ");
            if (!startWithAt) {
                var parts = line.split(":");
                line = "<b>" + parts[0] + ":</b>" + parts[1];
            }else{
                var isMyClass = line.includes(loggerName);
                if (isMyClass && loggerName){
                    line = '<b>' + line + '</b>';
                }
                line = tab + tab + line;
            }
            
        }

        enhanceStacktrace.push(line + '<br/>');
    }
    var newStacktrace = enhanceStacktrace.join('');
    return "<span class='red text'>" + newStacktrace + "</span>";
}

function toggleException(sequenceNumber) {
    var element = document.getElementById(sequenceNumber);
    var result_style = element.style;

    if (result_style.display === '') {
        result_style.display = "none";
    } else {
        result_style.display = '';
    }
}

function writeResponse(text) {
    segmentLog.innerHTML += text;
    if(logScrolling){
        var element = document.getElementById("logTerminal");
        element.scrollIntoView({block: "end"});
    }
}

function httpGet(theUrl) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open("GET", theUrl, false); // false for synchronous request
    xmlHttp.send(null);
    return xmlHttp.responseText;
}