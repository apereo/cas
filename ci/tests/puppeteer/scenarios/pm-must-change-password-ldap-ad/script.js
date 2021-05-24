const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();

    // Configure the navigation timeout to infinite if debugging
    // await page.setDefaultNavigationTimeout(0);
    await page.goto("https://localhost:8443/cas/login");

    await page.type('#username', "mustchangepassword");
    await page.type('#password', "P@ssw0rd");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    // await page.waitForTimeout(5000)

    const header = await page.$eval('#pwdmain h3', el => el.innerText)
    console.log(header)
    assert(header === "You must change your password.")

    await page.type('#password', "Jv!e0mKD&dCNl^Q");
    await page.type('#confirmedPassword', "Jv!e0mKD&dCNl^Q");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    await browser.close();
})();
