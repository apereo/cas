const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    await cas.doDelete("https://localhost:8443/cas/actuator/webAuthnDevices/casuser");
    
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const virtualAuthenticator = await cas.createWebAuthnVirtualAuthenticator(page, "ctap2");
    const storedCredentials = async () => (await virtualAuthenticator.client.send("WebAuthn.getCredentials", {
        authenticatorId: virtualAuthenticator.authenticator.authenticatorId
    })).credentials;

    await cas.gotoLogin(page);

    await cas.loginWith(page);
    await cas.sleep(1000);

    const endpoints = JSON.parse(await cas.doRequest("https://localhost:8443/cas/.well-known/passkey-endpoints",
        "GET", {"Accept": "application/json"}, 200));
    assert(endpoints.enroll === "https://localhost:8443/cas/account");
    assert(endpoints.manage === endpoints.enroll);

    await cas.goto(page, endpoints.manage);
    await cas.sleep(1000);
    await cas.click(page, "#linkMfaRegisteredAccounts");
    await cas.assertVisibility(page, "#mfaDevicesEmpty");
    await cas.sleep(1000);

    await cas.click(page, "button#register");
    await cas.sleep(2000);
    await cas.assertInnerTextContains(page, "#registrationOptions", "Google Authenticator");
    await cas.click(page, "#webauthnRegistrationLink");
    await cas.sleep(2000);

    await cas.clearValue(page, "#credentialNickname");
    const deviceName = await cas.randomWord(12, false);
    await cas.log(`Device Name: ${deviceName}`);
    await page.type("#credentialNickname", deviceName);
    await cas.sleep(1000);
    await cas.click(page, "#registerButton");

    await cas.sleep(4000);

    await cas.assertInnerText(page, "#mfaDevicesList [data-field=source]", "Web Authn");
    await cas.assertInnerText(page, "#mfaDevicesList [data-field=name]", deviceName);
    await cas.sleep(2000);
    assert((await storedCredentials()).length === 1);

    await cas.click(page, "#linkMfaRegisteredAccounts");
    await cas.sleep(1000);
    await cas.click(page, "#mfaDevicesList button[name=deleteMfaDevice]");
    await cas.sleep(3000);
    await cas.click(page, "#linkMfaRegisteredAccounts");
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#mfaDevicesEmpty");
    assert((await storedCredentials()).length === 0, "Deleting the last passkey should tell the browser to stop offering it");

    await cas.removeWebAuthnVirtualAuthenticator(virtualAuthenticator);
    
    await cas.closeBrowser(browser);
})();
