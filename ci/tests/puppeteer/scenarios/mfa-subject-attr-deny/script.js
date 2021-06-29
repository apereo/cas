const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");

    await page.waitForTimeout(3000)
    
    const p = await cas.innerText(page, '#loginErrorsPanel p');
    assert(p.startsWith("Authentication attempt for your account is denied"));
    
    await browser.close();
})();
