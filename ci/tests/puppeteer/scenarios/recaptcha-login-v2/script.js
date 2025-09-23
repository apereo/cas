
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    // await cas.sleep(10000)
    await cas.assertVisibility(page, "#recaptchaV2Section");
    await cas.assertVisibility(page, "#g-recaptcha");

    await cas.closeBrowser(browser);
})();
