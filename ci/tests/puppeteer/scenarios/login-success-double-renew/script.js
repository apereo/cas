const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await page.focus("#username");
    await page.keyboard.press("Tab");
    await page.focus("#password");
    await page.keyboard.press("Tab");

    await cas.assertVisibility(page, "#usernameValidationMessage");
    await cas.assertVisibility(page, "#passwordValidationMessage");

    await cas.loginWith(page);

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");

    for (let i = 0; i < 2; i++) {
        await cas.goto(page, "https://localhost:8443/cas/login?renew=true");
        await page.waitForTimeout(1000);

        await cas.assertVisibility(page, "#existingSsoMsg");
    }

    await browser.close();
})();
