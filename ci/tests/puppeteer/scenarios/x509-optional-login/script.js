const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    var element = await page.$('#x509Login');
    assert(await element.boundingBox() != null);

    await browser.close();
})();
