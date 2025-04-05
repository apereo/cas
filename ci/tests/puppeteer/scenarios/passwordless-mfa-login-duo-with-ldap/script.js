
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://apereo.github.io");

    await cas.type(page, "#username", "duobypass");
    await cas.pressEnter(page);
    await cas.screenshot(page);
    await cas.log("Waiting for Duo MFA to complete...");
    await cas.sleep(8000);
    await cas.screenshot(page);
    await cas.log("Checking for service ticket...");
    await cas.assertTicketParameter(page);
    await cas.log("Checking for SSO Session cookie...");
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);

    await browser.close();
})();
