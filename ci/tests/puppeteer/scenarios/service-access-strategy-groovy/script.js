const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    let response = await cas.gotoLogin(page, "https://localhost:9859/anything/denied");
    await page.waitForTimeout(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() === 403);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");

    response = await cas.gotoLogin(page, "https://localhost:9859/anything/allowed");
    response = await cas.loginWith(page);
    await page.waitForTimeout(2000);
    await cas.logPage(page);
    await cas.assertInnerText(page, "#loginErrorsPanel p", "Service access denied due to missing privileges.");
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() === 401);

    await browser.close();
})();
