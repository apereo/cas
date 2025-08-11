
const path = require("path");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    try {
        await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
        await cas.sleep(2000);
        await cas.screenshot(page);
        await cas.loginWith(page);
        await cas.sleep(2000);
        await cas.screenshot(page);
        await page.waitForSelector("#table_with_attributes", {visible: true});
        await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
        await cas.assertVisibility(page, "#table_with_attributes");

        const authData = JSON.parse(await cas.innerHTML(page, "details pre"));
        await cas.log(authData);

        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    } finally {
        await cas.screenshot(page);
    }
    await cas.closeBrowser(browser);
})();
