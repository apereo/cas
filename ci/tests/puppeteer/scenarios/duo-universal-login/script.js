const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');
const fs = require('fs');

(async () => {
    var args = process.argv.slice(2);
    let config = JSON.parse(fs.readFileSync(args[0]));
    assert(config != null)
    
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?authn_method=mfa-duo");
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    var result = new URL(page.url());
    assert(result.host === "api-d2e616a0.duosecurity.com");
    await browser.close();
})();
