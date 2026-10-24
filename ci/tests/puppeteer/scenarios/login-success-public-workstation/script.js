
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Trying first app with a fancy theme");
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#publicWorkstationButton");

    await cas.log("Theme properties from the template prefix apply in every browser language");
    const germanPage = await cas.newPage(browser);
    await germanPage.setExtraHTTPHeaders({"Accept-Language": "de-DE,de;q=0.9"});
    await cas.gotoLogin(germanPage, "https://localhost:9859/anything/cas");
    await cas.sleep(1000);
    await cas.assertVisibility(germanPage, "#publicWorkstationButton");
    await germanPage.close();

    await cas.click(page, "#publicWorkstationButton");
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");
    await cas.sleep(1000);
    await cas.closeBrowser(browser);
})();
