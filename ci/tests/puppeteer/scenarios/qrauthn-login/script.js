const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    let element = await page.$('#qrlogin .card-title span');
    const header = await page.evaluate(element => element.textContent, element);
    console.log(header)
    assert(header === "Login with QR Code");

    var image = await page.$('#qrlogin .card-text img');
    assert(await image.boundingBox() != null);

    var channel = await page.$('#qrchannel');
    assert(await channel.boundingBox() != null);

    await browser.close();
})();
