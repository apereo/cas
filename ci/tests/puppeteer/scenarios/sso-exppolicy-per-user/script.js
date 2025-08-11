
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.log("Waiting for hard timeout to complete...");
    await cas.sleep(3000);
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);
    await cas.closeBrowser(browser);
})();
