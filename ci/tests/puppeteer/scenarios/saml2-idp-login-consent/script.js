
const path = require("path");
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    await cas.log("Removing previous consent decisions for casuser");
    await cas.doDelete("https://localhost:8443/cas/actuator/attributeConsent/casuser");

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    const entityId = "https://localhost:9859/shibboleth";
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin`;
    await cas.goto(page, url);
    await cas.sleep(4000);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertTextContent(page, "#content h2", "Attribute Consent");
    await cas.screenshot(page);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(6000);
    await cas.logPage(page);
    await cas.screenshot(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:9859/post");
    const content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    await cas.log(payload);
    assert(payload.form.RelayState !== undefined);
    assert(payload.form.SAMLResponse !== undefined);
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await browser.close();
})();

