
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const virtualAuthenticator = await cas.createWebAuthnVirtualAuthenticator(page);

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
    await cas.pressBackspace(page);
    await page.type("#credentialNickname", "mydevice");

    await cas.click(page, "#registerButton");

    await cas.sleep(4000);

    await cas.assertInnerText(page, "#mfaDevicesTable tbody tr td:first-child", "Web Authn");
    await cas.assertInnerText(page, "#mfaDevicesTable tbody tr td:nth-child(3)", "mydevice");
    await cas.sleep(2000);

    await cas.removeWebAuthnVirtualAuthenticator(virtualAuthenticator);
    
    await cas.closeBrowser(browser);
})();
