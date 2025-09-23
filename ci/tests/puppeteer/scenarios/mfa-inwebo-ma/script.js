
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://127.0.0.1:8443/cas/login?authn_method=mfa-inwebo");
    await cas.loginWith(page, "testcas", "password");
    await cas.sleep(5000);
    await cas.screenshot(page);
    await cas.log("Asking for the PIN code");
    await cas.assertVisibility(page, "#code");
    await cas.assertVisibility(page, "#pin");
    await cas.assertVisibility(page, "#enrollButton");
    await cas.closeBrowser(browser);
})();
