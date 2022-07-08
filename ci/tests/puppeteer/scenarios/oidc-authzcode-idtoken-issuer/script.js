const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/authorize?response_type=code"
        + "&client_id=client&scope=openid%20profile&"
        + "redirect_uri=https://apereo.github.io";

    await cas.goto(page, url);
    await page.waitForTimeout(1000)
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }

    let code = await cas.assertParameter(page, "code");
    console.log(`Current code is ${code}`);
    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?grant_type=authorization_code`
        + `&client_id=client&client_secret=secret&redirect_uri=https://apereo.github.io&code=${code}`;
    await cas.goto(page, accessTokenUrl);
    await page.waitForTimeout(1000)
    let content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    console.log(payload);
    assert(payload.access_token != null);
    assert(payload.id_token != null);

    console.log("Decoding access token...");
    let decodedAccessToken = await cas.decodeJwt(payload.access_token);
    assert(decodedAccessToken.iss === "https://sso.example.org/cas/oidc");

    console.log("Decoding ID token...");
    let decodedIdToken = await cas.decodeJwt(payload.id_token);
    assert(decodedIdToken.sub !== null)
    assert(decodedIdToken.client_id !== null)
    assert(decodedIdToken.iss === "https://sso.example.org/cas/oidc");

    await browser.close();
})();
