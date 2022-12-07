const puppeteer = require('puppeteer');
const assert = require('assert');

const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.goto(page, "https://localhost:8443/cas/login");

    // await page.waitForTimeout(2000)

    let pswd = await page.$('#password');
    assert(pswd == null);

    await cas.type(page,'#username', "casuser");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    await page.waitForTimeout(4000);

    await cas.assertInvisibility(page, '#username');
    await cas.assertVisibility(page, '#password');

    await cas.type(page,'#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    await page.waitForTimeout(4000);

    await cas.assertCookie(page);
    
    await browser.close();
})();
