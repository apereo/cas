const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    await page.type('#username', "invalidlocation");
    await page.type('#password', "invalidlocation");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    // await page.waitForTimeout(5000)

    const header = await page.$eval('#content h2', el => el.innerText)
    console.log(header)
    assert(header === "You cannot login from this workstation.")
    
    await browser.close();
})();
