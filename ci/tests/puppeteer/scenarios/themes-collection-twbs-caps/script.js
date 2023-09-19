const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");

    await page.keyboard.press('CapsLock');
    await page.type('#password', "M");

    await cas.assertVisibility(page, '.caps-warn');
    const caps = await page.$('.caps-warn');
    await cas.log(`Caps warning is ${caps !== null ? 'NOT' : ''} hidden`);

    await browser.close();
})();
