
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
    cas.logg("authenticator: ", authenticator);

    await cas.gotoLogin(page);

    await cas.loginWith(page);
    await cas.sleep(1000);

    await cas.goto(page, "https://localhost:8443/cas/account");
    await cas.sleep(1000);

    await cas.click(page, "#linkMfaRegisteredAccounts");
    await cas.assertInnerTextContains(page, "#mfaDevicesTable", "No data available");
    await cas.sleep(1000);

    await cas.click(page, "button#register");
    await cas.sleep(2000);
    await cas.assertInnerTextContains(page, "#toolbar", "Google Authenticator");
    await cas.click(page, "#webauthnRegistrationLink");
    await cas.sleep(2000);

    await page.click("#credentialNickname", { clickCount: 3 });
    await page.keyboard.press("Backspace");
    await page.type("#credentialNickname", "mydevice");

    await page.click("#registerButton");

    await cas.sleep(4000);

    await cas.assertInnerText(page, "#mfaDevicesTable tbody tr td:first-child", "Web Authn");
    await cas.assertInnerText(page, "#mfaDevicesTable tbody tr td:nth-child(3)", "mydevice");
    await cas.sleep(2000);

    await client.send("WebAuthn.removeVirtualAuthenticator", {
        authenticatorId: authenticator.authenticatorId
    });
    await browser.close();
})();
