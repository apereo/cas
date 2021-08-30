const puppeteer = require('puppeteer');
const assert = require('assert');
const path = require('path');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)

    await cas.uploadSamlMetadata(page, path.join(__dirname, '/saml-md/sp-metadata.xml'));

    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(3000);
    await cas.loginWith(page, "casuser", "Mellon");

    await page.goto("https://localhost:8443/cas/login?service=https://github.com");
    await page.waitForTimeout(3000);

    await cas.assertVisibility(page, '#loginProviders')
    await cas.assertVisibility(page, '#existingSsoMsg')
    await cas.assertVisibility(page, 'li #SAML2Client')

    await cas.click(page, "li #SAML2Client")
    await page.waitForNavigation();

    await page.waitForTimeout(2000)

    await cas.loginWith(page, "morty", "panic");
    await page.waitForTimeout(3000)

    await cas.click(page, "input[name='_eventId_proceed']")
    await page.waitForTimeout(8000)

    const url = await page.url()
    console.log(`Page url: ${url}`)
    assert(url.startsWith("https://github.com/"))

    await cas.assertTicketParameter(page);
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();


