const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?service=https://example.org");

    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    await page.waitForTimeout(9000);
    const mfaGauth = await page.$('div#mfa-gauth');
    const form = await mfaGauth.$('form');
    await form.evaluate(form => form.submit());
    await page.waitForTimeout(5000);

    let element = await page.$('#login p');
    await page.type('#token', "123456");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    await page.waitForTimeout(5000);
    const html = await page.content();
    console.log(html);
    await browser.close();
})();

