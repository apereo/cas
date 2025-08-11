
const path = require("path");
const cas = require("../../cas.js");

(async () => {

    await cas.log("Removing previous consent decisions for casuser");
    await cas.doDelete("https://localhost:8443/cas/actuator/attributeConsent/casuser");

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Establishing SSO session...");
    await cas.gotoLogin(page);
    await cas.sleep(2000);
    await cas.loginWith(page);

    const entityId = "https://localhost:9859/shibboleth";
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}`;
    url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin";
    await cas.goto(page, url);
    await cas.sleep(5000);
    await cas.assertTextContent(page, "#content h2", "Attribute Consent");
    await cas.screenshot(page);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(6000);
    await cas.logPage(page);
    await cas.screenshot(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:9859/post");

    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await cas.sleep(4000);
    await cas.screenshot(page);
    await cas.assertTextContent(page, "#content h2", "Attribute Consent");
    await cas.submitForm(page, "#fm1");
    await cas.sleep(5000);
    await cas.screenshot(page);
    await page.waitForSelector("#table_with_attributes", {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");
    const authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();

