const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://apereo.github.io");
    await page.waitForTimeout(1000);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    await cas.gotoLogin(page, "https://github.com/apereo/cas");
    await page.waitForTimeout(1000);
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);
    
    await browser.close();
})();
