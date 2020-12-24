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

    let recaptchaV3Section = await page.$('#recaptchaV3Section');
    assert(await recaptchaV3Section.boundingBox() != null);

    let grecaptcha = await page.$('#g-recaptcha-token');
    assert(await grecaptcha != null);

    await browser.close();
})();
