const cas = require("../../cas.js");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);

    const har = await cas.startHar(page);

    await cas.gotoLogin(page);
    await cas.sleep(2000);

    await cas.click(page, "li #SAML2Client");
    await cas.waitForNavigation(page);

    await cas.loginWith(page, "user1", "password");
    await cas.sleep(2000);

    await cas.assertTextContent(page, "#titlePanel h2", "Choose Account");
    await cas.assertTextContentStartsWith(page, "#surrogateInfo", "You are provided with a list of accounts");
    await cas.assertVisibility(page, "#surrogateTarget");
    await page.select("#surrogateTarget", "morpheus");
    await cas.click(page, "#submit");
    await cas.waitForNavigation(page);
    await cas.sleep(1000);

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertInnerText(page, "#principalId", "morpheus");
    
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.stopHar(har);
    await cas.closeBrowser(browser);
})();

