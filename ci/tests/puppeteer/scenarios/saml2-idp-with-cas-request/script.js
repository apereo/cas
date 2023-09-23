const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https%3A%2F%2Fsamltest.id%2Fcallback%3Fclient_name%3DCasClient");
    await page.waitForTimeout(1000);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    await browser.close();
})();


