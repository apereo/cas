const cas = require("../../cas.js");
const SockJS = require("sockjs-client");
const StompJS = require("@stomp/stompjs");
const querystring = require("querystring");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.assertTextContent(page, "#qrlogin .card-title span", "Login with QR Code");
    await cas.assertVisibility(page, "#qrlogin .card-text img");
    await cas.assertVisibility(page, "#qrchannel");

    const qrCode = await cas.parseQRCode(page, "#qrcode");
    const channelId = qrCode.data;
    await cas.logg(`QR channel code is ${channelId}`);
    await connectAndLogin(channelId, page);

    await cas.closeBrowser(browser);
})();

async function connectAndLogin(channelId, page) {
    const client = new StompJS.Client({
        brokerURL: "ws://localhost:8443/cas/qr-websocket",
        debug: (str) => cas.log(str),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000
    });

    if (typeof WebSocket !== "function") {
        client.webSocketFactory = () => new SockJS("https://localhost:8443/cas/qr-websocket");
    }

    client.onConnect = (frame) => {
        const deviceId = "QRDevicePuppeteer";
        cas.logg(`We have now connected ${frame.headers["message"]}`);

        const formData = {
            username: "casuser",
            password: "Mellon",
            token: true,
            QR_AUTHENTICATION_DEVICE_ID: deviceId
        };
        const postData = querystring.stringify(formData);
        executeRequest("https://localhost:8443/cas/v1/tickets", 201, postData)
            .then((token) => {
                cas.log(`Received token ${token}`);
                const payload = JSON.stringify({"token": token});
                client.publish({
                    destination: "/qr/accept",
                    headers: {
                        "QR_AUTHENTICATION_CHANNEL_ID": channelId,
                        "QR_AUTHENTICATION_DEVICE_ID": deviceId
                    },
                    body: payload
                });
                cas.assertInnerText(page, "#content div h2", "Log In Successful");
                cas.assertCookie(page);
                client.deactivate();
                client.forceDisconnect();
                process.exit(0);
            });
    };

    client.onStompError = (frame) => {
        cas.logr(`Broker reported error: ${frame.headers["message"]}`);
        cas.logr(`Additional details: ${frame.body}`);
    };

    client.activate();
}

async function executeRequest(url, statusCode, requestBody) {
    return cas.doRequest(url, "POST", {
        "Accept": "application/json",
        "Content-Length": Buffer.byteLength(requestBody),
        "Content-Type": "application/x-www-form-urlencoded"
    }, statusCode, requestBody);
}
