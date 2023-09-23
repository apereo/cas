const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "invalidtime", "invalidtime");
    await cas.assertInnerText(page, "#content h2", "Your account is forbidden to login at this time.");
    await browser.close();
})();
