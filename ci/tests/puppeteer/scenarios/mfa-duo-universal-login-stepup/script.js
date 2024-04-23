
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const stepUpUrl = "https://localhost:8443/cas/login?service=https://localhost:9859/anything/1&authn_method=mfa-duo";

    await cas.log("Establish SSO session");
    await cas.gotoLogin(page);
    await cas.updateDuoSecurityUserStatus("duocode");
    await cas.loginWith(page, "duocode", "Mellon");
    await cas.assertCookie(page);

    await cas.log("Force SSO session to step up with Duo MFA");
    await cas.goto(page, stepUpUrl);
    await cas.sleep(4000);

    await cas.log("Abandon MFA and go back to CAS to check for SSO");
    await cas.gotoLogin(page);
    await cas.assertCookie(page);

    await cas.log("Repeat: force SSO session to step up with Duo MFA");
    await cas.goto(page, stepUpUrl);
    await cas.sleep(6000);
    await cas.loginDuoSecurityBypassCode(page, "duocode");
    await cas.sleep(6000);
    await cas.screenshot(page);
    await cas.assertTicketParameter(page);
    await browser.close();

})();
