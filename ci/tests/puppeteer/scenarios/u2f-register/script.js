const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?authn_method=mfa-u2f");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertTextContent(page, "#login h3", "Register Device")
    await cas.assertTextContent(page, "#login p", "Please touch the flashing U2F device now.")
    await browser.close();
})();
