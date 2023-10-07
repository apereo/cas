const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    let service = "https://apereo.github.io#hello-world";
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    await cas.loginWith(page);
    await page.waitForTimeout(2000);
    await cas.assertTicketParameter(page);
    await cas.logPage(page);
    let url = await page.url();
    assert((url.match(/#/g) || []).length === 1);
    let result = new URL(page.url());
    await cas.logg(`URL hash is ${result.hash}`);
    assert(result.hash === "#hello-world");
    await browser.close();
})();
