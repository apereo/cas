
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const entityId = "https://localhost:9859/anything";
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}`;
    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await cas.screenshot(page);
    await cas.sleep(4000);
    await cas.loginWith(page);
    await cas.waitForElement(page, "body");
    const content = JSON.parse(await cas.innerText(page, "body"));
    await cas.log(content);
    assert(content.form.SAMLResponse !== undefined);
    await cas.closeBrowser(browser);
})();

