const cas = require("../../cas.js");
const assert = require("assert");
const express = require("express");
const https = require("https");
const fs = require("fs");
const path = require("path");

let notifications = 0;
let notificationError = undefined;

async function verifyPushDelivery(userCode) {
    const clientId = "clientpush";
    const expiration = 60;
    const bindingMessage = "HelloFromCAS";
    let url = `https://localhost:8443/cas/oidc/oidcCiba?scope=${encodeURIComponent("openid profile")}`;
    url += "&client_notification_token=8d67dc78-7faa-4d41-aabd-67707b374255";
    url += `&login_hint=${encodeURIComponent("casuser@apereo.org")}`;
    url += `&binding_message=${bindingMessage}`;
    if (userCode.length > 0) {
        url += `&user_code=${userCode}`;
    }
    url += `&requested_expiry=${expiration}`;
    const authzHeader = `Basic ${Buffer.from(`${clientId}:secret`).toString("base64")}`;
    const authRequestId = await cas.doPost(url, "", {
        "Authorization": authzHeader,
        "Content-Type": "application/x-www-form-urlencoded"
    }, (res) => {
        cas.log(res.data);
        assert(res.data.expires_in <= expiration);
        return res.data.auth_req_id;
    }, (error) => {
        throw `CIBA operation failed: ${error}`;
    });
    await cas.log(`CIBA request id is ${authRequestId.length} characters`);
    assert(authRequestId.startsWith("CIBA-"), authRequestId);

    await cas.sleep(3000);
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        const verificationUrl = await cas.extractFromEmail(browser);
        assert(verificationUrl.endsWith(`/${clientId}/${authRequestId}`), verificationUrl);

        await cas.log("A tampered verification link is rejected");
        const index = Math.floor(authRequestId.length * 2 / 3);
        const tampered = `${authRequestId.substring(0, index)}${authRequestId[index] === "A" ? "B" : "A"}${authRequestId.substring(index + 1)}`;
        const response = await cas.goto(page, `https://localhost:8443/cas/oidc/oidcCiba/${clientId}/${tampered}`);
        assert(response.status() === 400);

        const received = notifications;
        await cas.goto(page, verificationUrl);
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
        await cas.assertVisibility(page, "#confirmation");
        assert(notificationError === undefined, `Push notification was rejected: ${notificationError}`);
        assert(notifications === received + 1, `Expected a push notification, received ${notifications - received}`);
    } finally {
        await cas.closeBrowser(browser);
    }
}

(async () => {
    const app = express();
    app.use(express.json());
    app.post("/notification", async (req, res) => {
        await cas.logg("Received CIBA notification from CAS");
        const requestBody = req.body;
        await cas.log(requestBody);
        try {
            assert(requestBody.access_token !== undefined);
            assert(requestBody.refresh_token !== undefined);
            assert(requestBody.id_token !== undefined);
            assert(requestBody.token_type === "Bearer");
            assert(requestBody.scope === "openid profile");
            const decoded = await cas.decodeJwt(requestBody.id_token);
            assert(decoded.sub === "casuser@apereo.org", decoded.sub);
            assert(decoded["urn:openid:params:jwt:claim:auth_req_id"] === requestBody.auth_req_id);
            notifications += 1;
            res.status(200).send("Accepted");
        } catch (e) {
            notificationError = e;
            res.status(500).send(e.message);
        }
    });
    const credentials = {
        key: fs.readFileSync(path.join(__dirname, "private.key"), "utf8"),
        cert: fs.readFileSync(path.join(__dirname, "certificate.crt"), "utf8")
    };
    const server = https.createServer(credentials, app).listen(5544, async () => {
        let failed = false;
        try {
            await verifyPushDelivery("393515b9-4bb0-43ef-9973-91f3c3236ffe");
            await cas.separator();
            await verifyPushDelivery("");
        } catch (e) {
            failed = true;
            throw e;
        } finally {
            server.close(() => cas.log("Exiting server..."));
            if (!failed) {
                await process.exit(0);
            }
        }
    });
})();
