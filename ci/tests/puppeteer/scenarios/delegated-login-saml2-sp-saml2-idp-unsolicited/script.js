
const cas = require("../../cas.js");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);

        await cas.gotoLogin(page);
        await cas.sleep(1000);

        await cas.doRequest("https://localhost:8443/cas/sp/metadata", "GET", {}, 200);
        await cas.doRequest("https://localhost:8443/cas/sp/idp/metadata", "GET", {}, 200);

        const spEntityId = "cas:apereo:pac4j:saml";
        await cas.goto(page, `http://localhost:9443/simplesaml/saml2/idp/SSOService.php?spentityid=${spEntityId}`);
        await cas.sleep(2000);

        await cas.screenshot(page);
        await cas.loginWith(page, "user1", "password");
        await cas.sleep(2000);

        await cas.log("Checking for page URL...");
        await cas.logPage(page);
        await cas.screenshot(page);
        await cas.assertTicketParameter(page);

        await cas.screenshot(page);
        await cas.gotoLogin(page);
        await cas.assertCookie(page);

        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    } finally {
        await cas.closeBrowser(browser);
    }
})();
