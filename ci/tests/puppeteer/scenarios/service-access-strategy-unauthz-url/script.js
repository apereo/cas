const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const service = "https://apereo.github.io";
    
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    await cas.screenshot(page);
    let url = await page.url();
    await cas.log(`Page URL: ${url}`);
    assert(url === "https://localhost:9859/anything/info");
    await browser.close();
})();
