
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertInnerText(page, "#content h2", "MFA Provider Unavailable");
    await cas.assertInnerTextStartsWith(page, "#content p", "CAS was unable to reach your configured MFA provider at this time.");
    await cas.closeBrowser(browser);
})();
