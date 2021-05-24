const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");
    // await page.waitForTimeout(60000)

    let hcaptchaSection = await page.$('#hcaptchaSection');
    assert(await hcaptchaSection.boundingBox() != null);

    await browser.close();
})();
