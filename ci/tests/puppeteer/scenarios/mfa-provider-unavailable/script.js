const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    
    const header = await page.$eval('#content h2', el => el.innerText)
    console.log(header)
    assert(header === "MFA Provider Unavailable")

    const sub = await page.$eval('#content p', el => el.innerText)
    console.log(sub)
    assert(sub.startsWith("CAS was unable to reach your configured MFA provider at this time."))


    await browser.close();
})();
