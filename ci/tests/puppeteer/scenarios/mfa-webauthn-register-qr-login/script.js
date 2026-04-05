const cas = require("../../cas.js");

async function removeWebAuthnDevices() {
    await cas.doDelete("https://localhost:8443/cas/actuator/webAuthnDevices/casuser");
}

async function verifyFlowUsingQRCodeButtonSameWindow() {
    await removeWebAuthnDevices();

    const browser = await cas.newBrowser(cas.browserOptions());
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);

    const virtualAuthenticator = await cas.createWebAuthnVirtualAuthenticator(page);

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

    await page.click("#credentialNickname", {clickCount: 3});
    await cas.pressBackspace(page);
    await page.type("#credentialNickname", "mydevice");

    await cas.click(page, "#registerButton");
    await cas.sleep(5000);
    await cas.click(page, "#qrCodeButton");
    await cas.sleep(1000);
    await cas.click(page, "#authnButton");
    await cas.sleep(5000);

    await page.bringToFront();
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.click(page, "#auth-tab");
    await cas.sleep(1000);
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[WebAuthnAuthenticationHandler]");
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[mfa-webauthn]");
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[WebAuthnCredential]");

    await cas.sleep(2000);
    await cas.removeWebAuthnVirtualAuthenticator(virtualAuthenticator);
    await context.close();
    await cas.closeBrowser(browser);
}

(async () => {
    await verifyFlowUsingQRCodeButtonSameWindow();
})();
