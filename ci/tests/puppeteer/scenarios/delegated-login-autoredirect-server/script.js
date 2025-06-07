
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8444/cas/logout");
    await cas.log("Checking for page URL redirecting based on service policy...");
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.sleep(2000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8444/cas/login");
    await cas.loginWith(page);
    await cas.sleep(8000);
    await cas.assertTicketParameter(page);
    await cas.logPage(page);
    await cas.sleep(2000);

    await cas.log("Checking for SSO availability of our CAS server...");
    await cas.gotoLogin(page);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/login");
    await cas.sleep(2000);
    await cas.assertCookie(page);

    await cas.log("Checking for SSO availability of external CAS server...");
    await cas.goto(page, "https://localhost:8444/cas/login");
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8444/cas/login");
    await cas.sleep(2000);
    await cas.assertCookie(page, true, "TGCEXT");

    await cas.log("Attempting to login based on existing SSO session");
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.logPage(page);
    await cas.sleep(4000);
    await cas.assertTicketParameter(page);

    await cas.log("Removing CAS SSO session");
    await cas.gotoLogout(page);

    await cas.log("Attempting to login for a different 2nd service");
    await cas.gotoLogin(page, "https://localhost:9859/anything/sample");
    await cas.log("Checking for page URL...");
    await cas.logPage(page);
    await cas.sleep(2000);

    await cas.log("External CAS server has no SSO, since logout request was propagated from our CAS server");
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");

    await browser.close();
})();
