
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    // perform a login at the CAS server for the CAS app as casuser
    await cas.goto(page, "https://localhost:8444");
    await cas.sleep(1000);
    await cas.goto(page, "https://localhost:8444/protected");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8444/protected");
    await cas.assertInnerTextContains(page, "div.starter-template h2 span", "casuser");
    await cas.sleep(1000);
    // renew login at the CAS server as admin
    await cas.gotoLogin(page, undefined, 8443, true);
    await cas.loginWith(page, "admin", "pwd");
    await cas.sleep(2000);
    // call the CAS app to confirm the new session as admin
    await cas.goto(page, "https://localhost:8444/protected");
    await cas.sleep(1000);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8444/protected");
    await cas.assertInnerTextContains(page, "div.starter-template h2 span", "admin");
    await cas.sleep(1000);
    await browser.close();
})();
