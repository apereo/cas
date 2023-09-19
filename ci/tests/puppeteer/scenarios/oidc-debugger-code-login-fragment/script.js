const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?" +
        "client_id=client&" +
        "redirect_uri=https%3A%2F%2Foidcdebugger.com%2Fdebug&" +
        "scope=openid%20email%20profile%20address%20phone&" +
        "response_type=code&" +
        "response_mode=fragment&" +
        "nonce=vn4qulthnx";
    await cas.goto(page, url);

    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000);

    await cas.click(page, "#allow");
    await page.waitForNavigation();
    await cas.log(`Page url: ${await page.url()}`);
    let result = await page.evaluate(() => window.location.hash);
    assert(result.includes("code="));
    assert(result.includes("nonce="));
    await page.waitForTimeout(1000);
    await cas.assertTextContent(page, "h1.green-text", "Success!");

    await browser.close();
})();

