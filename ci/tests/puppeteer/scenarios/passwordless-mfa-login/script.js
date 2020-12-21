const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    
    await page.goto("https://localhost:8443/cas/login");

    // await page.waitForTimeout(2000)

    var pswd = await page.$('#password');
    assert(pswd == null);

    await page.type('#username', "casuser");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    // await page.waitForTimeout(5000)

    const header = await page.$eval('#login h3', el => el.innerText)
    console.log(header)
    assert(header === "Use your registered YubiKey device(s) to authenticate.")

    await browser.close();
})();
