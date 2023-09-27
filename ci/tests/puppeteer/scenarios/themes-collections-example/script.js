const puppeteer = require('puppeteer');
const assert = require('assert');

const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await page.waitForTimeout(2000);

    await cas.assertVisibility(page, '#twitter-link');
    await cas.assertVisibility(page, '#youtube-link');
    await cas.assertInvisibility(page, '#pmlinks');

    const imgs = await page.$$eval('#cas-logo',
        imgs => imgs.map(img => img.getAttribute('src')));
    let logo = imgs.pop();
    await cas.log(logo);
    assert(logo === "/cas/themes/example/images/logo.png");

    await cas.gotoLogout(page);
    await page.waitForTimeout(2000);

    await browser.close();
})();
