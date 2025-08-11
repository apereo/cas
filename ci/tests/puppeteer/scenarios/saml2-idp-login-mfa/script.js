
const path = require("path");
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Establishing SSO session...");
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await cas.sleep(8000);
    await cas.screenshot(page);
    const code = await cas.extractFromEmail(browser);

    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(9000);
    await cas.screenshot(page);
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");

    let authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);
    const initialAuthData = authData.AuthnInstant;
    await cas.logg(`Initial authentication instant: ${initialAuthData}`);
    await cas.deleteCookies(page);

    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await cas.sleep(4000);

    authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);
    const nextAuthData = authData.AuthnInstant;
    await cas.logg(`Second authentication instant: ${nextAuthData}`);
    assert(nextAuthData !== initialAuthData);

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();

