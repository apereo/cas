
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#content h1", "Authentication Succeeded with Warnings");
    await cas.assertInnerTextContains(page, "#content ul li span", "Password policy rules are here");
    await cas.click(page, "#continue");
    await cas.sleep(2000);
    await cas.assertCookie(page);
    await cas.gotoLogout(page);
    await cas.closeBrowser(browser);
})();

