const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "user3+casuser", "Mellon");

    
    // await page.waitForTimeout(5000)

    const header = await cas.innerText(page, '#content div h2');
    assert(header === "Log In Successful")

    const p = await cas.innerText(page, '#content div p');
    assert(p.startsWith("You, user3, have successfully logged into the Central Authentication Service"))

    await browser.close();
})();
