
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#content h2", "This account has been locked.");
    await cas.assertCookie(page, false);
    await cas.gotoLogout(page);
    await cas.closeBrowser(browser);
})();

