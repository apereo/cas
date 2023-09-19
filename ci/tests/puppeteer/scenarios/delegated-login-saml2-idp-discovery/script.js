const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const path = require('path');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.click(page, "#saml2IdPDiscovery");
    await page.waitForTimeout(5000);
    await cas.type(page, "#idpSelectInput", "simplesaml");
    await page.waitForTimeout(1000);
    await cas.pressEnter(page);
    await page.waitForTimeout(4000);
    await cas.log(`Page url: ${ await page.url()}`);

    await cas.loginWith(page, "user1", "password");
    await page.waitForTimeout(2000);

    await cas.log(`Page url: ${ await page.url()}`);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await cas.assertCookie(page);
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();
