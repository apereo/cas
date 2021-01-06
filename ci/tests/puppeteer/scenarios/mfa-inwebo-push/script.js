const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();
    await page.goto("https://127.0.0.1:8443/cas/login?authn_method=mfa-inwebo");
    await page.type('#username', "testcaspush");
    await page.type('#password', "password");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    
    let form = await page.$('#pendingCheckResultForm');
    assert(form != null);

    await browser.close();
})();
