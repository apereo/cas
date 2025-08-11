
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.loginWith(page, "casscimuser", "Mellon");

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");

    await cas.doGet("http://localhost:9666/scim/v2/Users?attributes=userName",
        (res) => {
            assert(res.status === 200);
            const length = res.data.Resources.length;
            cas.log(`Found ${length} record`);
            assert(length === 1);
            assert(res.data.Resources[0].userName === "casscimuser");
        },
        (error) => {
            throw error;
        }, { "Authorization": "Basic c2NpbS11c2VyOmNoYW5nZWl0" });
    
    await cas.closeBrowser(browser);
})();
