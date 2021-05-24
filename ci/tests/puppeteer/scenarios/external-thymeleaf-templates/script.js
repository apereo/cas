const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    let footer = await page.$('#externalFooter');
    assert(await footer.boundingBox() != null);

    await browser.close();
})();
