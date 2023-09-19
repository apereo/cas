const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const path = require('path');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8443/cas/login?locale=en");
    await page.waitForTimeout(2000);

    await cas.assertVisibility(page, '#loginProviders');
    await cas.assertVisibility(page, 'li #SAML2Client');
    
    await cas.click(page, "li #SAML2Client");
    await page.waitForNavigation();

    await cas.loginWith(page, "user1", "password");
    await page.waitForTimeout(2000);

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await cas.assertCookie(page, true, "Pac4jCookie");

    await cas.log("Testing auto-redirection via configured cookie...");
    await cas.goto(page, "https://localhost:8443/cas/logout");
    await page.waitForTimeout(3000);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await page.waitForTimeout(2000);
    let url = await page.url();
    await cas.log(`Page url: ${url}`);
    await page.waitForTimeout(3000);
    assert(url.startsWith("http://localhost:9443/simplesaml/"));
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();


