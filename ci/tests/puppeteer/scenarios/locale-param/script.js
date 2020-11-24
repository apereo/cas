const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?locale=de");

    const header = await page.$eval('#content #fm1 button[name=submit]', el => el.innerText)
    console.log(header)
    assert(header == "ANMELDEN")
    
    await browser.close();
})();
