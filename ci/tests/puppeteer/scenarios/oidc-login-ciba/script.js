const cas = require("../../cas.js");
const assert = require("assert");
const express = require("express");
const https = require("https");
const fs = require("fs");
const path = require("path");

async function verifyDeliveryMode(clientId = "client", deliveryMode = "push", userCode = "393515b9-4bb0-43ef-9973-91f3c3236ffe", clientSecret = "secret") {
    await cas.log(`Attempting to verify CIBA delivery mode ${deliveryMode} with user code ${userCode} for client ID ${clientId}`);

    const expiration = 60;
    const clientNotificationToken = "8d67dc78-7faa-4d41-aabd-67707b374255";
    const bindingMessage = "HelloFromCAS";
    const scopes = `${encodeURIComponent("openid profile")}`;

    let url = `https://localhost:8443/cas/oidc/oidcCiba?scope=${scopes}`;
    url += `&client_notification_token=${clientNotificationToken}`;
    url += `&login_hint=${encodeURIComponent("casuser@apereo.org")}`;
    url += `&binding_message=${bindingMessage}`;

    if (userCode.length > 0) {
        url += `&user_code=${userCode}`;
    }
    url += `&requested_expiry=${expiration}`;

    const value = `${clientId}:${clientSecret}`;
    const buff = Buffer.alloc(value.length, value);
    const authzHeader = `Basic ${buff.toString("base64")}`;
    await cas.log(`Authorization header: ${authzHeader}`);

    const authRequestId = await cas.doPost(url, "",
        {
            "Authorization": authzHeader,
            "Content-Type": "application/x-www-form-urlencoded"
        },
        (res) => {
            cas.log(res.data);
            assert(res.data.expires_in === expiration);
            assert(res.data.auth_req_id !== undefined);
            return res.data.auth_req_id;
        }, (error) => {
            throw `CIBA operation failed: ${error}`;
        });
    await cas.log(`CIBA request id is ${authRequestId}`);
    if (deliveryMode === "ping" || deliveryMode === "poll") {
        const accessTokenUrl = `https://localhost:8443/cas/oidc/token?scope=${scopes}&grant_type=urn:openid:params:grant-type:ciba&auth_req_id=${authRequestId}`;
        await cas.doPost(accessTokenUrl, "",
            {
                "Authorization": authzHeader,
                "Content-Type": "application/x-www-form-urlencoded"
            },
            (res) => {
                throw `CIBA operation MUST fail but instead it passed incorrectly: ${res.data}`;
            }, (error) => cas.log(`CIBA operation failed correctly: ${error}`));
    }
    await cas.sleep(3000);
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const verificationUrl = await cas.extractFromEmail(browser);
    await cas.goto(page, verificationUrl);

    await cas.assertVisibility(page, "#scopes");
    await cas.assertVisibility(page, "#cibaContainer");
    await cas.assertInnerText(page, "#cibaContainer h1", "Backchannel Authentication Request");
    await cas.assertInnerText(page, "#cibaContainer #bindingMessage", bindingMessage);

    if (userCode.length > 0) {
        await cas.type(page, "#userCode", userCode);
    } else {
        await cas.assertInvisibility(page, "#userCode");
    }
    await cas.click(page, "#confirmButton");
    await cas.sleep(3000);
    await cas.assertInvisibility(page, "#error");
    await cas.assertInvisibility(page, "#cibaContainer");
    await cas.assertVisibility(page, "#confirmation");
    await cas.closeBrowser(browser);

    if (deliveryMode === "ping" || deliveryMode === "poll") {
        const accessTokenUrl = `https://localhost:8443/cas/oidc/token?scope=${scopes}&grant_type=urn:openid:params:grant-type:ciba&auth_req_id=${authRequestId}`;
        await cas.doPost(accessTokenUrl, "",
            {
                "Authorization": authzHeader,
                "Content-Type": "application/x-www-form-urlencoded"
            },
            (res) => {
                cas.logg("CIBA operation correctly succeeded to obtain tokens");
                cas.log(res.data);
                assert(res.data.access_token !== undefined);
                assert(res.data.refresh_token !== undefined);
                assert(res.data.id_token !== undefined);
                assert(res.data.token_type === "Bearer");
                assert(res.data.expires_in !== undefined);
                assert(res.data.scope === "openid profile");
            }, (error) => {
                throw `CIBA operation MUST failed to obtain tokens: ${error}`;
            });
    }
    await cas.sleep(3000);
    return authRequestId;
}

(async () => {
    const app = express();
    app.use(express.json());
    app.post("/notification", (req, res) => {
        cas.logg("Received CIBA notification from CAS");
        const requestBody = req.body;
        cas.log(requestBody);
        assert(requestBody.access_token !== undefined);
        assert(requestBody.refresh_token !== undefined);
        assert(requestBody.id_token !== undefined);
        assert(requestBody.token_type === "Bearer");
        assert(requestBody.expires_in !== undefined);
        assert(requestBody.scope === "openid profile");
        
        const decoded = cas.decodeJwt(requestBody.id_token);
        assert(decoded.sub === "casuser@apereo.org");
        assert(decoded["urn:openid:params:jwt:claim:auth_req_id"] !== undefined);
        assert(decoded["urn:openid:params:jwt:claim:rt_hash"] !== undefined);

        res.status(200).send("Accepted");
    });
    const privateKey = fs.readFileSync(path.join(__dirname, "private.key"), "utf8");
    const certificate = fs.readFileSync(path.join(__dirname, "certificate.crt"), "utf8");
    const credentials = { key: privateKey, cert: certificate };

    const httpsServer = https.createServer(credentials, app);
    const server = httpsServer.listen(5544, async () => {
        await cas.log("Starting CIBA request with PUSH delivery mode...");
        await verifyDeliveryMode("clientpush", "push");
        await cas.sleep();
        await cas.separator();
        await verifyDeliveryMode("clientpush", "push", "");
        await cas.separator();

        await cas.log("Starting CIBA request with PING delivery mode...");
        await verifyDeliveryMode("clientping", "ping");

        await cas.separator();
        await verifyDeliveryMode("clientping", "ping", "");
        await cas.separator();

        await cas.log("Starting CIBA request with POLL delivery mode...");
        await verifyDeliveryMode("clientpoll", "poll");
        await cas.separator();
        await verifyDeliveryMode("clientpoll", "poll", "");
        
        server.close(() => cas.log("Exiting server..."));
    });

})();

