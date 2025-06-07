
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

    await cas.loginWith(page, "user1", "password");
    await cas.sleep(2000);

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page, true, "Pac4jCookie");

    await cas.log("Testing auto-redirection via configured cookie...");
    await cas.gotoLogout(page);
    await cas.sleep(3000);
    await cas.gotoLogin(page);
    await cas.sleep(2000);
    await cas.logPage(page);
    await cas.sleep(3000);
    await cas.assertPageUrlStartsWith(page, "http://localhost:9443/simplesaml/");
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await browser.close();
})();

