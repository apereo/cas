
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://example.com");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#mfa-gauth");
    await cas.assertVisibility(page, "#mfa-yubikey");
    await cas.click(page, "#btn-mfa-gauth");
    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.log("Fetching Scratch codes from /cas/actuator...");
    const scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    await cas.log(`Using scratch code ${scratch} to login...`);
    await cas.type(page,"#token", scratch);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.screenshot(page);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.assertCookie(page, true, "MFATRUSTED");

    const baseUrl = "https://localhost:8443/cas/actuator/multifactorTrustedDevices";
    const response = await cas.doRequest(baseUrl);
    const record = JSON.parse(response)[0];
    console.dir(record, {depth: null, colors: true});
    assert(record.id !== undefined);
    assert(record.name !== undefined);
    await cas.gotoLogout(page);

    await cas.gotoLogin(page, "https://example.com");
    await cas.loginWith(page);
    await cas.sleep(3000);
    await cas.screenshot(page);
    await cas.assertTicketParameter(page);

    await cas.gotoLogout(page);
    await cas.log(`Removing trusted device ${record.name}`);
    await cas.doDelete(`${baseUrl}/${record.recordKey}`);
    await cas.gotoLogin(page, "https://example.com");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.screenshot(page);
    await cas.assertVisibility(page, "#mfa-gauth");
    await cas.assertVisibility(page, "#mfa-yubikey");
    
    await cas.closeBrowser(browser);
})();
