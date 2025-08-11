
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.updateDuoSecurityUserStatus("duocode");
    await cas.type(page, "#username", "duocode");
    await cas.pressEnter(page);
    await cas.screenshot(page);
    await cas.log("Waiting for Duo MFA to complete. Duo Push times out after 60 seconds...");
    await cas.sleep(63000);
    await cas.screenshot(page);
    await cas.assertVisibility(page, "#token");
    await cas.sleep(1000);
    
    await cas.closeBrowser(browser);
})();
