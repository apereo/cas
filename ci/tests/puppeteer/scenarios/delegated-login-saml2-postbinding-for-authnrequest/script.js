const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const path = require("path");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);

    await cas.assertVisibility(page, "#loginProviders");
    await cas.assertVisibility(page, "li #SAML2Client");
    
    await cas.click(page, "li #SAML2Client");
    await cas.waitForNavigation(page);

    await cas.loginWith(page, "user1", "password");
    await cas.waitForElement(page, "#content div h2");
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page, true, "Pac4jCookie");

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await browser.close();
})();
