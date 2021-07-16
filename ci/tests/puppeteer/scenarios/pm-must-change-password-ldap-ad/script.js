const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "mustchangepassword", "P@ssw0rd");
    await page.waitForTimeout(2000)
    const header = await cas.innerText(page, '#pwdmain h3');

    assert(header === "Hello, mustchangepassword. You must change your password.")

    await cas.type(page,'#password', "Jv!e0mKD&dCNl^Q");
    await cas.type(page,'#confirmedPassword', "Jv!e0mKD&dCNl^Q");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    const success = await cas.innerText(page, '#content h2');
    assert(success === "Password Change Successful")

    await browser.close();
})();
