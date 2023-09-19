const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8443/cas/login?service=https://apereo.github.io");
    await cas.log("Checking for page URL redirecting, based on service policy...");
    let url = await page.url();
    await cas.log(url);
    await page.waitForTimeout(1000);
    assert(url.startsWith("https://localhost:8444/cas/login"));

    await cas.goto(page, "https://localhost:8443/cas/login?service=https://github.com/apereo/cas");
    await cas.log("Checking for page URL...");
    url = await page.url();
    await cas.log(url);
    assert(url.startsWith("https://localhost:8444/cas/login"));
    await browser.close();
})();
