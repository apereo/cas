const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    // await page.waitForTimeout(1000)

    let source = await page.$('#authnSourceSection');
    assert(await source.boundingBox() != null);

    let json = await page.$eval('#JSON-authnSource', el => el.innerText)
    console.log(json.trim())
    assert(json.trim() === "JSON")

    let example = await page.$eval('#EXAMPLE-authnSource', el => el.innerText)
    console.log(example.trim())
    assert(example.trim() === "EXAMPLE")

    await browser.close();
})();
