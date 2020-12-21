const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    
    await page.goto("https://localhost:8443/cas/login");

    // await page.waitForTimeout(2000)

    var pswd = await page.$('#password');
    assert(pswd == null);

    await page.type('#username', "casuser");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    // await page.waitForTimeout(8000)

    var username = await page.$('#username');
    assert(await username.boundingBox() == null);

    pswd = await page.$('#password');
    assert(await pswd.boundingBox() != null);

    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    // await page.waitForTimeout(10000)

    const tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);
    
    await browser.close();
})();
