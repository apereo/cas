function sqrlInProgress(sqrlurl) {
    var sqrlImgSrc = $("#sqrlImg").attr("src");
    var showingSqrlQr = sqrlImgSrc != "/cas/images/spinner.gif";
    if (!showingSqrlQr) {
        return;
    }
    $("#cancelSqrl").hide();

    $("#sqrlImg").attr("src", "/cas/images/spinner.gif");
    $("#cancelSqrl").show();
    $("#cancelSqrl").css("visibility", "");
    $("#fm1").hide();
    
    window.location.replace(sqrlurl);
}

function stopPolling(socket, subsocket, request) {
    subsocket.push("done");
    socket.close();
}

$(document).ready(function () {
    $("#cancelSqrl").hide();
    var socket = atmosphere;
    var subsocket;
    var atmosphereurl = window.location.pathname.substring(0, window.location.pathname.lastIndexOf("/")) + "/sqrlauthpolling";
    var request = {
        url: atmosphereurl,
        contentType: "application/json",
        logLevel: "debug",
        transport: "sse",
        reconnectInterval: 5000,
        fallbackTransport: "long-polling"
    };
    request.onOpen = function (response) {
        console.debug("Atmosphere connected using " + response.transport);
    };

    request.onReconnect = function (request, response) {
        console.info("Atmosphere connection lost, trying to reconnect " + request.reconnectInterval);
    };
    request.onReopen = function (response) {
        console.info("Atmosphere re-connected using " + response.transport);
    };
    request.onMessage = function (response) {
        var status = response.responseBody;
        console.error("received from server: " + status);
        if (status.indexOf("ERROR_") > -1) {
            window.location.replace("login?error=" + status);
        } else if (status == "AUTH_COMPLETE") {
            subsocket.push(atmosphere.util.stringifyJSON({state: "AUTH_COMPLETE"}));
            subsocket.close();
            window.location.replace("sqrllogin");
        } else if (status == "COMMUNICATING") {
            subsocket.push(atmosphere.util.stringifyJSON({state: "COMMUNICATING"}));
            sqrlInProgress();
        } else {
            console.error("received unknown state from server: " + status);
        }
    };
    request.onClose = function (response) {
        console.info("Server closed the connection after a timeout");
    };
    request.onError = function (response) {
        console.error("Error, there\'s a problem with the socket connection or the server is down");
    };

    subsocket = socket.subscribe(request);
    subsocket.push(atmosphere.util.stringifyJSON({state: "CORRELATOR_ISSUED"}));
});
