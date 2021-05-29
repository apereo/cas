const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    
    await page.goto("https://localhost:8443/cas/login");

    // await page.waitForTimeout(2000)

    let pswd = await page.$('#password');
    assert(pswd == null);

    await page.type('#username', "casuser");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    // await page.waitForTimeout(8000)

    await cas.assertInvisibility(page, '#username')
    await cas.assertVisibility(page, '#password');

    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    // await page.waitForTimeout(10000)

    const tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);
    
    await browser.close();
})();
