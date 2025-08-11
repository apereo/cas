
const path = require("path");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    let success = false;
    try {
        const page = await cas.newPage(browser);
        await cas.log("Load identity providers on login...");
        await cas.gotoLogin(page);
        await cas.sleep(1000);

        await cas.log("Login and establish SSO...");
        await cas.gotoLogin(page);
        await cas.sleep(1000);
        await cas.loginWith(page);
        await cas.sleep(4000);

        await cas.log("Launch into a service that requires delegation");
        await cas.gotoLogin(page, "https://github.com");
        await cas.sleep(1000);
        await cas.assertVisibility(page, "#loginProviders");
        await cas.assertVisibility(page, "#existingSsoMsg");
        await cas.assertVisibility(page, "li #SAML2Client");

        await cas.submitForm(page, "li #formSAML2Client");
        await cas.sleep(5000);

        await cas.loginWith(page, "user1", "password");
        await cas.sleep(8000);
        await cas.logPage(page);
        await cas.assertPageUrlStartsWith(page, "https://github.com");

        await cas.assertTicketParameter(page);
        success = true;
    } finally {
        if (success) {
            await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
        }
        await cas.closeBrowser(browser);
    }
})();

