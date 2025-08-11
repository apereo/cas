
const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const entityIds = [
        "sp1:example",
        "sp2:example"
    ];
    for (const entityId of entityIds) {
        const url = `https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO?providerId=${entityId}`;
        await cas.log(`Navigating to ${url}`);
        await cas.goto(page, url);
        await cas.screenshot(page);
        await cas.sleep(2000);
        await cas.loginWith(page);
        await cas.sleep(3000);
        const content = JSON.parse(await cas.innerText(page, "body"));
        await cas.log(content);
        assert(content.form.SAMLResponse !== undefined);
        await cas.gotoLogout(page);
    }
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();

