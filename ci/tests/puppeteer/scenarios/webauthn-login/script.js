const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?authn_method=mfa-webauthn");
    await cas.loginWith(page, "casuser", "Mellon");

    await page.waitForTimeout(1000)
    await cas.assertTextContent(page, "#status", "Login with FIDO2-enabled Device");
    
    let errorPanel = await page.$('#errorPanel');
    assert(await errorPanel == null);

    await cas.assertVisibility(page, '#messages')
    await cas.assertInvisibility(page, '#deviceTable')
    await cas.assertVisibility(page, '#authnButton')

    const endpoints = ["health", "webAuthnDevices/casuser"];
    const baseUrl = "https://localhost:8443/cas/actuator/"
    for (let i = 0; i < endpoints.length; i++) {
        let url = baseUrl + endpoints[i];
        const response = await page.goto(url);
        console.log(`${response.status()} ${response.statusText()}`)
        assert(response.ok())
    }

    await browser.close();
})();
