const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);

    await cas.assertElementDoesNotExist(page, "#password");

    await cas.type(page,"#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);

    await cas.sleep(1000);

    await cas.assertVisibility(page, "#token");
    await cas.sleep(1000);

    const code = await cas.extractFromEmail(browser);
    
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(1000);

    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "firstname");
    await cas.sleep(2000);

    await cas.closeBrowser(browser);
})();
