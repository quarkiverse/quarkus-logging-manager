var zoom = 0.9;
var increment = 0.05;

var webSocket;
var messages = document.getElementById("messages");
var tab = "&nbsp;&nbsp;&nbsp;&nbsp";
var space = "&nbsp;";

var isRunning = true;
var logScrolling = true;

var loggersUrl = "/loggers";

var filter = "";

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
    followLogButton.addEventListener("click", followLogEvent);
    currentFilterInputButton.addEventListener("click", applyFilter);
    
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
    
    currentFilterInput.addEventListener("keyup", function(event) {
        if (event.keyCode === 13) {
            event.preventDefault();
            currentFilterInputButton.click();
        }
    });
    
    $('#filterModal').on('shown.bs.modal', function () {
        $('#currentFilterInput').trigger('focus');
    });
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
    writeResponse("<hr/>");

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

function applyFilter(){
    filter = $("#currentFilterInput").val();
    if(filter===""){
        clearFilter();
    }else{
        currentFilter.innerHTML = "<span style='border-bottom: 1px dotted;'>" + filter + " <i class='fas fa-times-circle' onclick='clearFilter();'></i></span>";
        
        var currentlines = $("#segmentLog").html().split('<!-- logline -->');
        
        var filteredHtml = "";
        var i;
        for (i = 0; i < currentlines.length; i++) {
            var htmlline = currentlines[i];
            filteredHtml = filteredHtml + getLogLine(htmlline) + "<!-- logline -->";
        } 
        
        segmentLog.innerHTML = "";
        writeResponse(filteredHtml);
    }
    $('#filterModal').modal('hide');
}

function getLogLine(htmlline){
    
    if(filter===""){
        return htmlline;
    }else{
        
        var textline = $(htmlline).text();
        if(textline.includes(filter)){
            return htmlline;
        }else{
            return htmlline.replace('<span>', '<span class="filtered">');
        }
    }
}

function clearFilter(){
    $("#currentFilterInput").val("");
    filter === "";
    currentFilter.innerHTML = "";
    
    var currentlines = $("#segmentLog").html().split('<!-- logline -->');
        
    var filteredHtml = "";
    var i;
    for (i = 0; i < currentlines.length; i++) {
        var htmlline = currentlines[i].replace('<span class="filtered">', '<span>');
        filteredHtml = filteredHtml + htmlline + "<!-- logline -->";
    } 

    segmentLog.innerHTML = "";
    writeResponse(filteredHtml);
    
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
        var level = json.level;

        var htmlLine = "<span>" + 
            getLevelIcon(level)
                + getSequenceNumber(json.sequenceNumber)
                + getDateString(timestamp)
                + getTimeString(timestamp)
                + getLevelText(level)
                + getClassFullAbbreviatedName(json.sourceClassNameFull, json.sourceClassNameFullShort)
                + getFullClassName(json.sourceClassNameFull)
                + getClassName(json.sourceClassName)
                + getMethodName(json.sourceMethodName)
                + getThreadId(json.threadName, json.threadId)
                + getThreadName(json.threadName, json.threadId)
                + getLogMessage(json.formattedMessage) + "<br/>";
                
        if (json.stacktrace) {
            for (var i in json.stacktrace) {
                var stacktrace = enhanceStacktrace(json.loggerName, json.stacktrace[i]);
                htmlLine = htmlLine + stacktrace;
            }
        }
        
        htmlLine = htmlLine + "</span><!-- logline -->";
        
        if(filter!=""){
            writeResponse(getLogLine(htmlLine));
        }else{
            writeResponse(htmlLine);
        }   
    }
}

function getLevelIcon(level) {
    if($('#levelIconSwitch').is(":checked")){
        level = level.toUpperCase();
        if (level === "WARNING" || level === "WARN")
            return "<i class='levelicon text-warning fas fa-exclamation-triangle'></i>" + tab;
        if (level === "SEVERE" || level === "ERROR")
            return "<i class='levelicon text-danger fas fa-radiation'></i>" + tab;
        if (level === "INFO")
            return "<i class='levelicon text-primary fas fa-info-circle'></i>" + tab;
        if (level === "DEBUG")
            return "<i class='levelicon text-secondary fas fa-bug'></i>" + tab;

        return "<i class='levelicon fas fa-circle'></i>" + tab;
    }
    return "";
}

function getSequenceNumber(sequenceNumber){
    if($('#sequenceNumberSwitch').is(":checked")){
        return "<span class='badge badge-info'>" + sequenceNumber + "</span>" + tab;   
    }
    return "";
}

function getDateString(timestamp){
    if($('#dateSwitch').is(":checked")){
        return timestamp.toLocaleDateString() + tab;
    }
    return "";
}

function getTimeString(timestamp){
    if($('#timeSwitch').is(":checked")){
        return timestamp.toLocaleTimeString() + tab;
    }
    return "";
}

function getLevelText(level) {
    if($('#levelSwitch').is(":checked")){
        level = level.toUpperCase();
        if (level === "WARNING" || level === "WARN")
            return "<span class='text-warning'>WARN </span>" + tab;
        if (level === "SEVERE" || level === "ERROR")
            return "<span class='text-danger'>ERROR</span>" + tab;
        if (level === "INFO")
            return "<span class='text-primary'>INFO </span>" + tab;
        if (level === "DEBUG")
            return "<span class='text-secondary'>DEBUG</span>" + tab;

        return level + tab;
    }
    return "";
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

function getClassFullAbbreviatedName(sourceClassNameFull, sourceClassNameFullShort) {
    if($('#sourceClassFullAbbreviatedSwitch').is(":checked")){
        return "<span class='text-primary' data-toggle='tooltip' data-placement='top' title='" + sourceClassNameFull + "'>[" + sourceClassNameFullShort + "]</span>" + space;
    }
    return "";
}

function getFullClassName(sourceClassNameFull) {
    if($('#sourceClassFullSwitch').is(":checked")){
        return "<span class='text-primary'>[" + sourceClassNameFull + "]</span>" + space;
    }
    return "";
}

function getClassName(className) {
    if($('#sourceClassSwitch').is(":checked")){
        return "<span class='text-primary'>[" + className + "]</span>" + space;
    }
    return "";
}

function getMethodName(methodName) {
    if($('#sourceMethodNameSwitch').is(":checked")){
        return methodName + tab;
    }
    return "";
}

function getThreadId(threadName, threadId) {
    if($('#threadIdSwitch').is(":checked")){
        return "<span class='text-success' data-toggle='tooltip' data-placement='top' title='Thread Name: " + threadName + "'>(" + threadId + ")</span>" + tab;
    }
    return "";
}

function getThreadName(threadName, threadId) {
    if($('#threadNameSwitch').is(":checked")){
        return "<span class='text-success' data-toggle='tooltip' data-placement='top' title='Thread Id: " + threadId + "'>(" + threadName + ")</span>" + tab;
    }
    return "";
}

function getLogMessage(message){
    if($('#messageSwitch').is(":checked")){
        return message;
    }
    return "";
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