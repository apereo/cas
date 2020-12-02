const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    // await page.waitForTimeout(20000)

    let source = await page.$('#phone-customField');
    assert(await source.boundingBox() != null);

    await browser.close();
})();
