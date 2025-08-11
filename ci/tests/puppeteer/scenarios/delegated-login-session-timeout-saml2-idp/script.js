
const cas = require("../../cas.js");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.sleep(2000);

    await cas.assertVisibility(page, "#loginProviders");
    await cas.assertVisibility(page, "li #SAML2Client");
    
    await cas.click(page, "li #SAML2Client");
    await cas.waitForNavigation(page);

    await cas.sleep(3000);
    await cas.loginWith(page, "user1", "password");

    await cas.screenshot(page);
    await cas.assertCookie(page, false);

    await cas.sleep(2000);
    await cas.logPage(page);
    await cas.assertParameter(page, "client_name");
    await cas.assertPageUrlContains(page, "https://localhost:8443/cas/login");
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();

