
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.assertCookie(page, true, "CASWebflowCookie");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.closeBrowser(browser);
})();
