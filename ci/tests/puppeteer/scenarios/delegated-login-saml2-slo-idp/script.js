const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const path = require('path');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8444");
    await page.waitForTimeout(1000);

    console.log("Accessing protected CAS application");
    await cas.goto(page, "https://localhost:8444/protected");
    let url = await page.url();
    console.log(`Page url: ${url}`);

    // await cas.goto(page, "https://localhost:8443/cas/login?locale=en");
    await page.waitForTimeout(2000);
    await cas.screenshot(page);

    await cas.assertVisibility(page, '#loginProviders');
    await cas.assertVisibility(page, 'li #SAML2Client');

    console.log("Choosing SAML2 identity provider for login...");
    await cas.click(page, "li #SAML2Client");
    await page.waitForNavigation();

    await cas.loginWith(page, "user1", "password");
    await page.waitForTimeout(2000);

    console.log("Checking CAS application access...");
    url = await page.url();
    console.log(`Page url: ${url}`);
    await cas.screenshot(page);
    assert(url.startsWith("https://localhost:8444/protected"));
    await cas.assertInnerTextContains(page, "div.starter-template h2 span", "user1@example.com");

    console.log("Checking CAS SSO session...");
    await cas.goto(page, "https://localhost:8443/cas/login?locale=en");
    await cas.screenshot(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await cas.assertCookie(page, true, "Pac4jCookie");
    await page.waitForTimeout(3000);

    console.log("Invoking SAML2 identity provider SLO...");
    await cas.goto(page, "http://localhost:9443/simplesaml/saml2/idp/SingleLogoutService.php?ReturnTo=https://apereo.github.io");
    await page.waitForTimeout(5000);
    url = await page.url();
    console.log(`Page url: ${url}`);

    await cas.goto(page, "https://localhost:8443/cas/login?locale=en");
    await cas.assertCookie(page, false);

    console.log("Accessing protected CAS application");
    await cas.goto(page, "https://localhost:8444/protected");
    await page.waitForTimeout(3000);
    await cas.screenshot(page);
    url = await page.url();
    console.log(`Page url: ${url}`);
    await cas.screenshot(page);
    assert(url.startsWith("http://localhost:9443/simplesaml"));
    await cas.assertVisibility(page, '#username');
    await cas.assertVisibility(page, '#password');
    const title = await page.title();
    console.log(title);
    assert(title === "Enter your username and password");
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();


