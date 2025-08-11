
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions({ options: [ "--accept-lang=de", "--lang=de"] }));
    const page = await cas.newPage(browser);

    const service = "https://localhost:9859/anything/1";

    await cas.log("Establish SSO session");
    await cas.gotoLogin(page);
    await cas.updateDuoSecurityUserStatus("duocode");
    await cas.sleep(2000);
    await cas.loginWith(page, "duocode", "Mellon");
    await cas.sleep(2000);
    await cas.assertCookie(page);

    await cas.log("Force SSO session to step up with Duo MFA");
    await cas.gotoLoginWithAuthnMethod(page, service, "mfa-duo");
    await cas.sleep(4000);

    await cas.log("Abandon MFA and go back to CAS to check for SSO");
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);

    await cas.log("Repeat: force SSO session to step up with Duo MFA");
    await cas.gotoLoginWithAuthnMethod(page, service, "mfa-duo");
    await cas.sleep(6000);
    await cas.loginDuoSecurityBypassCode(page, "duocode");
    await cas.sleep(6000);
    await cas.screenshot(page);
    await cas.assertTicketParameter(page);
    await cas.closeBrowser(browser);

})();
