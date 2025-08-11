
const cas = require("../../cas.js");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    try {
        await cas.gotoLogin(page);
        await cas.sleep(1000);

        await cas.doRequest("https://localhost:8443/cas/sp/metadata", "GET", {}, 200);
        await cas.doRequest("https://localhost:8443/cas/sp/idp/metadata", "GET", {}, 200);

        await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
        await cas.sleep(1000);

        await cas.assertVisibility(page, "li #SAML2Client");
        await cas.click(page, "li #SAML2Client");
        await cas.sleep(6000);

        await cas.loginWith(page, "user1", "password");
        await cas.sleep(2000);

        await cas.log("Checking for page URL...");
        await cas.logPage(page);

        await page.waitForSelector("#table_with_attributes", {visible: true});
        await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
        await cas.assertVisibility(page, "#table_with_attributes");
        const authData = JSON.parse(await cas.innerHTML(page, "details pre"));
        await cas.log(authData);

        await cas.gotoLogin(page);
        await cas.assertCookie(page);
        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    } finally {
        await cas.closeBrowser(browser);
    }
})();
