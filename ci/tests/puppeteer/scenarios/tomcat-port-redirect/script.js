const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "http://localhost:8080/cas/login");
    await cas.log("Checking for page URL...");
    let url = await page.url();
    await cas.log(url);
    assert(url === "https://localhost:8443/cas/login");
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");
    await browser.close();
})();
