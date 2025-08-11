
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);
    await cas.goto(page, "https://localhost:8443/cas");
    await cas.assertCookie(page);
    await cas.closeBrowser(browser);
})();
