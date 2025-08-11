
const path = require("path");
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Sending SP passive authentication request...");
    let response = await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await cas.sleep(3000);
    await cas.screenshot(page);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await cas.log("Establishing SSO session...");
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);

    await cas.log("Sending SP passive authentication request with single sign-on session...");
    response = await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await cas.sleep(3000);
    await cas.screenshot(page);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await page.waitForSelector("#table_with_attributes", {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");

    const authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);
    
    await cas.sleep(1000);
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);

})();
