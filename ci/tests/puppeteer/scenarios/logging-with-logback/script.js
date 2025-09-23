
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");

    const baseUrl = "https://localhost:8443/cas/actuator/";
    await cas.doGet(`${baseUrl}loggers`,
        (res) => {
            assert(res.data.loggers.ROOT.configuredLevel === "OFF");
            assert(res.data.loggers["org.apereo.cas"].configuredLevel === "INFO");
        },
        (err) => {
            throw err;
        });
    await cas.closeBrowser(browser);
})();
