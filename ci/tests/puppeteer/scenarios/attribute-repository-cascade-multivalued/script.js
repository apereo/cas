
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "[CAS, sys_CAS]");
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "[User, sys_User]");
    await cas.assertCookie(page);
    await browser.close();
})();
