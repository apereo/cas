const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "unknown-service");
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    await browser.close();
})();
