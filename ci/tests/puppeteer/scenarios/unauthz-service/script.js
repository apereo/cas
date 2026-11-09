
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "unknown-service");
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");

    await cas.log("A gateway request for an unregistered service is refused rather than redirected");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://localhost:9859/anything/unknown&gateway=true");
    await cas.sleep(1000);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/login");
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    await cas.assertElementDoesNotExist(page, "#username");
    await cas.closeBrowser(browser);
})();
