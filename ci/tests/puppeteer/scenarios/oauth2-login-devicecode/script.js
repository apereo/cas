const assert = require('assert');
const cas = require('../../cas.js');
const puppeteer = require("puppeteer");

(async () => {
    const url = `https://localhost:8443/cas/oauth2.0/accessToken?response_type=device_code&client_id=client`;
    await cas.doPost(url, "", {
        'Content-Type': "application/json"
    }, res => {
        // await cas.log(res.data);
        assert(res.data.device_code !== null);
        assert(res.data.user_code !== null);
        assert(res.data.verification_uri !== null);
        assert(res.data.interval !== null);
        assert(res.data.expires_in !== null);

        verifyDeviceCode(res.data);
    }, error => {
        throw `Operation failed to obtain access token: ${error}`;
    });
})();

async function verifyDeviceCode(data) {
    let params = new URLSearchParams();
    params.append('grant_type', 'urn:ietf:params:oauth:grant-type:device_code');
    params.append('client_id', 'client');
    params.append('device_code', data.device_code);
    const url = `https://localhost:8443/cas/oauth2.0/accessToken?${params.toString()}`;
    await cas.doPost(url, "", {
        'Content-Type': "application/json"
    }, res => {
        throw `Operation must fail`;
    }, error => {
        assert(error.response.status === 400);
        assert(error.response.data.error === "slow_down")
    });
    await cas.sleep(3000);
    await cas.doPost(url, "", {
        'Content-Type': "application/json"
    }, res => {
        throw `Operation must fail`;
    }, error => {
        assert(error.response.status === 400);
        assert(error.response.data.error === "authorization_pending")
    });

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto(data.verification_uri);
    await page.waitForTimeout(1000);
    await cas.log(`Page url: ${await page.url()}`);
    await cas.loginWith(page);
    await cas.type(page, "#usercode", data.user_code);
    await cas.pressEnter(page);
    await page.waitForNavigation();
    await page.waitForTimeout(2000);
    await browser.close();
    
    await cas.doPost(url, "", {
        'Content-Type': "application/json"
    }, res => {
        assert(res.data.access_token !== null);
        assert(res.data.token_type !== null);
        assert(res.data.expires !== null);
        assert(res.data.refresh_token !== null)
    }, error => {
        throw `Operation failed ${error}`;
    });
}
