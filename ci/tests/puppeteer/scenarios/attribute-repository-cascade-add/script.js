
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "[CAS]");
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "[User]");
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "[123456]");
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "[Yellow]");
    await cas.assertCookie(page);
    await browser.close();
})();
