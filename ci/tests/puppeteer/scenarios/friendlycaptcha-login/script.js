
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.assertVisibility(page, "#friendlyCaptchaSection");

    await cas.closeBrowser(browser);
})();
