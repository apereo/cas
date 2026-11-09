
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.screenshot(page);
    await cas.logPage(page);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    await cas.sleep(4000);
    await cas.assertPageUrl(page, "https://localhost:9859/anything/info");

    await cas.log("A gateway request for a service the user may not access returns to the service without a ticket");
    await cas.log("and skips both the error page and the unauthorized redirect URL");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://localhost:9859/anything/cas&gateway=true");
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:9859/anything/cas");
    await cas.assertTicketParameter(page, false);
    await cas.closeBrowser(browser);
})();
