
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);

    await cas.sleep(3000);
    await cas.assertInnerTextStartsWith(page, "#loginErrorsPanel p", "Authentication attempt for your account is denied");

    await cas.closeBrowser(browser);
})();
