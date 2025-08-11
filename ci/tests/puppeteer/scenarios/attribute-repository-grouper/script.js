
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.authenticate({"username":"GrouperSystem", "password": "@4HHXr6SS42@IHz2"});
    await cas.goto(page, "https://localhost:7443/grouper");
    await cas.sleep(10000);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "GrouperSystem", "Mellon");
    await cas.sleep(1000);
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "grouperGroups");
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "etc:grouperUi:grouperUiUserData");
    await cas.assertCookie(page);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.assertTicketParameter(page);
    await cas.closeBrowser(browser);
})();
