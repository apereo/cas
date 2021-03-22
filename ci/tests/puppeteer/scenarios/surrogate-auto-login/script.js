const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: false,
        devtools: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    await page.type('#username', "user3+casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    
    // await page.waitForTimeout(5000)

    const header = await page.$eval('#content div h2', el => el.innerText.trim())
    console.log(header)
    assert(header === "Log In Successful")

    const p = await page.$eval('#content div p', el => el.innerText.trim())
    console.log(p)
    assert(p.startsWith("You, user3, have successfully logged into the Central Authentication Service"))

    await browser.close();
})();
