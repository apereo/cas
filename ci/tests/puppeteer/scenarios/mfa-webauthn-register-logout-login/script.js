
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const client = await page.target().createCDPSession();

    await client.send("WebAuthn.enable");

    const authenticator = await client.send("WebAuthn.addVirtualAuthenticator", {
        options: {
            protocol: "u2f",
            transport: "usb",
            hasResidentKey: false,
            hasUserVerification: true,
            isUserVerified: true
        }
    });
    console.log("authenticator: ", authenticator);

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

    await page.click("#credentialNickname", { clickCount: 3 });
    await page.keyboard.press("Backspace");
    await page.type("#credentialNickname", "mydevice");

    await page.click("#registerButton");

    await cas.sleep(5000);

    await page.click("#authnButton");

    await cas.sleep(5000);

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await page.click("#auth-tab");
    await cas.sleep(1000);
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[WebAuthnAuthenticationHandler]");
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[mfa-webauthn]");
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[WebAuthnCredential]");

    await cas.gotoLogout(page);

    await cas.sleep(1000);
    await cas.assertPageUrl(page, "https://localhost:8443/cas/logout");
    await cas.assertCookie(page, false);
    await cas.assertInnerText(page, "#main-content h2", "Logout successful");

    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-webauthn");

    await cas.loginWith(page);
    await cas.sleep(1000);

    await page.click("#authnButton");

    await cas.sleep(5000);

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await page.click("#auth-tab");
    await cas.sleep(1000);
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[WebAuthnAuthenticationHandler]");
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[mfa-webauthn]");
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[WebAuthnCredential]");

    await cas.sleep(2000);

    await client.send("WebAuthn.removeVirtualAuthenticator", {
        authenticatorId: authenticator.authenticatorId
    });
    await browser.close();
})();
