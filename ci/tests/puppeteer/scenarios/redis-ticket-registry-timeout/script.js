const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    await cas.logg("Removing all SSO Sessions");
    await cas.doDelete("https://localhost:8443/cas/actuator/ssoSessions?type=ALL&from=1&count=100000");
    
    const service = "https://localhost:9859/anything/cas";
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.sleep(3000);

    await cas.log("The SSO session should be gone now.");
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);

    await cas.log("Creating new SSO session and keeping it alive...");
    await cas.loginWith(page);
    await cas.assertCookie(page);
    
    await cas.log("Asking for service tickets to ensure SSO session remains updated...");
    for (let i = 0; i < 5; i++) {
        await cas.sleep(1000);
        await cas.gotoLogin(page, service);
        await cas.assertTicketParameter(page);
    }
    await cas.sleep(3000);
    await cas.log("The SSO session should be gone now");
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);
    await browser.close();

    await cas.doGet("https://localhost:8443/cas/actuator/ticketRegistry/query?type=TGT", async (res) => {
        assert(res.status === 200);
        assert(res.data.length === 0);
    }, async (err) => {
        throw err;
    }, {
        "Accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded"
    });
})();

