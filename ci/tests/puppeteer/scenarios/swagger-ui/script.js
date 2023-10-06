const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    let response = await cas.goto(page, "https://localhost:8443/cas/v3/api-docs");
    await page.waitForTimeout(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    response = await cas.goto(page, "https://localhost:8443/cas/swagger-ui/index.html");
    await page.waitForTimeout(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await browser.close();
})();
