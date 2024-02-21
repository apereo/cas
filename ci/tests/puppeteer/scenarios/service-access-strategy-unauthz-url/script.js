const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://apereo.github.io");
    await cas.loginWith(page);
    await cas.screenshot(page);
    await cas.logPage(page);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    const url = await page.url();
    assert(url === "https://localhost:9859/anything/info");
    await browser.close();
})();
