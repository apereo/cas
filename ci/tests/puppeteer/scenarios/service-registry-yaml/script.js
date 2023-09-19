const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://apereo.github.io");

    await cas.loginWith(page);
    const url = await page.url();
    await cas.log(`Page url: ${url}`);
    await cas.assertTicketParameter(page);
    await browser.close();
})();
