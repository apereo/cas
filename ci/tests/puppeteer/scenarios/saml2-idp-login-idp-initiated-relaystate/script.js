
const assert = require("assert");
const path = require("path");
const cas = require("../../cas.js");

async function unsolicited(page, target) {
    const entityId = "http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp";

    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}`;
    url += `&target=${target}`;

    await cas.goto(page, url);
    await cas.sleep(8000);
    await cas.logPage(page);
    await cas.assertPageUrlContains(page, target);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const response = await cas.goto(page, "https://localhost:8443/cas/idp/metadata");
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await cas.gotoLogin(page);
    await cas.sleep(2000);

    await cas.loginWith(page);
    await cas.sleep(5000);
    
    await unsolicited(page, "https://apereo.github.io");
    await cas.sleep(5000);

    await unsolicited(page, "https://github.com/apereo/cas");
    await cas.sleep(4000);

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();
