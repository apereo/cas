
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/adaptive";
    await cas.gotoLogin(page, service);
    await cas.sleep(2000);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertInnerTextContains(page, "#loginErrorsPanel p", "authentication attempt is determined to be risky");
    const body = await cas.extractFromEmail(browser);
    assert(body.includes("casuser with score 1.00"));
    await cas.closeBrowser(browser);
})();
