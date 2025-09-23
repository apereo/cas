
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    // await cas.sleep(60000)

    await cas.assertVisibility(page, "#hcaptchaSection");

    await cas.closeBrowser(browser);
})();
