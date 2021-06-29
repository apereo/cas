const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?authn_method=mfa-u2f");
    await cas.loginWith(page, "casuser", "Mellon");

    await page.waitForTimeout(3000)
    
    let header = await cas.textContent(page, "#login h3");
    assert(header === "Authenticate Device")

    header = await cas.textContent(page, "#login p");
    assert(header === "Please touch the flashing U2F device now.")

    await browser.close();
})();
