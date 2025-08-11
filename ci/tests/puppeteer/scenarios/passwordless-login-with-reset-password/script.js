const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);

    await cas.assertElementDoesNotExist(page, "#password");

    await cas.type(page,"#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);

    await cas.sleep(2000);

    await cas.assertInvisibility(page, "#username");
    await cas.assertVisibility(page, "#password");
    await cas.assertVisibility(page, "#forgotPasswordLink");

    await cas.click(page, "#forgotPasswordLink");
    await cas.sleep(1000);

    await cas.type(page,"#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(2000);

    const pwdResetUrl = await cas.extractFromEmail(browser);

    await page.goto(pwdResetUrl);
    await cas.sleep(1000);

    await cas.assertInnerText(page, "#content h3", "Hello, casuser. You must change your password.");
    await cas.sleep(2000);

    await cas.closeBrowser(browser);
})();
