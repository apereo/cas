const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    console.log("Attempting invalid password reset without SSO")
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?pswdrst=bad_token_with_no_sso");
    let header = await cas.innerText(page, '#content h2');

    assert(header === "Password Reset Failed")

    console.log("Attempting invalid password reset with SSO")
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    await page.goto("https://localhost:8443/cas/login?pswdrst=bad_token_with_sso");
    header = await cas.innerText(page, '#content h2');

    assert(header === "Password Reset Failed")

    await browser.close();
})();
