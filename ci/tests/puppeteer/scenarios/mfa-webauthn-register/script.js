
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-webauthn");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertTextContent(page, "#status", "Register Device");
    await cas.assertVisibility(page, "#messages");
    await cas.assertInvisibility(page, "#device-info");
    await cas.assertInvisibility(page, "#device-icon");
    await cas.assertInvisibility(page, "#device-name");
    await cas.assertVisibility(page, "#credentialNickname");
    await cas.assertVisibility(page, "#registerButton");
    await cas.assertVisibility(page, "#residentKeysPanel");
    await cas.assertVisibility(page, "#registerDiscoverableCredentialButton");

    await browser.close();
})();
