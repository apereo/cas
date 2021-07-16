const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    let header = await cas.innerText(page, '#sidebar div p');
    assert(header === "Stay safe!")
    header = await cas.innerText(page, '#login-form-controls h3 span');
    assert(header === "Welcome to CAS")
    await browser.close();
})();
