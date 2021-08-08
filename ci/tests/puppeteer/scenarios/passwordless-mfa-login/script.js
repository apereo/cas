const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    let pswd = await page.$('#password');
    assert(pswd == null);
    await cas.type(page, '#username', "casuser");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await cas.assertInnerText(page, "#login h3", "Use your registered YubiKey device(s) to authenticate.")
    await browser.close();
})();
