const puppeteer = require('puppeteer');

const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://example.com");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertVisibility(page, '#mfa-gauth')
    await cas.assertVisibility(page, '#mfa-yubikey')
    await browser.close();
})();
