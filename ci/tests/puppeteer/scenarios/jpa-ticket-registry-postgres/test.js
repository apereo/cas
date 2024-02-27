
const cas = require("../../cas.js");
const colors = require("colors");
const assert = require("assert");

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
    const url = await page.url();
    assert(url === "https://localhost:8443/cas/logout");

    await cas.sleep(1000);
    await cas.assertCookie(page, false);

    await browser.close();
    console.log(colors.green("Login test complete."));
})();
