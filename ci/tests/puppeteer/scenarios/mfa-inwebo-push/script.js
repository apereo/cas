const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-inwebo");
    await cas.loginWith(page, "testcaspush", "password");
    await cas.sleep(2000);
    const pendingForm = await page.$("#pendingCheckResultForm");
    const retryForm = await page.$("#retryForm");
    assert(retryForm !== null || pendingForm !== null);
    await cas.closeBrowser(browser);
})();
