const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    let url1 = "https://localhost:9859/anything/sample";
    await cas.logg(`Trying with URL ${url1}`);
    let payload = await getPayload(page, url1, "client", "secret");
    let decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded["email"] === "cas@example.org");

    let url2 = "https://localhost:9859/anything/mapped";
    await cas.logg(`Trying with URL ${url2}`);
    payload = await getPayload(page, url2, "client2", "secret2");
    decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded["email"] === "cas@example.org");
    assert(decoded["memberships"].includes("director"));
    assert(decoded["memberships"].includes("admin"));

    await browser.close();
})();

async function getPayload(page, redirectUri, clientId, clientSecret) {
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${clientId}&scope=openid%20profile%20email&redirect_uri=${redirectUri}`;
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
