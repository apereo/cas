const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost1:8443/cas/login?authn_method=mfa-inwebo");
    await page.type('#username', "testcaspush");
    await page.type('#password', "password");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.waitForTimeout(2000)

    let form = await page.$('#pendingCheckResultForm');
    assert(form != null);

    await browser.close();
})();
