const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?service=https://example.com");

    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    let gauth = await page.$('#mfa-gauth');
    assert(await gauth.boundingBox() != null);

    let yb = await page.$('#mfa-yubikey');
    assert(await yb.boundingBox() != null);

    await browser.close();
})();
