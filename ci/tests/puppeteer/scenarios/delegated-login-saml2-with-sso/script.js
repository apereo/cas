const puppeteer = require('puppeteer');
const assert = require('assert');
const path = require('path');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    try {
        await cas.log("Load identity providers on login...");
        await cas.goto(page, "https://localhost:8443/cas/login");
        await page.waitForTimeout(1000);

        await cas.log("Login and establish SSO...");
        await cas.goto(page, "https://localhost:8443/cas/login");
        await page.waitForTimeout(1000);
        await cas.loginWith(page, "casuser", "Mellon");
        await page.waitForTimeout(1000);

        await cas.log("Launch into a service that requires delegation");
        await cas.goto(page, "https://localhost:8443/cas/login?service=https://github.com");
        await page.waitForTimeout(1000);

        await cas.assertVisibility(page, '#loginProviders');
        await cas.assertVisibility(page, '#existingSsoMsg');
        await cas.assertVisibility(page, 'li #SAML2Client');

        await cas.submitForm(page, "li #formSAML2Client");
        await page.waitForTimeout(2000);

        await cas.loginWith(page, "user1", "password");
        await page.waitForTimeout(2000);

        const url = await page.url();
        await cas.log(`Page url: ${url}`);
        assert(url.startsWith("https://github.com/"));

        await cas.assertTicketParameter(page);
    } finally {
        await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    }
    await browser.close();
})();


