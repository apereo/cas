const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);
    await cas.closeBrowser(browser);

    const browser2 = await cas.newBrowser(cas.browserOptions());
    const page2 = await cas.newPage(browser2);
    await cas.gotoLogin(page2);
    await cas.sleep(1000);
    await cas.assertCookie(page2);
    await cas.closeBrowser(browser2);
})();
