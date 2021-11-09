const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await page.goto("https://localhost:8443/cas/login?service=https://github.com");
    await page.waitForTimeout(1000);

    let loginProviders = await page.$('#loginProviders');
    assert(loginProviders == null);

    await page.goto("https://localhost:8443/cas/login?service=https://google.com");
    await page.waitForTimeout(1000);

    await cas.assertVisibility(page, 'li #CASServerOne')
    await cas.assertVisibility(page, 'li #CASServerTwo');
    assert(await page.$('#username') == null);
    assert(await page.$('#password') == null);

    await browser.close();
})();


