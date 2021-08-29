const puppeteer = require('puppeteer');
const assert = require('assert');
const path = require('path');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.uploadSamlMetadata(page, path.join(__dirname, '/saml-md/idp-metadata.xml'));

    // await page.waitForTimeout(1000)

    await page.goto("https://samltest.id/start-idp-test/");
    await cas.type(page,'input[name=\'entityID\']', "https://cas.apereo.org/saml/idp");
    // await page.waitForTimeout(1000)
    await cas.click(page, "input[type='submit']")
    await page.waitForNavigation();

    // await page.waitForTimeout(1000)

    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForSelector('div.entry-content p', { visible: true });
    await cas.assertInnerTextStartsWith(page, "div.entry-content p", "Your browser has completed the full SAML 2.0 round-trip");
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    const endpoints = ["health", "samlIdPRegisteredServiceMetadataCache?serviceId=Sample&entityId=https://samltest.id/saml/sp"];
    const baseUrl = "https://localhost:8443/cas/actuator/"
    for (let i = 0; i < endpoints.length; i++) {
        let url = baseUrl + endpoints[i];
        const response = await page.goto(url);
        console.log(`${response.status()} ${response.statusText()}`)
        assert(response.ok())
    }

    await browser.close();
})();


