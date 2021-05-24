const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    // await page.waitForTimeout(20000)

    let source = await page.$('#phone-customField');
    assert(await source.boundingBox() != null);

    await browser.close();
})();
