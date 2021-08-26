const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await cas.loginWith(page, "testuser", "testuser");
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt")
    await cas.submitForm(page, "#fm1");
    await cas.assertTextContent(page, "#content h1", "Authentication Succeeded with Warnings")
    await cas.submitForm(page, "#form");
    await cas.assertTicketGrantingCookie(page);
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await browser.close();
})();
