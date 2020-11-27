const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?service=unknown-service");

    const header = await page.$eval('#content h2', el => el.innerText)
    console.log(header)
    assert(header === "Application Not Authorized to Use CAS")
    
    await browser.close();
})();
