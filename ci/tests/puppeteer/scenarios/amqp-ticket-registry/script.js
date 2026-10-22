
const cas = require("../../cas.js");
const path = require("path");

(async () => {
    await cas.verifyTicketRegistryAcrossNodes();

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await cas.log("Logging in using external SAML2 identity provider...");
        await cas.gotoLogin(page);
        await cas.sleep(1000);
        await cas.click(page, "li #SAML2Client");
        await cas.waitForNavigation(page);
        await cas.loginWith(page, "user1", "password");
        await cas.sleep(3000);
        await cas.assertCookie(page);

        await cas.sleep(1000);
        await cas.goto(page, "https://localhost:8444/cas/login");
        await cas.sleep(2000);
        await cas.assertCookie(page);
        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    } finally {
        await cas.closeBrowser(browser);
    }
})();
