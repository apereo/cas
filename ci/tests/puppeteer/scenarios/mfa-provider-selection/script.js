const puppeteer = require('puppeteer');

const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://example.com");
    await cas.loginWith(page);
    await page.waitForTimeout(2000);
    await cas.assertVisibility(page, '#mfa-gauth');
    await cas.assertVisibility(page, '#mfa-yubikey');
    await browser.close();
})();
