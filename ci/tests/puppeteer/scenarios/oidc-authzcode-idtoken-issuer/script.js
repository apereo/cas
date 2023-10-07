const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

async function testService(page, clientId, oidc = true) {
    await cas.log(`Testing application with client id ${clientId}`);
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${clientId}&scope=openid%20profile&redirect_uri=https://apereo.github.io`;
    await cas.goto(page, url);
    await page.waitForTimeout(1000);
    await cas.loginWith(page);
    await page.waitForTimeout(1000);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }

    let code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?grant_type=authorization_code&client_id=${clientId}&client_secret=secret&redirect_uri=https://apereo.github.io&code=${code}`;
    await cas.goto(page, accessTokenUrl);
    await page.waitForTimeout(1000);
    let content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    await cas.log(payload);
    assert(payload.access_token != null);

    await cas.log("Decoding access token...");
    let decodedAccessToken = await cas.decodeJwt(payload.access_token);

    if (oidc) {
        assert(decodedAccessToken.iss === "https://sso.example.org/cas/oidc");
        await cas.log("Decoding ID token...");
        assert(payload.id_token != null);
        let decodedIdToken = await cas.decodeJwt(payload.id_token);
        assert(decodedIdToken.sub !== null);
        assert(decodedIdToken.client_id !== null);
        assert(decodedIdToken.iss === "https://sso.example.org/cas/oidc");
    } else {
        assert(decodedAccessToken.grant_type === "authorization_code");
        assert(decodedAccessToken.iss === "https://localhost:8443/cas/oidc");
        assert(decodedAccessToken.client_id === "oauth-clientid");
    }
    
    await cas.goto(page, `https://localhost:8443/cas/logout`);
    await page.waitForTimeout(1000);
    await cas.log("=========================================================")
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await testService(page, "client", true);
    await testService(page, "oauth-clientid", false);
    await browser.close();
})();
