const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const path = require('path');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        const service = "https://apereo.github.io";
        await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
        await page.waitForTimeout(3000);
        await cas.assertVisibility(page, '#loginProviders');
        await cas.assertVisibility(page, 'li #SAML2Client');

        await cas.click(page, "li #SAML2Client");
        await page.waitForNavigation();
        await page.waitForTimeout(8000);
        await cas.screenshot(page);
        await page.waitForSelector('#username', {visible: true});
        await cas.loginWith(page);
        await page.waitForTimeout(8000);
        await cas.screenshot(page);
        await cas.log("Checking for page URL...");
        await cas.logPage(page);
        let ticket = await cas.assertTicketParameter(page);
        await cas.log(`Received ticket ${ticket}`);
        const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}`);
        await cas.log(body);
        assert(body.includes('<cas:credentialType>ClientCredential</cas:credentialType>'));
        assert(body.includes('<cas:user>casuser@example.org</cas:user>'));
        assert(body.includes('<cas:isFromNewLogin>true</cas:isFromNewLogin>'));
        assert(body.includes('<cas:authenticationMethod>DelegatedClientAuthenticationHandler</cas:authenticationMethod>'));
        await cas.goto(page, `https://localhost:8443/cas/login`);
        await cas.assertCookie(page);
        await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
        await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    } finally {
        await cas.removeDirectory(path.join(__dirname, '/saml-md'));
        await browser.close();
    }
})();


