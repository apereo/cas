const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    let response = await page.goto("http://localhost:8500/ui/dc1/services/cas/instances");
    await page.waitForTimeout(2000)
    console.log(`${response.status()} ${response.statusText()}`)
    assert(response.ok())
    await cas.click(page, "div.header a")
    await page.waitForResponse(response => response.status() === 200)
    await page.waitForTimeout(2000)
    await browser.close();
})();
