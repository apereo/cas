const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    await page.type('#username', "mustchangepassword");
    await page.type('#password', "P@ssw0rd");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    // await page.waitForTimeout(5000)

    const title = await page.title();
    console.log(title)
    assert(title === "You must change your password.")

//    let element = await page.$('#main-content #login #fm1 h3');
//    const header = await page.evaluate(element => element.textContent, element);
//    console.log(header)
//    assert(header === "You must change your password.")
//
//    let accept = await page.$('button[name=submit]');
//    assert(await accept.boundingBox() != null);
//
//    let cancel = await page.$('button[name=cancel]');
//    assert(await cancel.boundingBox() != null);

    await browser.close();
})();
