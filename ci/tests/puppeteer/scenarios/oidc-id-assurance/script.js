const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    let url = "https://localhost:9859/anything/sample1";
    await cas.logg(`Trying with URL ${url}`);
    let payload = await getPayload(page, url, "client1", "secret1");
    let decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded.sub === "casuser");
    assert(decoded.txn !== null);
    assert(decoded.given_name === undefined);
    assert(decoded.family_name === undefined);
    assert(decoded.client_id === "client1");
    assert(decoded["preferred_username"] === "casuser");
    assert(decoded["locale"] === "en");
    assert(decoded["name"] === "casuser");
    assert(decoded["nickname"] === "CAS");
    assert(decoded["verified_claims"]["verification"]["trust_framework"] === "spid");
    assert(decoded["verified_claims"]["verification"]["verification_process"] === "b54c6f-6d3f-4ec5-973e-b0d8506f3bc7");
    assert(decoded["verified_claims"]["verification"]["evidence"][0]["type"] === "document");
    assert(decoded["verified_claims"]["claims"]["given_name"] === "CAS");
    assert(decoded["verified_claims"]["claims"]["family_name"] === "Apereo");
    await browser.close();
})();

async function getPayload(page, redirectUri, clientId, clientSecret) {
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${clientId}&scope=openid%20profile&redirect_uri=${redirectUri}`;
    await cas.goto(page, url);
    await cas.logPage(page);
    await page.waitForTimeout(1000);

    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await page.waitForTimeout(1000)
    }
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }

    let code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?grant_type=authorization_code`
        + `&client_id=${clientId}&client_secret=${clientSecret}&redirect_uri=${redirectUri}&code=${code}`;
    return await cas.doPost(accessTokenUrl, "", {
        'Content-Type': "application/json"
    }, res => {
        return res.data;
    }, error => {
        throw `Operation failed to obtain access token: ${error}`;
    });
}
