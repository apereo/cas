const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");
    // await page.waitForTimeout(60000)

    let hcaptchaSection = await page.$('#hcaptchaSection');
    assert(await hcaptchaSection.boundingBox() != null);

    await browser.close();
})();
