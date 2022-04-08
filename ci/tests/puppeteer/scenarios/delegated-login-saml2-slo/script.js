const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const path = require('path');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8443/cas/login");
    await page.waitForTimeout(2000);

    await cas.click(page, "li #SAML2Client")
    await page.waitForNavigation();

    await cas.loginWith(page, "user1", "password");
    await page.waitForTimeout(2000)

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");

    console.log("Testing auto-redirection via configured cookie...")
    await cas.goto(page, "https://localhost:8443/cas/logout?service=https://apereo.github.io");
    await page.waitForTimeout(6000)

    // await cas.goto(page, "https://localhost:8443/cas/login");
    // await page.waitForTimeout(2000);
    // let url = await page.url()
    // console.log(`Page url: ${url}`)
    // await page.waitForTimeout(2000);
    // await cas.assertCookie(page, true, "Pac4jCookie");
    // await cas.assertCookie(page);
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();


