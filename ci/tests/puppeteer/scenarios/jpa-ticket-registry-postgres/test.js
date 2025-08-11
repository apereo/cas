
const cas = require("../../cas.js");
const colors = require("colors");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.loginWith(page);

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");

    await cas.gotoLogout(page);
    await cas.logPage(page);
    await cas.assertPageUrl(page, "https://localhost:8443/cas/logout");

    await cas.sleep(1000);
    await cas.assertCookie(page, false);

    await browser.close();
})();
