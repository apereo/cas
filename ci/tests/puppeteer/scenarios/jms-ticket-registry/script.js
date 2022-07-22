const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    let response = await cas.goto(page, "https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)
    console.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");

    await cas.goto(page, "https://localhost:8443/cas/logout");
    let url = await page.url();
    console.log(`Page url: ${url}`);
    assert(url === "https://localhost:8443/cas/logout");

    await page.waitForTimeout(1000)
    await cas.assertCookie(page, false);

    await browser.close();
})();
