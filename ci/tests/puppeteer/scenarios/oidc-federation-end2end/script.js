const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const url = "http://localhost:8080";
    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.sleep(1000);

    await cas.click(page, "#protect");
    await cas.sleep(3000);

    await cas.loginWith(page);
    await cas.sleep(2000);

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }
    await cas.sleep(2000);

    await cas.assertPageUrl(page, "http://localhost:8080/protected/index");
    await cas.assertInnerTextContains(page, "body", "OidcProfile");

    await cas.closeBrowser(browser);
})();
