const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    
    const header = await cas.innerText(page, '#content h2');

    assert(header === "MFA Provider Unavailable")

    const sub = await cas.innerText(page, '#content p');
    assert(sub.startsWith("CAS was unable to reach your configured MFA provider at this time."))


    await browser.close();
})();
