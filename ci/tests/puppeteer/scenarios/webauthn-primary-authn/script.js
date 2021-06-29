const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await page.waitForTimeout(1000)

    await cas.assertVisibility(page, '#webauthnLoginPanel div h2#status')

    const header = await cas.textContent(page, "#webauthnLoginPanel div h2#status");
    assert(header === "Login with FIDO2-enabled Device");

    await browser.close();
})();
