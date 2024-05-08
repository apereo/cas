const cas = require("../../cas.js");
const assert = require("assert");

async function verifyPushDeliveryMode(userCode = "393515b9-4bb0-43ef-9973-91f3c3236ffe") {
    await cas.log(`Attempting to verify CIBA push delivery mode with user code ${userCode}`);
    
    const clientNotificationToken = "8d67dc78-7faa-4d41-aabd-67707b374255";
    const bindingMessage = "HelloFromCAS";
    let url = `https://localhost:8443/cas/oidc/oidcCiba?scope=${encodeURIComponent("openid profile")}`;
    url += `&client_notification_token=${clientNotificationToken}`;
    url += `&login_hint=${encodeURIComponent("casuser@apereo.org")}`;
    url += `&binding_message=${bindingMessage}`;
    if (userCode.length > 0) {
        url += `&user_code=${userCode}`;
    }
    url += "&requested_expiry=60";

    const value = "client:secret";
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
            assert(res.data.expires_in === 60);
            assert(res.data.auth_req_id !== undefined);
            return res.data.auth_req_id;
        }, (error) => {
            throw `CIBA operation failed: ${error}`;
        });
    await cas.log(`CIBA request id is ${authRequestId}`);

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
    await browser.close();
}

(async () => {
    await verifyPushDeliveryMode("393515b9-4bb0-43ef-9973-91f3c3236ffe");
    await verifyPushDeliveryMode("");
})();

