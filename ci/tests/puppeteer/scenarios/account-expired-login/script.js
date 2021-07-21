const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await cas.loginWith(page, "expired", "expired");
    
    const header = await cas.innerText(page, '#pwdmain h3');

    assert(header === "Hello, expired. Your password has expired.")

    let pwddesc = await cas.innerText(page, '#pwddesc');
    assert(pwddesc === "Please change your password.")
    
    await browser.close();
})();
