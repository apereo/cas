const puppeteer = require('puppeteer');

const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await page.evaluate(() => typeof onRecaptchaV2Submit === 'function')

    await cas.assertVisibility(page, 'button.g-recaptcha')

    await browser.close();
})();
