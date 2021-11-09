const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "mustchangepswd", "mustchangepswd");
    await cas.assertInnerText(page, "#pwdmain h3", "Hello, mustchangepswd. You must change your password.")
    await cas.assertInnerText(page, "#pwddesc", "Please change your password.")
    await browser.close();
})();
