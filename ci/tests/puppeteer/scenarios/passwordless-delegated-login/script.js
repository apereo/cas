const puppeteer = require('puppeteer');
const assert = require('assert');

const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await page.goto("https://localhost:8443/cas/login");

    // await page.waitForTimeout(5000)

    let pswd = await page.$('#password');
    assert(pswd == null);

    await cas.type(page,'#username', "casuser");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    // await page.waitForTimeout(5000)

    const url = await page.url()
    console.log(`Page url: ${url}`)
    assert(url.startsWith("https://github.com/"))
    
    await browser.close();
})();
