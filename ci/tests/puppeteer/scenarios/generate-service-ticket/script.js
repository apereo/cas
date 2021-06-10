const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    console.log("Generating service ticket without SSO")
    await page.goto("https://localhost:8443/cas/login?service=https://google.com");
    await cas.loginWith(page, "casuser", "Mellon");

    console.log(page.url());
    let result = new URL(page.url());
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
