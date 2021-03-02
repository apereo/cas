const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?authn_method=mfa-u2f");
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    // await page.waitForTimeout(10000)
    
    let element = await page.$('#login h3');
    let header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "Authenticate Device")

    element = await page.$('#login p');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "Please touch the flashing U2F device now.")

    await browser.close();
})();
