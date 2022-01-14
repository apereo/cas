const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await page.focus("#username");
    await page.keyboard.press("Tab");
    await page.focus("#password");
    await page.keyboard.press("Tab");

    await cas.assertVisibility(page, "#usernameValidationMessage");
    await cas.assertVisibility(page, "#passwordValidationMessage");

    await cas.loginWith(page, "casuser", "Mellon");

    await cas.assertTicketGrantingCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await browser.close();
})();
