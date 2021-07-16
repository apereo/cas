const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=unknown-service");

    const header = await cas.innerText(page, '#content h2');
    assert(header === "Application Not Authorized to Use CAS")
    
    await browser.close();
})();
