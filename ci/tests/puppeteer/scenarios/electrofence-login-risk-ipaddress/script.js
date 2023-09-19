const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    let service = "https://localhost:9859/anything/adaptive";
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    await page.waitForTimeout(2000);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(2000);
    await cas.assertInnerTextContains(page, "#loginErrorsPanel p", "authentication attempt is determined to be risky");

    await cas.goto(page, "http://localhost:8282");
    await page.waitForTimeout(1000);
    await cas.click(page, "table tbody td a");
    await page.waitForTimeout(1000);
    let body = await cas.textContent(page, "div[name=bodyPlainText] .well");
    await cas.log(`Email message body is: ${body}`);
    assert(body.includes("casuser with score 1.00"));
    await browser.close();
})();
