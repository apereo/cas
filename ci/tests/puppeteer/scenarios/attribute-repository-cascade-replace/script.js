const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "[John]");
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "[Smith]");
    await cas.assertCookie(page);
    await browser.close();
})();
