const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await cas.assertTextContent(page, "#qrlogin .card-title span", "Login with QR Code");
    await cas.assertVisibility(page, '#qrlogin .card-text img');
    await cas.assertVisibility(page, '#qrchannel')

    await browser.close();
})();
