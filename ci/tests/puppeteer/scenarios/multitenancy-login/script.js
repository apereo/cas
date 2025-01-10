const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginForTenant(page, "shire");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.sleep(1000);
    await cas.assertCookie(page);
    await browser.close();
})();
