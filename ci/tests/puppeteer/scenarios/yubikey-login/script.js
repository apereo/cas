const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?authn_method=mfa-yubikey");
    await cas.loginWith(page, "casuser", "Mellon");

    await page.waitForTimeout(1000)
    await cas.assertTextContent(page, "#login h3", "Use your registered YubiKey device(s) to authenticate.");

    await cas.assertVisibility(page, 'button[name=register]')
    await cas.type(page, "#token", "12345678901234567890123456789012345");
    await cas.submitForm(page, "#yubiKeyForm")

    const endpoints = ["yubikeyAccountRepository/casuser"];
    const baseUrl = "https://localhost:8443/cas/actuator/"
    for (let i = 0; i < endpoints.length; i++) {
        let url = baseUrl + endpoints[i];
        const response = await page.goto(url);
        console.log(`${response.status()} ${response.statusText()}`)
        assert(response.ok())
    }

    await browser.close();
})();
