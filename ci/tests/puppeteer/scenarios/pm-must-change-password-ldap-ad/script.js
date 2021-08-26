const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "mustchangepassword", "P@ssw0rd");
    await page.waitForTimeout(2000)

    await cas.assertInnerText(page, "#pwdmain h3", "Hello, mustchangepassword. You must change your password.")
    await cas.type(page,'#password', "Jv!e0mKD&dCNl^Q");
    await cas.type(page,'#confirmedPassword', "Jv!e0mKD&dCNl^Q");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await cas.assertInnerText(page, "#content h2", "Password Change Successful")
    await browser.close();
})();
