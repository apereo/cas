const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    await page.type('#username', "mustchangepassword");
    await page.type('#password', "P@ssw0rd");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.waitForTimeout(2000)
    const header = await page.$eval('#pwdmain h3', el => el.innerText.trim())
    console.log(header)
    assert(header === "You must change your password.")

    await page.type('#password', "Jv!e0mKD&dCNl^Q");
    await page.type('#confirmedPassword', "Jv!e0mKD&dCNl^Q");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    const success = await page.$eval('#content h2', el => el.innerText.trim())
    console.log(success)
    assert(success === "Password Change Successful")

    await browser.close();
})();
