const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const path = require('path');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await page.waitForTimeout(2000);

    await cas.assertVisibility(page, '#loginProviders');
    await cas.assertVisibility(page, 'li #SAML2Client');
    
    await cas.click(page, "li #SAML2Client");
    await page.waitForNavigation();

    await cas.loginWith(page, "user1", "password");
    await page.waitForTimeout(8000);
    await cas.screenshot(page);
    await cas.logPage(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await cas.assertCookie(page, true, "Pac4jCookie");
    await cas.screenshot(page);

    const service = "https://localhost:9859/anything/sample1";
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    let ticket = await cas.assertTicketParameter(page);
    let body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    let json = JSON.parse(body);
    let authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes.email[0] === "Hello-user1@example.com");

    await cas.log("Testing auto-redirection via configured cookie...");
    await cas.gotoLogout(page);
    await page.waitForTimeout(3000);
    await cas.gotoLogin(page);
    await page.waitForTimeout(2000);
    let url = await page.url();
    await cas.logPage(page);
    await page.waitForTimeout(3000);
    assert(url.startsWith("http://localhost:9443/simplesaml/"));
    await cas.removeDirectoryOrFile(path.join(__dirname, '/saml-md'));
    await browser.close();
})();


