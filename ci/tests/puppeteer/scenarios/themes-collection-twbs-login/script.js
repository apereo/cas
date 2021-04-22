const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: false,
        devtools: true,
        slowMo: 500
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    await page.keyboard.press('CapsLock');
    await page.type('#password', "M");
    
    const isNotHidden = await page.$eval('.caps-warn', (elem) => {
        return elem.style.display !== 'none';
    });
    console.log(isNotHidden)
    assert(isNotHidden === true);


    await browser.close();
})();
