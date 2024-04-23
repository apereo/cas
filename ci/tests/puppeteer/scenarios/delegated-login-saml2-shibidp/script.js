
const cas = require("../../cas.js");
const path = require("path");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        const service = "https://localhost:9859/anything/cas";
        await cas.gotoLogin(page, service);
        await cas.sleep(3000);
        await cas.assertVisibility(page, "#loginProviders");
        await cas.assertVisibility(page, "li #SAML2Client");

        await cas.click(page, "li #SAML2Client");
        await cas.waitForNavigation(page);
        await cas.sleep(8000);
        await cas.screenshot(page);
        await page.waitForSelector("#username", {visible: true});
        await cas.loginWith(page);
        await cas.sleep(8000);
        await cas.screenshot(page);
        await cas.log("Checking for page URL...");
        await cas.logPage(page);
        const ticket = await cas.assertTicketParameter(page);
        await cas.log(`Received ticket ${ticket}`);
        const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}`);
        await cas.log(body);
        assert(body.includes("<cas:credentialType>ClientCredential</cas:credentialType>"));
        assert(body.includes("<cas:user>casuser@example.org</cas:user>"));
        assert(body.includes("<cas:isFromNewLogin>true</cas:isFromNewLogin>"));
        assert(body.includes("<cas:authenticationMethod>DelegatedClientAuthenticationHandler</cas:authenticationMethod>"));
        await cas.gotoLogin(page);
        await cas.assertCookie(page);
        await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    } finally {
        await browser.close();
    }
})();

