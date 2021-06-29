const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://127.0.0.1:8443/cas/login?authn_method=mfa-inwebo");

    await cas.loginWith(page, "testcas", "password");

    await page.waitForTimeout(1000);
    const startBrowserForm = await page.$('#startBrowserForm');
    assert(startBrowserForm != null);
    const startPushForm = await page.$('#startPushForm');
    assert(startPushForm != null);

    // Asking for the PIN code
    await page.$eval('button[name=browser]', button => button.click());
    await page.waitForTimeout(1000);

    console.log("Checking for PIN code...")
    let header = await cas.innerText(page, "main h2");
    assert(header === "Fill in your PIN code:")
    const enrollForm = await page.$('#enrollForm');
    assert(enrollForm != null);

    // Let's wait for Inwebo javascript to execute
    // And redirect to error/registration
    console.log("Checking for error/registration")
    await page.waitForTimeout(5000);
    const header2 = await cas.innerText(page, 'main h2');
    assert(header2 === "An error has occurred.")

    await browser.close();
})();
