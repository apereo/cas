
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://apereo.github.io";
    await cas.gotoLogin(page, `${service}&renew=true`);
    await cas.assertVisibility(page, "#username");
    await cas.logg("Waiting for the service registry cache to expire...");
    await cas.sleep(3000);
    await cas.gotoLogin(page, `${service}&renew=true`);
    await cas.assertVisibility(page, "#username");

    const baseUrl = "https://localhost:8443/cas/actuator/registeredServices/type";
    await cas.doGet(`${baseUrl}/CasRegisteredService`,
        (res) => {
            assert(res.status === 200);
            assert(res.data[1].length === 1);
        },
        (error) => {
            throw error;
        }, {"Content-Type": "application/json"});
    await cas.doGet(`${baseUrl}/OidcRegisteredService`,
        (res) => {
            assert(res.status === 200);
            assert(res.data[1].length === 0);
        },
        (error) => {
            throw error;
        }, {"Content-Type": "application/json"});

    await cas.closeBrowser(browser);
})();
