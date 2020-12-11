const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();
    await page.goto("https://127.0.0.1:8443/cas/login?authn_method=mfa-inwebo");
    await page.type('#username', "testcas");
    await page.type('#password', "password");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    
    const startBrowserForm = await page.$('#startBrowserForm');
    const startPushForm = await page.$('#startPushForm');
    assert(startBrowserForm != null);
    assert(startPushForm != null);

    // asking for the PIN code
    await page.$eval('button[name=browser]', button => button.click());
    var header = await page.$eval('main h2', el => el.innerText)
    assert(header === "Fill in your PIN code:")

    await browser.close();
})();
