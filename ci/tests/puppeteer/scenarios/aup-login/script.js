const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://example.org");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertTextContent(page, "#main-content #login #fm1 h3", "Acceptable Usage Policy")
    await cas.assertVisibility(page, 'button[name=submit]')
    await cas.assertVisibility(page, 'button[name=cancel]')
    await browser.close();
})();
