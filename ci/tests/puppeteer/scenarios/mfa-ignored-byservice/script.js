const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?service=https://github.com");
    await cas.loginWith(page, "casuser", "Mellon");

    await page.waitForTimeout(1000)

    console.log(page.url());
    let result = new URL(page.url());
    console.log(result.searchParams.get("ticket"));
    assert(result.searchParams.has("ticket"));

    await page.goto("https://localhost:8443/cas");
    const tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);

    await browser.close();
})();
