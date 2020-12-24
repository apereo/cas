const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");
    // await page.waitForTimeout(10000)

    let recaptchaV2Section = await page.$('#recaptchaV2Section');
    assert(await recaptchaV2Section.boundingBox() != null);

    let grecaptcha = await page.$('#g-recaptcha');
    assert(await grecaptcha.boundingBox() != null);

    await browser.close();
})();
