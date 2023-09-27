const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login?authn_method=mfa-duo");
    await cas.loginWith(page, "duocode", "Mellon");
    await page.waitForTimeout(4000);
    await cas.loginDuoSecurityBypassCode(page, "duocode");
    await page.waitForTimeout(4000);
    await cas.screenshot(page);
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await cas.assertCookie(page);
    await browser.close();
})();
