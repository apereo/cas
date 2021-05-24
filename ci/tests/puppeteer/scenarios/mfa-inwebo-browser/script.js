const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?authn_method=mfa-inwebo");
    await page.type('#username', "testcas");
    await page.type('#password', "password");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.waitForTimeout(1000);
    const startBrowserForm = await page.$('#startBrowserForm');
    assert(startBrowserForm != null);
    const startPushForm = await page.$('#startPushForm');
    assert(startPushForm != null);

    // Asking for the PIN code
    await page.$eval('button[name=browser]', button => button.click());
    await page.waitForTimeout(2000);
    const header = await page.$eval('main h2', el => el.innerText)
    assert(header === "Fill in your PIN code:")

    // Let's wait for Inwebo javascript to execute
    // And redirect to error/registration
    await page.waitForTimeout(5000);
    const header2 = await page.$eval('main h2', el => el.innerText)
    assert(header2 === "An error has occurred.")
    const enrollForm = await page.$('#enrollForm');
    assert(enrollForm != null);

    await browser.close();
})();
