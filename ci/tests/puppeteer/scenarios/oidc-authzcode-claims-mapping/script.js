const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/authorize?response_type=code"
        + "&client_id=client&scope=openid%20profile%20email"
        + "&redirect_uri=https://apereo.github.io";

    await cas.goto(page, url);
    await cas.log(`Page URL: ${page.url()}`);
    await page.waitForTimeout(1000);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000);
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }

    let code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?grant_type=authorization_code`
        + `&client_id=client&client_secret=secret&redirect_uri=https://apereo.github.io&code=${code}`;

    await cas.goto(page, accessTokenUrl);
    await page.waitForTimeout(1000);
    let content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    await cas.log(payload);
    let decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded.sub !== null);
    assert(decoded.client_id !== null);
    assert(decoded["preferred_username"] === "apereo-casuser");
    assert(decoded["email"] !== null);
    assert(decoded["family_name"] !== null);
    assert(decoded["given_name"] !== null);
    assert(decoded["name"] !== null);
    await browser.close();
})();
