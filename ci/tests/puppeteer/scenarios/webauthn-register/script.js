const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?authn_method=mfa-webauthn");
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)
    await cas.assertTextContent(page, "#status", "Register Device");
    await cas.assertVisibility(page, '#messages')
    await cas.assertInvisibility(page, '#device-info')
    await cas.assertInvisibility(page, '#device-icon')
    await cas.assertInvisibility(page, '#device-name')
    await cas.assertVisibility(page, '#credentialNickname')
    await cas.assertVisibility(page, '#registerButton')
    await cas.assertVisibility(page, '#residentKeysPanel')
    await cas.assertVisibility(page, '#registerDiscoverableCredentialButton')

    await browser.close();
})();
