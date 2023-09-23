const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?response_type=code"
        + "&client_id=client&scope=openid%20profile%20MyCustomScope&"
        + "redirect_uri=https://apereo.github.io";

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
    const accessTokenUrl = `https://localhost:8443/cas/oidc/token?grant_type=authorization_code`
        + `&client_id=client&client_secret=secret&redirect_uri=https://apereo.github.io&code=${code}`;
    await cas.goto(page, accessTokenUrl);
    await page.waitForTimeout(1000);
    let content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    await cas.log(payload);
    assert(payload.access_token != null);
    assert(payload.token_type != null);
    assert(payload.expires_in != null);
    assert(payload.scope != null);

    let decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded.sub !== null);
    assert(decoded.client_id !== null);
    assert(decoded["preferred_username"] !== null);
    assert(decoded["name"] !== null);

    assert(decoded["entitlements"].includes('ent-A'));
    assert(decoded["entitlements"].includes('ent-B'));

    assert(decoded["aud"].includes('cas'));
    assert(decoded["aud"].includes(decoded.client_id));
    assert(decoded["aud"].includes('apereo'));

    await browser.close();
})();
