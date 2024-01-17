const puppeteer = require("puppeteer");
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-inwebo");
    await cas.loginWith(page, "testcaspush", "password");
    await page.waitForTimeout(2000);
    const form = await page.$("#pendingCheckResultForm");
    assert(form !== null);
    await browser.close();
})();
