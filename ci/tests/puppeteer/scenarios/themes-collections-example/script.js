const puppeteer = require('puppeteer');
const assert = require('assert');

const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(2000)

    await cas.assertVisibility(page, '#twitter-link')
    await cas.assertVisibility(page, '#youtube-link')

    const imgs = await page.$$eval('#cas-logo',
        imgs => imgs.map(img => img.getAttribute('src')));
    let logo = imgs.pop();
    console.log(logo)
    assert(logo === "/cas/themes/example/images/logo.png")

    await page.goto("https://localhost:8443/cas/logout");
    await page.waitForTimeout(2000)

    await browser.close();
})();
