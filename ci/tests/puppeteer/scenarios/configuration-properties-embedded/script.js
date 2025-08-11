
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const authzCredentials = "casuser:Mellon";
    const buff = Buffer.alloc(authzCredentials.length, authzCredentials);
    const authzHeader = `Basic ${buff.toString("base64")}`;
    await cas.refreshContext("https://localhost:8443/cas", {
        "Authorization": authzHeader
    });
    await cas.sleep(2000);
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "securecas", "paSSw0rd");
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.doGet(`https://${authzCredentials}@localhost:8443/cas/actuator/info`,
        (res) => assert(res.status === 200), (err) => {
            throw err;
        });

    await cas.closeBrowser(browser);
})();
