const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    // await page.waitForTimeout(1000)

    await cas.assertVisibility(page, '#authnSourceSection')

    let json = await cas.innerText(page, '#JSON-authnSource');
    assert(json.trim() === "JSON")

    let example = await cas.innerText(page, '#EXAMPLE-authnSource');
    assert(example.trim() === "EXAMPLE")

    await browser.close();
})();
