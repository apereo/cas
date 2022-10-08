const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login?authn_method=mfa-duo");
    await cas.loginWith(page, "unknown", "Mellon");
    await page.waitForTimeout(5000);
    await cas.screenshot(page);
    let url = await page.url();
    console.log(url);
    assert(url === "https://httpbin.org/anything/1");
    await browser.close();
})();
