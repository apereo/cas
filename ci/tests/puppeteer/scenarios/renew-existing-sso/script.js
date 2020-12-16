const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    
    await page.goto("https://localhost:8443/cas/login?service=https://example.com");
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)
    const tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);

    await page.goto("https://localhost:8443/cas/login?service=https://example.com&renew=true");
    await page.waitForTimeout(1000)

    let span = await page.$('#existingSsoMsg');
    assert(await span.boundingBox() != null);

    await browser.close();
})();
