
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#clientIp");
    await cas.assertVisibility(page, "#userAgent");
    await cas.closeBrowser(browser);
})();
