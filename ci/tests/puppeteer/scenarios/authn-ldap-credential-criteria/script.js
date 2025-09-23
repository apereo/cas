const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "casuser@example.org");
    await cas.sleep(1000);
    await cas.assertCookie(page);
    await cas.gotoLogout(page);

    await cas.gotoLogin(page);
    await cas.loginWith(page, "cassystem@system.org");
    await cas.sleep(1000);
    await cas.assertCookie(page);
    
    await cas.closeBrowser(browser);
})();
