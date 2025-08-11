
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertInnerText(page, "#content h1", "Authentication Succeeded with Warnings");
    await cas.assertInnerTextContains(page, "#content ul li span", "Your password is commonly used");
    await cas.closeBrowser(browser);
})();
