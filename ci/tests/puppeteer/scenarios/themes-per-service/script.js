const puppeteer = require('puppeteer');
const assert = require('assert');

const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://github.com/apereo/cas");
    await page.waitForTimeout(1000);

    await cas.assertVisibility(page, '#twitter-link');
    await cas.assertVisibility(page, '#youtube-link');

    const imgs = await page.$$eval('#cas-logo',
        imgs => imgs.map(img => img.getAttribute('src')));
    let logo = imgs.pop();
    await cas.log(logo);
    assert(logo === "/cas/themes/example/images/logo.png");

    await cas.log("Logging out...");
    await cas.goto(page, "https://localhost:8443/cas/logout?service=https://github.com/apereo/cas");
    await page.waitForTimeout(1000);

    await cas.assertVisibility(page, '#twitter-link');
    await cas.assertVisibility(page, '#youtube-link');
    
    await cas.assertVisibility(page, '#logoutButton');
    await cas.submitForm(page, "#fm1");

    await page.waitForTimeout(1000);
    const url = await page.url();
    await cas.logPage(page);
    assert(url.toString().startsWith("https://localhost:8443/cas/logout"));
    await cas.assertCookie(page, false);

    await cas.assertVisibility(page, '#twitter-link');
    await cas.assertVisibility(page, '#youtube-link');
    
    await browser.close();
})();
