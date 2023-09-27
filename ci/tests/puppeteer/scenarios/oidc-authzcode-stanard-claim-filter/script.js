const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    let url1 = "https://httpbin.org/anything/sample";
    await cas.logg(`Trying with URL ${url1}`);
    let payload = await getPayload(page, url1, "client", "secret");
    let decoded = await cas.decodeJwt(payload.id_token);

    assert(decoded["sub"] === "casuser");
    assert(decoded["email"] === "cas@apereo.org");
    assert(decoded["locale"] === "german");
    assert(decoded["name"] === "casoidc");
    assert(decoded["preferred_username"] === "casuser");

    assert(decoded["given_name"] === undefined);
    assert(decoded["last_name"] === undefined);
    assert(decoded["email_verified"] === undefined);
    assert(decoded["gender"] === undefined);

    await browser.close();
})();

async function getPayload(page, redirectUri, clientId, clientSecret) {
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${clientId}&scope=openid%20profile%20email&redirect_uri=${redirectUri}`;
    await cas.goto(page, url);
    await cas.log(`Page URL: ${page.url()}`);
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
    await cas.goto(page, accessTokenUrl);
    await cas.log(`Page URL: ${page.url()}`);
    await page.waitForTimeout(1000);
    let content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    await cas.log(payload);
    return payload;
}
