const puppeteer = require('puppeteer');
const assert = require('assert');
const path = require('path');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    try {
        await cas.log("Load identity providers on login...");
        await cas.gotoLogin(page);
        await page.waitForTimeout(1000);

        await cas.log("Login and establish SSO...");
        await cas.gotoLogin(page);
        await page.waitForTimeout(1000);
        await cas.loginWith(page);
        await page.waitForTimeout(1000);

        await cas.log("Launch into a service that requires delegation");
        await cas.gotoLogin(page, "https://github.com");
        await page.waitForTimeout(1000);

        await cas.assertVisibility(page, '#loginProviders');
        await cas.assertVisibility(page, '#existingSsoMsg');
        await cas.assertVisibility(page, 'li #SAML2Client');

        await cas.submitForm(page, "li #formSAML2Client");
        await page.waitForTimeout(2000);

        await cas.loginWith(page, "user1", "password");
        await page.waitForTimeout(2000);

        const url = await page.url();
        await cas.logPage(page);
        assert(url.startsWith("https://github.com/"));

        await cas.assertTicketParameter(page);
    } finally {
        await cas.removeDirectoryOrFile(path.join(__dirname, '/saml-md'));
    }
    await browser.close();
})();


