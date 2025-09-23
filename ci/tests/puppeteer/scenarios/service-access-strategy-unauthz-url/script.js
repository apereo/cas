
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.screenshot(page);
    await cas.logPage(page);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    await cas.sleep(4000);
    await cas.assertPageUrl(page, "https://localhost:9859/anything/info");
    await cas.closeBrowser(browser);
})();
