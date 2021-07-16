const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.setExtraHTTPHeaders({
        'Authorization': 'Negotiate unknown-token'
    })
    await page.goto("https://localhost:8443/cas/login");
    
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(3000)

    const header = await cas.innerText(page, '#content div h2');
    assert(header === "Log In Successful")

    await browser.close();
})();
