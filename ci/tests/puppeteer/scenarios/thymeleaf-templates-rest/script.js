const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    await cas.log("Starting HTTP server...");
    await cas.httpServer(__dirname);

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await page.waitForTimeout(2000);

    const title = await cas.innerText(page, "#title");
    assert(title === "Hello, World!");

    await browser.close();
    await process.exit(0);
})();
