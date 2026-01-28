
const cas = require("../../cas.js");

async function verifyWithoutService() {
    const browser1 = await cas.newBrowser(cas.browserOptions());
    const context1 = await browser1.createBrowserContext();
    const page = await cas.newPage(context1);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await context1.close();
    await cas.closeBrowser(browser1);

    const browser2 = await cas.newBrowser(cas.browserOptions());
    const context2 = await browser2.createBrowserContext();
    const page2 = await cas.newPage(context2);
    await page2.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page2, "casuser", "Mellon");
    await cas.sleep(2000);
    await cas.assertInnerTextStartsWith(page2, "#loginErrorsPanel p",
        "You cannot log in at this time, since you have another active single sign-on session in progress");
    await context2.close();
    await cas.closeBrowser(browser2);
}

async function verifyWithService() {
    const browser1 = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser1);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.closeBrowser(browser1);

    const browser2 = await cas.newBrowser(cas.browserOptions());
    const page2 = await cas.newPage(browser2);
    await page2.goto("https://localhost:8443/cas/login?service=https://localhost:9859/anything/1");
    await cas.loginWith(page2, "casuser", "Mellon");
    await cas.sleep(2000);
    await cas.assertInnerTextStartsWith(page2, "#loginErrorsPanel p",
        "You cannot log in at this time, since you have another active single sign-on session in progress");
    await cas.closeBrowser(browser2);
}

(async () => {
    await verifyWithoutService();
    await verifyWithService();
})();
