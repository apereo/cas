
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.sleep(2000);
    await cas.assertVisibility(page, "li #CasClient");
    await cas.screenshot(page);
    await cas.click(page, "li #CasClient");
    await cas.waitForNavigation(page);
    await cas.sleep(3000);
    await cas.screenshot(page);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.screenshot(page);
    await cas.logPage(page);
    await cas.assertParameter(page, "ticket");
    await cas.assertParameter(page, "client_name");
    await cas.assertPageUrlContains(page, "https://localhost:8443/cas/login");
    await browser.close();
})();

