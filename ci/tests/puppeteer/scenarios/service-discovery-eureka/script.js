const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.goto(page, "https://localhost:8761");
    let app = await page.evaluate((element) => {
        let elements = document.querySelectorAll(element);
        let btn = elements[elements.length - 1];
        return btn.querySelectorAll(":scope > tbody tr td")[0].innerText;
    }, "table#instances");
    assert(app === "CAS");
    await browser.close();
})();
