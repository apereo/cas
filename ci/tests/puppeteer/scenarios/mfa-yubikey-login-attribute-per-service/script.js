const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://apereo.github.io");
    await cas.loginWith(page);

    await page.waitForTimeout(1000);
    await cas.assertTextContent(page, "#login h3", "Use your registered YubiKey device(s) to authenticate.");

    await cas.assertInvisibility(page, 'button[name=register]');
    await cas.type(page, "#token", "12345678901234567890123456789012345");
    await cas.submitForm(page, "#yubiKeyForm", response => response.status() === 200);
    await cas.assertTextContentStartsWith(page, "div .banner-danger span", "Credentials are rejected/invalid");

    const endpoints = ["yubikeyAccountRepository/casuser"];
    const baseUrl = "https://localhost:8443/cas/actuator/";
    for (let i = 0; i < endpoints.length; i++) {
        let url = baseUrl + endpoints[i];
        const response = await cas.goto(page, url);
        await cas.logg(`Status: ${response.status()} ${response.statusText()}`);
        assert(response.ok())
    }

    await browser.close();
})();
