const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://google.com&renew=true");
    await cas.loginWith(page, "casuser", "Mellon");

    await page.goto("https://localhost:8443/cas");
    let tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length === 0);

    await page.goto("https://localhost:8443/cas/login?service=https://github.com&renew=true");
    await cas.loginWith(page, "casuser", "Mellon");

    await page.goto("https://localhost:8443/cas");
    tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);

    await browser.close();
})();
