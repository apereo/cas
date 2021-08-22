const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?locale=de");
    await cas.assertInnerText(page, "#content #fm1 button[name=submit]", "ANMELDEN")
    await browser.close();
})();
