
const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        const response = await cas.gotoLogin(page);
        await cas.sleep(3000);
        await cas.log(`${response.status()} ${response.statusText()}`);
        assert(response.ok());

        await cas.loginWith(page);
        await cas.sleep(3000);
        await cas.assertCookie(page);
        await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");

        await cas.gotoLogout(page);
        await cas.logPage(page);
        await cas.assertPageUrl(page, "https://localhost:8443/cas/logout");
        await cas.sleep(1000);
        await cas.assertCookie(page, false);

        await cas.log("Logging in using external SAML2 identity provider...");
        await cas.gotoLogin(page);
        await cas.sleep(1000);
        await cas.click(page, "li #SAML2Client");
        await cas.waitForNavigation(page);
        await cas.loginWith(page, "user1", "password");
        await cas.sleep(3000);
        await cas.assertCookie(page);

        await cas.sleep(1000);
        await cas.goto(page, "https://localhost:8444/cas/login");
        await cas.sleep(2000);
        await cas.assertCookie(page);
        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    } finally {
        await browser.close();
    }
})();
