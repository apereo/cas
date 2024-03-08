
const cas = require("../../cas.js");
const path = require("path");
const assert = require("assert");

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
    await cas.sleep(8000);
    await cas.screenshot(page);
    await cas.logPage(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page, true, "Pac4jCookie");
    await cas.screenshot(page);

    const service = "https://localhost:9859/anything/sample1";
    await cas.gotoLogin(page, service);
    const ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    const json = JSON.parse(body);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes.email[0] === "Hello-user1@example.com");

    await cas.log("Testing auto-redirection via configured cookie...");
    await cas.gotoLogout(page);
    await cas.sleep(3000);
    await cas.gotoLogin(page);
    await cas.sleep(2000);
    const url = await page.url();
    await cas.logPage(page);
    await cas.sleep(3000);
    assert(url.startsWith("http://localhost:9443/simplesaml/"));
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await browser.close();
})();

