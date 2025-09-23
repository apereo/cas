
const fs = require("fs");
const path = require("path");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await cas.sleep(2000);
    await cas.loginWith(page);
    await page.waitForSelector("#table_with_attributes", {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");
    const authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);
    const artifacts = [
        "idp-metadata.xml",
        "idp-encryption.key",
        "idp-signing.key",
        "idp-encryption.crt",
        "idp-signing.crt"
    ];
    artifacts.forEach((art) => {
        const pt = path.join(__dirname, `/saml-md/${art}`);
        cas.log(`Deleting ${pt}`);
        fs.rmSync(pt, { force: true });
    });

    await cas.closeBrowser(browser);
})();

