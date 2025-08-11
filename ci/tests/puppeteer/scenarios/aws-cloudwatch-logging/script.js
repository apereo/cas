const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.closeBrowser(browser);

    const count = 10;
    await cas.doGet(`https://localhost:8443/cas/actuator/cloudWatchLogs/stream?count=${count}`,
        async (res) => {
            assert(res.data.length === count);
            res.data.forEach((entry) => {
                assert(entry.message !== null);
                assert(entry.timestamp !== null);
                assert(entry.level !== null);
            });
        }, async (err) => {
            throw (err);
        });

})();
