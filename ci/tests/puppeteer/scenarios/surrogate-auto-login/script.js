
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.loginWith(page, "unknown+casuser", "Mellon");
    await cas.assertInnerTextStartsWith(page, "#loginErrorsPanel p", "You are not authorized to impersonate");

    await cas.gotoLogin(page);
    await cas.loginWith(page, "user3+casuser", "Mellon");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user3, have successfully logged into the Central Authentication Service");

    const page2 = await browser.newPage();
    await page2.goto("http://localhost:8282");
    await cas.sleep(1000);
    await cas.click(page2, "table tbody td a");
    await cas.sleep(1000);

    await cas.closeBrowser(browser);
})();
