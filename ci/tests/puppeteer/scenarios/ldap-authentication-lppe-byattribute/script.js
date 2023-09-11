const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page);
    await page.waitForTimeout(2000);
    await cas.assertInnerText(page, "#content h2", "This account has been locked.");
    await cas.assertCookie(page, false);
    await cas.goto(page, "https://localhost:8443/cas/logout");
    await browser.close();
})();

