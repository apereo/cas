const assert = require("assert");
const cas = require("../../cas.js");
const SockJS = require("sockjs-client");
const querystring = require("querystring");

const DEVICES = "https://localhost:8443/cas/actuator/qrDevices";
const JSON_HEADERS = {"Accept": "application/json", "Content-Type": "application/json"};

/**
 * Only devices registered for the principal may log in, so the device is registered through the qrDevices endpoint
 * before the login, and a second device is registered and removed again to check the endpoint keeps them apart.
 */
async function registerDevices() {
    assert(JSON.parse(await cas.doRequest(`${DEVICES}/casuser`, "GET", JSON_HEADERS, 200)).length === 0);
    await cas.doRequest(`${DEVICES}/casuser/QRDevicePuppeteer`, "POST", JSON_HEADERS, 204);
    await cas.doRequest(`${DEVICES}/casuser/QRDeviceRemoved`, "POST", JSON_HEADERS, 204);
    let registered = JSON.parse(await cas.doRequest(`${DEVICES}/casuser`, "GET", JSON_HEADERS, 200));
    assert(registered.length === 2 && registered.includes("QRDevicePuppeteer") && registered.includes("QRDeviceRemoved"));
    await cas.doRequest(`${DEVICES}/QRDeviceRemoved`, "DELETE", JSON_HEADERS, 204);
    registered = JSON.parse(await cas.doRequest(`${DEVICES}/casuser`, "GET", JSON_HEADERS, 200));
    assert(registered.length === 1 && registered[0] === "QRDevicePuppeteer");
    assert(JSON.parse(await cas.doRequest(`${DEVICES}/someoneelse`, "GET", JSON_HEADERS, 200)).length === 0);
}

(async () => {
    await registerDevices();
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
    const {Client} = await import("@stomp/stompjs");
    const client = new Client({
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
            .then(async (token) => {
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
                await cas.sleep(3000);
                await cas.assertInnerText(page, "#content div h2", "Log In Successful");
                await cas.assertCookie(page);
                await cas.doRequest(`${DEVICES}/QRDevicePuppeteer`, "DELETE", JSON_HEADERS, 204);
                assert(JSON.parse(await cas.doRequest(`${DEVICES}/casuser`, "GET", JSON_HEADERS, 200)).length === 0);
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
