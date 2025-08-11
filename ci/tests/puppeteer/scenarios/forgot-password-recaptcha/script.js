
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.sleep(2000);
    await cas.assertTextContent(page, "#forgotPasswordLink", "Reset your password");

    await cas.click(page, "#forgotPasswordLink");
    await cas.sleep(1000);

    await cas.assertTextContent(page, "#reset #fm1 h3", "Reset your password");
    await cas.assertVisibility(page, "#username");
    await cas.type(page,"#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.assertTextContent(page, "div .banner-danger p", "reCAPTCHA’s validation failed.");
    await cas.closeBrowser(browser);
})();
