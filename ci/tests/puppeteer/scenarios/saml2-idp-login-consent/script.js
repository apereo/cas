const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    await cas.log("Removing previous consent decisions for casuser");
    await cas.doRequest("https://localhost:8443/cas/actuator/attributeConsent/casuser", "DELETE");

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    let entityId = "https://localhost:9859/shibboleth";
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}`;
    url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin";
    await cas.goto(page, url);
    await page.waitForTimeout(4000);
    await cas.loginWith(page);
    await page.waitForTimeout(2000);
    await cas.assertTextContent(page, '#content h2', "Attribute Consent");
    await cas.screenshot(page);
    await cas.submitForm(page, "#fm1");
    await page.waitForTimeout(6000);
    await cas.log(await page.url());
    await cas.screenshot(page);
    assert(await page.url().startsWith("https://localhost:9859/post"));
    let content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    await cas.log(payload);
    assert(payload.form.RelayState != null);
    assert(payload.form.SAMLResponse != null);
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();

