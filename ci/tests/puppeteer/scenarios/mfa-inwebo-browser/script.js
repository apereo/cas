const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://127.0.0.1:8443/cas/login?authn_method=mfa-inwebo");
    await cas.loginWith(page, "testcas", "password");
    await page.waitForTimeout(5000);
    await cas.screenshot(page);

    const startBrowserForm = await page.$('#startBrowserForm');
    assert(startBrowserForm != null);
    const startPushForm = await page.$('#startPushForm');
    assert(startPushForm != null);

    console.log("Asking for the PIN code");
    await page.$eval('button[name=browser]', button => button.click());
    await page.waitForTimeout(8000);

    await cas.assertVisibility(page, "#code");
    await cas.assertVisibility(page, "#pin");
    await cas.assertVisibility(page, "#enrollButton");

    await browser.close();
})();
