const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();

    console.log("Generating service ticket without SSO")
    await page.goto("https://localhost:8443/cas/login?service=https://google.com");
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    console.log(page.url());
    var result = new URL(page.url());
    console.log(result.searchParams.get("ticket"));
    assert(result.searchParams.has("ticket"));

    console.log("Generating service ticket with SSO")
    await page.goto("https://localhost:8443/cas/login?service=https://google.com");
    console.log(page.url());
    result = new URL(page.url());
    console.log(result.searchParams.get("ticket"));
    assert(result.searchParams.has("ticket"));

    await browser.close();
})();
