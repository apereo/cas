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

    await page.waitForTimeout(3000);
    await cas.loginWith(page, "user1", "password");

    await cas.screenshot(page);
    await cas.assertCookie(page, false);

    await page.waitForTimeout(2000);
    let url = await page.url();
    await cas.log(`Page URL: ${url}`);
    await cas.assertParameter(page, "client_name");
    assert(url.includes("https://localhost:8443/cas/login"));
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();


