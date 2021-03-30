const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: false,
        args: ['--lang=de']
    });
    const page = await browser.newPage();
    await page.setExtraHTTPHeaders({
        'Accept-Language': 'de'
    });
    await page.goto("https://localhost:8443/cas/login");

    await page.waitForTimeout(1000)
    const header = await page.$eval('#content #fm1 button[name=submit]', el => el.innerText)
    console.log(header)
    assert(header == "ANMELDEN")
    await browser.close();
})();
