
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-yubikey");
    await cas.loginWith(page);

    await cas.sleep(1000);
    await cas.assertTextContent(page, "#login h3", "Use your registered YubiKey device(s) to authenticate.");

    await cas.assertVisibility(page, "button[name=register]");
    await cas.type(page, "#token", "12345678901234567890123456789012345");
    await cas.submitForm(page, "#yubiKeyForm");

    const endpoints = ["yubikeyAccountRepository/casuser"];
    const baseUrl = "https://localhost:8443/cas/actuator/";
    for (let i = 0; i < endpoints.length; i++) {
        const url = baseUrl + endpoints[i];
        const response = await cas.goto(page, url);
        await cas.logg(`Status: ${response.status()} ${response.statusText()}`);
        assert(response.ok());
    }

    await cas.closeBrowser(browser);
})();
