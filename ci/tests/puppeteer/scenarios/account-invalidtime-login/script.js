
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.loginWith(page, "invalidtime", "invalidtime");
    await cas.assertInnerText(page, "#content h2", "Your account is forbidden to log in at this time.");
    await cas.closeBrowser(browser);
})();
