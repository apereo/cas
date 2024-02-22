const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        const response = await cas.gotoLogin(page);
        await cas.log(`${response.status()} ${response.statusText()}`);
        assert(response.ok());
        await cas.loginWith(page);
        await cas.waitForTimeout(3000);
        await cas.waitForElement(page, "#content div h2");
        await cas.assertCookie(page);
        await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");

        await cas.gotoLogout(page);
        await cas.logPage(page);
        const url = await page.url();
        assert(url === "https://localhost:8443/cas/logout");

        await cas.assertCookie(page, false);

        await cas.log("Logging in using external SAML2 identity provider...");
        await cas.gotoLogin(page);

        await cas.click(page, "li #SAML2Client");
        await page.waitForNavigation();
        await cas.loginWith(page, "user1", "password");
        await cas.waitForTimeout(4000);
        await cas.assertCookie(page);
        await cas.goto(page, "https://localhost:8444/cas/login");
        await cas.assertCookie(page);

    } finally {
        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
        await browser.close();
    }
})();
