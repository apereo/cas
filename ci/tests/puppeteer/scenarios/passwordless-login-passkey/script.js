const cas = require("../../cas.js");

async function registerPasskey(page) {
    await cas.gotoLogin(page);
    await cas.attributeValue(page, "#username", "autocomplete", "username webauthn");
    await cas.type(page, "#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);

    await cas.assertVisibility(page, "#fmPassword");
    await cas.assertVisibility(page, "#fmPasskey");
    await cas.submitForm(page, "#fmPassword");
    await cas.sleep(1000);
    await cas.type(page, "#password", "Mellon");
    await cas.pressEnter(page);
    await cas.sleep(3000);

    await cas.assertTextContent(page, "#status", "Register Device");
    await page.click("#credentialNickname", {clickCount: 3});
    await cas.pressBackspace(page);
    await page.type("#credentialNickname", "mydevice");
    await cas.click(page, "#registerButton");
    await cas.sleep(5000);
    await cas.click(page, "#authnButton");
    await cas.sleep(5000);
    await cas.assertCookie(page);
    await cas.gotoLogout(page);
}

async function loginWithPasskey(page) {
    await cas.gotoLogin(page);
    await cas.type(page, "#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);

    await cas.assertVisibility(page, "#fmPasskey");
    await cas.assertInvisibility(page, "#passkeyError");
    await cas.click(page, "#btnPasskey");
    await cas.sleep(5000);

    await cas.assertCookie(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, casuser, have successfully logged in");
    await cas.click(page, "#auth-tab");
    await cas.sleep(1000);
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[WebAuthnAuthenticationHandler]");
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[WebAuthnCredential]");
    await cas.gotoLogout(page);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const virtualAuthenticator = await cas.createWebAuthnVirtualAuthenticator(page, "ctap2", true);

    await registerPasskey(page);
    await loginWithPasskey(page);

    await cas.removeWebAuthnVirtualAuthenticator(virtualAuthenticator);
    await cas.closeBrowser(browser);
})();
