const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {

    await cas.log("Removing previous consent decisions for casuser");
    await cas.doRequest("https://localhost:8443/cas/actuator/attributeConsent/casuser", "DELETE");

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Establishing SSO session...");
    await cas.goto(page, "https://localhost:8443/cas/login");
    await page.waitForTimeout(2000);
    await cas.loginWith(page, "casuser", "Mellon");

    let entityId = "https://localhost:9859/shibboleth";
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}`;
    url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin";
    await cas.goto(page, url);
    await page.waitForTimeout(1000);
    await cas.assertTextContent(page, '#content h2', "Attribute Consent");
    await cas.screenshot(page);
    await cas.submitForm(page, "#fm1");
    await page.waitForTimeout(2000);
    await cas.log(page.url());
    assert(page.url().startsWith("https://localhost:9859/post"));

    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await page.waitForTimeout(1000);
    await cas.assertTextContent(page, '#content h2', "Attribute Consent");
    await cas.screenshot(page);
    await cas.submitForm(page, "#fm1");
    await page.waitForTimeout(2000);
    await cas.screenshot(page);
    await page.waitForSelector('#table_with_attributes', {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");
    let authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);

    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();

