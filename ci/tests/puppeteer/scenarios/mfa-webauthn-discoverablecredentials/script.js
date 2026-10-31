const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const virtualAuthenticator = await cas.createWebAuthnVirtualAuthenticator(page, "ctap2", true);

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

    const registrationRequests = [];
    page.on("response", async (response) => {
        if (response.request().method() === "POST" && response.url().includes("/webauthn/register")) {
            registrationRequests.push(await response.text().catch(() => ""));
        }
    });
    await cas.click(page, "#registerDiscoverableCredentialButton");

    await cas.sleep(5000);
    assert(registrationRequests.some((body) => body.includes("\"attestation\":\"direct\"")),
        "Registration must ask the authenticator for direct attestation");

    if (process.env.SCENARIO_VARIATION === "untrusted-attestation-refused") {
        await cas.logg("The attestation of the virtual authenticator cannot be trusted, so the registration is refused");
        const status = await cas.innerText(page, "#status");
        assert(/error|failed/i.test(status), `Registration must fail, but the status is ${status}`);
        await cas.assertCookie(page, false);
        await cas.removeWebAuthnVirtualAuthenticator(virtualAuthenticator);
        await cas.closeBrowser(browser);
        return;
    }

    await cas.click(page, "#authnButton");

    await cas.sleep(5000);

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.click(page, "#auth-tab");
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

    await cas.sleep(1000);

    await cas.click(page, "#authnButton");

    await cas.sleep(5000);

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.click(page, "#auth-tab");
    await cas.sleep(1000);
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[WebAuthnAuthenticationHandler]");
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[mfa-webauthn]");
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[WebAuthnCredential]");

    await cas.logg("User verification is required, so an authenticator that cannot verify the user is refused");
    await cas.gotoLogout(page);
    await virtualAuthenticator.client.send("WebAuthn.setUserVerified", {
        authenticatorId: virtualAuthenticator.authenticator.authenticatorId,
        isUserVerified: false
    });
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-webauthn");
    await cas.sleep(1000);
    await cas.click(page, "#authnButton");
    await cas.sleep(5000);
    await cas.assertCookie(page, false);

    await cas.sleep(2000);

    await cas.removeWebAuthnVirtualAuthenticator(virtualAuthenticator);
    await cas.closeBrowser(browser);
})();
