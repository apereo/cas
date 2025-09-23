
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "[John]");
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "[Smith]");
    await cas.assertCookie(page);
    await cas.closeBrowser(browser);
})();
