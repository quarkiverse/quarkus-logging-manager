var zoom = 0.9;
var increment = 0.05;

var webSocket;
var messages = document.getElementById("messages");
var tab = "&nbsp;&nbsp;&nbsp;&nbsp";

var isRunning = true;
var logScrolling = true;

var loggersUrl = "/loggers";

$('document').ready(function () {
    openSocket();
    // Make sure we stop the connection when the browser close
    window.onbeforeunload = function () {
        closeSocket();
    };

    stopStartButton.addEventListener("click", stopStartEvent);
    clearLogButton.addEventListener("click", clearScreenEvent);
    zoomOutButton.addEventListener("click", zoomOutEvent);
    zoomInButton.addEventListener("click", zoomInEvent);
//    filterButton.addEventListener("click", filterEvent);
    followLogButton.addEventListener("click", followLogEvent);
    
    populateLoggerLevelModal();
    
    // Add listener to stop
    var ctrlDown = false,
            ctrlKey = 17,
            cmdKey = 91,
            cKey = 67;

    $(document).keydown(function (e) {
        if (e.keyCode === ctrlKey || e.keyCode === cmdKey)
            ctrlDown = true;
    }).keyup(function (e) {
        if (e.keyCode === ctrlKey || e.keyCode === cmdKey)
            ctrlDown = false;
    });

    $(document).keydown(function (e) {
        if (ctrlDown && (e.keyCode === cKey))stopLog();
    });
    
    $('[data-toggle="tooltip"]').tooltip();
});

function stopStartEvent() {
    if (isRunning) {
        stopLog();
    } else {
        startLog();
    }
}

function stopLog() {
    webSocket.send("stop");
    writeResponse("^C</br>");

    stopStartButton.innerHTML = "<i class='fas fa-play'></i>";
    $("#followLogIcon").hide();
    isRunning = false;
}

function startLog() {
    webSocket.send("start");

    stopStartButton.innerHTML = "<i class='fas fa-stop'></i>";
    $("#followLogIcon").show();
    isRunning = true;
}

function clearScreenEvent() {
    segmentLog.innerHTML = "";
}

function zoomOutEvent() {
    zoom = zoom - increment;
    $('#segmentLog').css("font-size", zoom + "em");
}

function zoomInEvent() {
    zoom = zoom + increment;
    $('#segmentLog').css("font-size", zoom + "em");
}

//function filterEvent() {
//    console.log("Popup filter modal");
//}

function followLogEvent() {
    if (logScrolling) {
        logScrolling = false;
        $("#followLogIcon").removeClass("text-success");
        $("#followLogIcon").removeClass("fa-spin");
    } else {
        logScrolling = true;
        $("#followLogIcon").addClass("text-success");
        $("#followLogIcon").addClass("fa-spin");
    }
}

function scrollToTop() {
    logScrolling = false;
}

function scrollToBottom() {
    logScrolling = true;
}

function writeResponse(text) {
    segmentLog.innerHTML += text;
    if (logScrolling) {
        var element = document.getElementById("logTerminal");
        element.scrollIntoView({block: "end"});
    }
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
        if (isRunning) {
            stopLog();
            writeResponse("Connection closed<br/>");
        }
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
                + getThread(json.threadName, json.threadId) + tab
                + json.formattedMessage + "</br>");


        if (json.stacktrace) {
            for (var i in json.stacktrace) {
                var stacktrace = enhanceStacktrace(json.loggerName, json.stacktrace[i]);
                writeResponse(stacktrace);
            }
        }
    }
}

function getLevelIcon(level) {
    level = level.toUpperCase();
    if (level === "WARNING" || level === "WARN")
        return "<i class='levelicon text-warning fas fa-exclamation-triangle'></i>";
    if (level === "SEVERE" || level === "ERROR")
        return "<i class='levelicon text-danger fas fa-radiation'></i>";
    if (level === "INFO")
        return "<i class='levelicon text-primary fas fa-info-circle'></i>";
    if (level === "DEBUG")
        return "<i class='levelicon text-secondary fas fa-bug'></i>";

    return "<i class='levelicon fas fa-circle'></i>";
}

