const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());

    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await page.waitForTimeout(1000);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");

    await cas.goto(page, "https://localhost:8443/cas/account");
    await page.waitForTimeout(1000);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000);
    let url = await page.url();
    await cas.log(`Page url: ${url}`);
    assert(url === "https://localhost:8443/cas/account");
    await cas.assertTicketParameter(page, false);

    await browser.close();
})();
