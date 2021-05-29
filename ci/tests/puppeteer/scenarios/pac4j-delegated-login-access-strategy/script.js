const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    
    await page.goto("https://localhost:8443/cas/login?service=https://github.com");
    await page.waitForTimeout(1000);

    let loginProviders = await page.$('#loginProviders');
    assert(loginProviders == null);

    await page.goto("https://localhost:8443/cas/login?service=https://google.com");
    await page.waitForTimeout(1000);

    await cas.assertVisibility(page, 'li #CASServerOne')

    await cas.assertInvisibility(page, 'li #CASServerTwo');

    await browser.close();
})();


