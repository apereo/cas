const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());

    const context1 = await browser.createBrowserContext();
    const page1 = await cas.newPage(context1);
    await cas.gotoLogin(page1);
    await cas.sleep(2000);
    await cas.assertVisibility(page1, "li #CasClient");

    const context2 = await browser.createBrowserContext();
    const page2 = await cas.newPage(context2);
    await cas.gotoLogin(page2, "https://localhost:9859/anything/cas");
    await cas.sleep(2000);
    await cas.assertVisibility(page2, "li #CasClient");

    await cas.click(page1, "li #CasClient");
    await cas.waitForNavigation(page1);

    await cas.click(page2, "li #CasClient");
    await cas.waitForNavigation(page2);

    await cas.loginWith(page1, "casuser", "Mellon");
    await cas.sleep(6000);
    await cas.logPage(page1);
    await cas.assertMissingParameter(page1, "service");

    await context1.close();
    await context2.close();
    await cas.closeBrowser(browser);
})();
