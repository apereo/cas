const puppeteer = require('puppeteer');

const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://apereo.github.io");
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(2000)
    await cas.assertVisibility(page, '#mfa-gauth')
    await cas.assertVisibility(page, '#mfa-webauthn')
    await browser.close();
})();
