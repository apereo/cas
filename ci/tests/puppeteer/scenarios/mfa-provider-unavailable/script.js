const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertInnerText(page, "#content h2", "MFA Provider Unavailable")
    await cas.assertInnerTextStartsWith(page, "#content p", "CAS was unable to reach your configured MFA provider at this time.")
    await browser.close();
})();
