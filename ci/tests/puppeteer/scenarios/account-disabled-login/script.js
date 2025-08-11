
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.loginWith(page, "disabled", "disabled");
    await cas.assertInnerText(page, "#content h2", "This account has been disabled.");
    await cas.closeBrowser(browser);
})();
