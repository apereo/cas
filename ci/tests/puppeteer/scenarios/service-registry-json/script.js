
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.logPage(page);
    await cas.assertTicketParameter(page);
    await cas.closeBrowser(browser);
})();
