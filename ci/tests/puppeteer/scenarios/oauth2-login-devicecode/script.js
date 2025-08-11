const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const url = "https://localhost:8443/cas/oauth2.0/accessToken?response_type=device_code&client_id=client";
    await cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(res.data.device_code !== undefined);
        assert(res.data.user_code !== undefined);
        assert(res.data.verification_uri !== undefined);
        assert(res.data.interval !== undefined);
        assert(res.data.expires_in !== undefined);

        verifyDeviceCode(res.data);
    }, (error) => {
        throw `Operation failed to obtain device token: ${error}`;
    });
})();

async function verifyDeviceCode(data) {
    const params = new URLSearchParams();
    params.append("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
    params.append("client_id", "client");
    params.append("device_code", data.device_code);
    const url = `https://localhost:8443/cas/oauth2.0/accessToken?${params.toString()}`;
    await cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, () => {
        throw "Operation must fail";
    }, (error) => {
        assert(error.response.status === 400);
        assert(error.response.data.error === "slow_down");
    });
    await cas.sleep(3000);
    await cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, () => {
        throw "Operation must fail";
    }, (error) => {
        assert(error.response.status === 400);
        assert(error.response.data.error === "authorization_pending");
    });

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto(data.verification_uri);
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.loginWith(page);
    await cas.type(page, "#usercode", data.user_code);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(2000);
    await cas.closeBrowser(browser);
    
    await cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(res.data.access_token !== undefined);
        assert(res.data.token_type !== undefined);
        assert(res.data.expires_in !== undefined);
        assert(res.data.scope === undefined);
        assert(res.data.refresh_token !== undefined);
    }, (error) => {
        throw `Operation failed ${error}`;
    });
}
