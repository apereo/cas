
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page, "https://localhost:9859/anything/cas1");
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.sleep(1000);

    await cas.gotoLogin(page, "https://localhost:9859/anything/cas1");
    await cas.assertTicketParameter(page);
    await cas.sleep(1000);

    await cas.log("Attempting to access service again, now without SSO session");
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas2");
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);
    await cas.sleep(1000);

    await cas.log("Attempting to access service again, now with SSO session");
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas1");
    await cas.assertTicketParameter(page);
    await cas.sleep(1000);

    await cas.log("Attempting to access service once again, now without SSO session");
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas2");
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);
    await cas.sleep(1000);

    await cas.closeBrowser(browser);
})();
