const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    const header = await cas.textContent(page, "#qrlogin .card-title span");
    assert(header === "Login with QR Code");

    await cas.assertVisibility(page, '#qrlogin .card-text img')

    await cas.assertVisibility(page, '#qrchannel')

    await browser.close();
})();
