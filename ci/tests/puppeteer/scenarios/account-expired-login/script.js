
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "expired", "expired");
    await cas.assertInnerText(page, "#pwdmain h3", "Hello, expired. Your password has expired.");
    await cas.assertInnerText(page, "#pwddesc", "Please change your password.");
    await browser.close();
})();
