const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    let response = await page.goto("https://localhost:8443/cas/v3/api-docs");
    await page.waitForTimeout(3000)
    console.log(`${response.status()} ${response.statusText()}`)
    assert(response.ok())

    response = await page.goto("https://localhost:8443/cas/swagger-ui.html");
    await page.waitForTimeout(3000)
    console.log(`${response.status()} ${response.statusText()}`)
    assert(response.ok())
    
    await browser.close();
})();
