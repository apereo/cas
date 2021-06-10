const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions({ args: ['--lang=de'] }));
    const page = await cas.newPage(browser);
    await page.setExtraHTTPHeaders({
        'Accept-Language': 'de'
    });
    await page.goto("https://localhost:8443/cas/login");

    await page.waitForTimeout(1000)
    const header = await cas.innerText(page, '#content #fm1 button[name=submit]');

    assert(header === "ANMELDEN")
    await browser.close();
})();
