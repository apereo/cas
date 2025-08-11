
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page);

    await cas.sleep(1000);
    await cas.assertTextContent(page, "#login h3", "Use your registered YubiKey device(s) to authenticate.");

    await cas.assertInvisibility(page, "button[name=register]");
    await cas.type(page, "#token", "12345678901234567890123456789012345");
    await cas.submitForm(page, "#yubiKeyForm", (response) => response.status() === 200);
    await cas.assertTextContentStartsWith(page, "div .banner-danger span", "Authentication attempt has failed");
    await cas.closeBrowser(browser);
})();
