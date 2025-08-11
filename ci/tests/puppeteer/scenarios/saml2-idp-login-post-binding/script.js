const path = require("path");
const cas = require("../../cas.js");

async function normalAuthenticationFlow(context) {
    const page = await cas.newPage(context);
    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=post-sp");
    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.loginWith(page);
    await cas.sleep(3000);
    await page.waitForSelector("#table_with_attributes", {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");

    const authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);
    await cas.sleep(1000);
    await cas.gotoLogout(page);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const context = await browser.createBrowserContext();
    await cas.log("Running test for normal authentication flow");
    await normalAuthenticationFlow(context);
    await context.close();
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();

