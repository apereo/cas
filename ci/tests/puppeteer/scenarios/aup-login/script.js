const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?service=https://example.org");

    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    // await page.waitForTimeout(5000)
    
    let element = await page.$('#main-content #login #fm1 h3');
    const header = await page.evaluate(element => element.textContent, element);
    console.log(header)
    assert(header === "Acceptable Usage Policy")

    let accept = await page.$('button[name=submit]');
    assert(await accept.boundingBox() != null);

    let cancel = await page.$('button[name=cancel]');
    assert(await cancel.boundingBox() != null);

    await browser.close();
})();
