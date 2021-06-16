const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await cas.loginWith(page, "casuser", "Mellon");

    await cas.assertTicketGrantingCookie(page);
    
    const title = await page.title();
    console.log(title);
    assert(title === "CAS - Central Authentication Service")

    const header = await cas.innerText(page, '#content div h2');
    assert(header === "Log In Successful")

    await browser.close();
})();
