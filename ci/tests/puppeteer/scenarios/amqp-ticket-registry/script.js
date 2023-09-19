const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");
const path = require("path");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        let response = await cas.goto(page, "https://localhost:8443/cas/login");
        await page.waitForTimeout(3000);
        await cas.log(`${response.status()} ${response.statusText()}`);
        assert(response.ok());

        await cas.loginWith(page);
        await page.waitForTimeout(1000);
        await cas.assertCookie(page);
        await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
        await cas.assertInnerText(page, '#content div h2', "Log In Successful");

        await cas.goto(page, "https://localhost:8443/cas/logout");
        let url = await page.url();
        await cas.log(`Page url: ${url}`);
        assert(url === "https://localhost:8443/cas/logout");
        await page.waitForTimeout(1000);
        await cas.assertCookie(page, false);

        await cas.log("Logging in using external SAML2 identity provider...");
        await cas.goto(page, "https://localhost:8443/cas/login");
        await page.waitForTimeout(1000);
        await cas.click(page, "li #SAML2Client");
        await page.waitForNavigation();
        await cas.loginWith(page, "user1", "password");
        await page.waitForTimeout(3000);
        await cas.assertCookie(page);

        await page.waitForTimeout(1000);
        await cas.goto(page, "https://localhost:8444/cas/login");
        await page.waitForTimeout(1000);
        await cas.assertCookie(page);

    } finally {
        await cas.removeDirectory(path.join(__dirname, '/saml-md'));
        await browser.close();
    }
})();
