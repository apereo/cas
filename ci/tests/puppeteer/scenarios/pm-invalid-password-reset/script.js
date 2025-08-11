const cas = require("../../cas.js");
const assert = require("assert");

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

    const service = "https://localhost:9859/anything/cas";
    const baseEndpoint = "https://localhost:8443/cas/actuator";
    await cas.doPost(`${baseEndpoint}/passwordManagement/reset/casuser?service=${service}`,
        "",
        {
            "Content-Type": "application/json"
        }, (res) => assert(res.status === 200), (error) => {
            throw `Operation failed: ${error}`;
        });
    await cas.sleep(2000);
    const link = await cas.extractFromEmail(browser);
    assert(link !== undefined);
    await cas.goto(page, link);
    await cas.sleep(2000);
    await cas.assertTextContent(page, "#pwdmain h3", "Hello, casuser. You must change your password.");
    await cas.closeBrowser(browser);
})();
