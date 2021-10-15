const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    console.log("Removing previous consent decisions for casuser");
    await cas.doRequest("https://localhost:8443/cas/actuator/attributeConsent/casuser", "DELETE");

    console.log("Establishing SSO session...");
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");

    let entityId = "https://httpbin.org/shibboleth";
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}`;
    url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin";
    await page.goto(url);
    await page.waitForTimeout(1000)
    await cas.assertTextContent(page, '#content h2', "Attribute Consent");
    await cas.screenshot(page);
    await cas.submitForm(page, "#fm1");
    await page.waitForTimeout(2000)
    console.log(page.url());
    assert(page.url().startsWith("https://httpbin.org/post"))

    await cas.uploadSamlMetadata(page, path.join(__dirname, '/saml-md/idp-metadata.xml'));
    await page.goto("https://samltest.id/start-idp-test/");
    await cas.type(page, 'input[name=\'entityID\']', "https://cas.apereo.org/saml/idp");
    await cas.click(page, "input[type='submit']")
    await page.waitForNavigation();
    await page.waitForTimeout(1000)
    await cas.assertTextContent(page, '#content h2', "Attribute Consent");
    await cas.screenshot(page);
    await cas.submitForm(page, "#fm1");
    await page.waitForTimeout(1000)

    await page.waitForSelector('div.entry-content p', {visible: true});
    await cas.assertInnerTextStartsWith(page, "div.entry-content p",
        "Your browser has completed the full SAML 2.0 round-trip");
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();

