const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "unknown+casuser", "Mellon");
    let p = await cas.innerText(page, '#loginErrorsPanel p');
    assert(p.startsWith("You are not authorized to impersonate"));

    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "user3+casuser", "Mellon");
    const header = await cas.innerText(page, '#content div h2');
    assert(header === "Log In Successful")
    p = await cas.innerText(page, '#content div p');
    assert(p.startsWith("You, user3, have successfully logged into the Central Authentication Service"))

    await browser.close();
})();