function getLevelText(level) {
    level = level.toUpperCase();
    if (level === "WARNING" || level === "WARN")
        return "<span class='text-warning'>WARN </span>";
    if (level === "SEVERE" || level === "ERROR")
        return "<span class='text-danger'>ERROR</span>";
    if (level === "INFO")
        return "<span class='text-primary'>INFO </span>";
    if (level === "DEBUG")
        return "<span class='text-secondary'>DEBUG</span>";

    return level;
}

function getTextClass(level){
    level = level.toUpperCase();
    if (level === "WARNING" || level === "WARN")
        return "text-warning";
    if (level === "SEVERE" || level === "ERROR")
        return "text-danger";
    if (level === "INFO")
        return "text-primary";
    if (level === "DEBUG")
        return "text-secondary";

    return "";
}

function getClassName(sourceClassNameFull, sourceClassNameFullShort, sourceMethodName) {
    return "<span class='text-primary' data-toggle='tooltip' data-placement='top' title='" + sourceClassNameFull + "'>[" + sourceClassNameFullShort + "]</span> " + sourceMethodName;
}

function getThread(threadName, threadId) {
    return "<span class='text-success' data-toggle='tooltip' data-placement='top' title='Thread Id: " + threadId + "'>(" + threadName + ")</span>";
}

function closeSocket() {
    webSocket.close();
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
            } else {
                var isMyClass = line.includes(loggerName);
                if (isMyClass && loggerName) {
                    line = '<b>' + line + '</b>';
                }
                line = tab + tab + line;
            }

        }

        enhanceStacktrace.push(line + '<br/>');
    }
    var newStacktrace = enhanceStacktrace.join('');
    return "<span class=\"text-wrap text-danger\">" + newStacktrace + "</span>";
}

function populateLoggerLevelModal(){
    // Get all logger names 
    
    var loggerNames = httpGet(loggersUrl);
    var loggerNamesArray = JSON.parse(loggerNames);
    
    var levelsUrl = loggersUrl + "/levels";
    var levelNames = httpGet(levelsUrl);
    var levelNamesArray = JSON.parse(levelNames);
    
    var tbodyLevels = $('#logLevelsTableBody');
    
    // Populate the dropdown
    for (var i = 0; i < loggerNamesArray.length; i++) {
        var row = "<tr><td id='" + createLevelRowId(loggerNamesArray[i].name) + "' class=" + getTextClass(loggerNamesArray[i].effectiveLevel) + ">" + loggerNamesArray[i].name + "</td><td>" + createDropdown(loggerNamesArray[i].name, loggerNamesArray[i].effectiveLevel,levelNamesArray) + "</td></tr>";
        tbodyLevels.append(row);
    }
    
    $('select').on('change', function() {
        changeLogLevel(this.value, $(this).find('option:selected').text());
    });
    
    populated = true;
}

function changeLogLevel(val,text){
    var xhr = new XMLHttpRequest();
    xhr.open("POST", loggersUrl, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    var data = JSON.stringify({"name": val, "configuredLevel": text});
    xhr.send(data);
    
    // Also change the style of the row
    var id = createLevelRowId(val);
    $('#' + id).removeClass();
    $('#' + id).addClass(getTextClass(text));    
}

function createLevelRowId(logger){
    var name = logger + "_row";
    return name.replaceAll(".", "_");
}

function httpGet(theUrl) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open("GET", theUrl, false); // false for synchronous request
    xmlHttp.send(null);
    return xmlHttp.responseText;
}

function createDropdown(name, level, levelNamesArray){
    
    var dd = "<select class='custom-select custom-select-sm'>";
    // Populate the dropdown
    for (var i = 0; i < levelNamesArray.length; i++) {
        var selected = "";
        if(level === levelNamesArray[i]){
            selected = "selected";
        }
        dd = dd + "<option " + selected + " value='" + name + "'>" + levelNamesArray[i] +"</option>";
    }
    dd = dd + "</select>";
    
    return dd;
}