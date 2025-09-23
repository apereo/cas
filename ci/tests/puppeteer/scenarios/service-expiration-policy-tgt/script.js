
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.sleep(4000);
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");
    await cas.closeBrowser(browser);
})();
