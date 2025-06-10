
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const service = "https://localhost:9859/anything/cas";

    await cas.gotoLogin(page, service);
    await cas.log("Checking for page URL...");
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8444/cas/login");
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(8000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, service);
    await cas.assertTicketParameter(page);

    await cas.log("Attempting login after SSO...");
    await cas.gotoLogin(page, service);
    await cas.sleep(3000);
    await cas.logPage(page);
    await cas.assertTicketParameter(page);

    await browser.close();
})();
