const cas = require("../../cas.js");

(async () => {
    const service = "https://localhost:9859/anything/cas";
    
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.sleep(2000);
    await cas.assertInvisibility(page, "#turnstileSection");

    await cas.gotoLogin(page, service);
    await cas.sleep(4000);
    await cas.assertVisibility(page, "#turnstileSection");
    
    await cas.closeBrowser(browser);
})();
