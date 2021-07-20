const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const authorize_url = 'https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=openid&redirect_uri=https://www.google.fr&nonce=x&state=y';
    const url = authorize_url + '&prompt=none';
    await page.goto(url);

    await page.waitForTimeout(2000)

    assert(page.url() === 'https://www.google.fr/?error=login_required');

    await page.goto(authorize_url);

    await page.waitForTimeout(2000)

    await cas.loginWith(page, "casuser", "Mellon");

    await page.waitForTimeout(2000)

    await browser.close();
})();
