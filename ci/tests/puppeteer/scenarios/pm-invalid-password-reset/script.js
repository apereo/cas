
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    await cas.log("Attempting invalid password reset without SSO");
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login?pswdrst=bad_token_with_no_sso");
    await cas.assertInnerText(page, "#content h2", "Password Reset Failed");
    await cas.log("Attempting invalid password reset with SSO");
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.goto(page, "https://localhost:8443/cas/login?pswdrst=bad_token_with_sso");
    await cas.assertInnerText(page, "#content h2", "Password Reset Failed");
    await browser.close();
})();
