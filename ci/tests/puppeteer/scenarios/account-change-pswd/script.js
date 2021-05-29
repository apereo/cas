const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    await cas.loginWith(page, "mustchangepswd", "mustchangepswd");

    const header = await cas.innerText(page, '#pwdmain h3');
    assert(header === "You must change your password.")

    let pwddesc = await cas.innerText(page, '#pwddesc');
    assert(pwddesc === "Please change your password.")
    
    await browser.close();
})();
