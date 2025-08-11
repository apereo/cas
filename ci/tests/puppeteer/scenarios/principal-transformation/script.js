
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "   CasUser  ", "p@ssw0rd");
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");

    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "employeeNumber");
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "123456");

    await cas.closeBrowser(browser);
})();
