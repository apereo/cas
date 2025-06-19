
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8443/cas/auth");
    await cas.sleep(2000);
    await cas.logPage(page);
    await cas.assertPageUrl(page, "https://localhost:8443/cas/auth");
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.sleep(2000);
    await cas.goto(page, "https://localhost:8443/cas/off");
    await cas.sleep(2000);
    await cas.assertCookie(page, false);

    await browser.close();
})();
