const path = require("path");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const entityIds = [
        "https://sp.example.org/saml1",
        "https://sp.example.org/saml2",
        "https://sp.example.org/saml3"
    ];

    for (const entityId of entityIds) {
        await cas.gotoLogout(page);
        const url = `https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO?providerId=${entityId}`;
        await cas.log(`Navigating to ${url}`);
        await cas.goto(page, url);
        await cas.screenshot(page);
        await cas.sleep(4000);
        await cas.loginWith(page);
        await cas.sleep(4000);
        await cas.logPage(page);
        await cas.gotoLogout(page);
    }

    await cas.closeBrowser(browser);
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
})();

