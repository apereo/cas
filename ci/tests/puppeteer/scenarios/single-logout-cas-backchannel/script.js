const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8444");
    await page.waitForTimeout(1000);
    await cas.goto(page, "https://localhost:8444/protected");
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    await cas.logPage(page);
    let url = await page.url();
    assert(url.startsWith("https://localhost:8444/protected"));
    await cas.assertInnerTextContains(page, "div.starter-template h2 span", "casuser");
    await cas.gotoLogout(page);
    await page.waitForTimeout(2000);
    await cas.goto(page, "https://localhost:8444/protected");
    await page.waitForTimeout(2000);
    await cas.log(await page.url());
    assert(url.startsWith("https://localhost:8443/cas/login?service="));
    await cas.shutdownCas("https://localhost:8444");
    await browser.close();
})();
