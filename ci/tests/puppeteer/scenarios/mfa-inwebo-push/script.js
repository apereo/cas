const puppeteer = require("puppeteer");
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login?authn_method=mfa-inwebo");
    await cas.loginWith(page, "testcaspush", "password");
    await page.waitForTimeout(2000);
    const form = await page.$("#pendingCheckResultForm");
    assert(form !== null);
    await browser.close();
})();
