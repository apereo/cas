
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page, "https://google.com");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.log("Selecting mfa-gauth");
    await cas.assertInvisibility(page, "#mfa-gauth");
    await cas.assertInvisibility(page, "#mfa-yubikey");
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    
    await cas.closeBrowser(browser);
})();
