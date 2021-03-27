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

    await page.waitForTimeout(10000)
    
    const p = await page.$eval('#loginErrorsPanel p', el => el.innerText)
    console.log(p);
    assert(p.startsWith("Authentication attempt for your account is denied"));
    
    await browser.close();
})();
