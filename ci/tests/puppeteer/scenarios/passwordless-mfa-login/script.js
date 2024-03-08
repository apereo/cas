
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    const pswd = await page.$("#password");
    assert(pswd === null);
    await cas.type(page, "#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertInnerText(page, "#login h3", "Use your registered YubiKey device(s) to authenticate.");
    await browser.close();
})();
