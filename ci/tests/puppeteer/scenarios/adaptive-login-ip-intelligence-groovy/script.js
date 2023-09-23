const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page);
    await page.waitForTimeout(2000);

    await cas.loginWith(page);
    await page.waitForTimeout(2000);
    await cas.assertTextContent(page, "#content h2", "Authentication attempt is blocked.");

    await browser.close();
})();
