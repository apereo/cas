
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Trying first app with a fancy theme");
    await cas.gotoLogin(page, "https://apereo.github.io&authn_method=mfa-duo");
    await cas.sleep(1000);
    await cas.click(page, "#publicWorkstation");
    await cas.updateDuoSecurityUserStatus("duocode");
    await cas.loginWith(page, "duocode", "Mellon");
    await cas.sleep(4000);
    await cas.loginDuoSecurityBypassCode(page,"duocode");
    await cas.sleep(4000);
    await cas.screenshot(page);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");
    await browser.close();
})();
