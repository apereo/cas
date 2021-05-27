const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.setDefaultNavigationTimeout(0);
    await page.goto("https://localhost:8443/cas/login?authn_method=mfa-gauth");
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.waitForTimeout(1000)

    console.log("Using scratch code to login...");
    await page.type('#token', "83766843");
    await page.$eval('#fm1', form => form.submit());
    await page.waitForTimeout(1000)

    await page.$eval('#deviceName', el => el.value = '');
    await page.type('#deviceName', "My Trusted Device");
    
    let expiration = await page.$('#expiration');
    assert(await expiration.boundingBox() == null);

    let timeUnit = await page.$('#timeUnit');
    assert(await timeUnit.boundingBox() != null);

    await page.$eval('#registerform', form => form.submit());
    await page.waitForTimeout(1000)

    const header = await page.$eval('#content div h2', el => el.innerText.trim())
    console.log(header)
    assert(header === "Log In Successful")

    await browser.close();
})();
