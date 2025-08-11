
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const context = browser.defaultBrowserContext();
    
    await context.overridePermissions("https://localhost:8443/cas/login", ["geolocation"]);
    await page.setGeolocation({latitude: 90, longitude: 20});

    await cas.gotoLogin(page);
    await cas.sleep(2000);

    await cas.loginWith(page);
    await cas.assertTextContent(page, "#content h2", "Authentication attempt is blocked.");

    await cas.closeBrowser(browser);
})();
