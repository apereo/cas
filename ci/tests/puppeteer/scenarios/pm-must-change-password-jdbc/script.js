const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, "mustchangepswd", "password");
    await page.waitForTimeout(2000);
    await cas.assertInnerText(page, "#pwdmain h3", "Hello, mustchangepswd. You must change your password.");
    await cas.type(page,'#password', "Jv!e0mKD&dCNl^Q");
    await cas.type(page,'#confirmedPassword', "Jv!e0mKD&dCNl^Q");
    await await cas.pressEnter(page);
    await page.waitForNavigation();
    await cas.assertInnerText(page, "#content h2", "Password Change Successful");
    await cas.goto(page, "https://localhost:8443/cas/logout");
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, "mustchangepswd", "Jv!e0mKD&dCNl^Q");
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await browser.close();
})();
