const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    // await page.waitForTimeout(20000)

    let element = await page.$('#twitter-link');
    assert(await element.boundingBox() != null);

    element = await page.$('#youtube-link');
    assert(await element.boundingBox() != null);

    const imgs = await page.$$eval('#cas-logo',
        imgs => imgs.map(img => img.getAttribute('src')));
    let logo = imgs.pop();
    console.log(logo)
    assert(logo === "/cas/themes/example/images/logo.png")

    await browser.close();
})();
