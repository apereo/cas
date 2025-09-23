
const path = require("path");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    const entityId = "http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/signed-sp";
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}`;
    url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin%3Flocale%3Den";
    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await cas.screenshot(page);
    await cas.sleep(4000);
    await cas.loginWith(page);
    await cas.sleep(4000);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();
