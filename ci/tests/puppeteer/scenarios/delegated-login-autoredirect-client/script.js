
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.log("Checking for page URL redirecting, based on service policy...");
    await cas.logPage(page);
    await cas.sleep(1000);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8444/cas/login");

    await cas.gotoLogin(page, "https://localhost:9859/anything/sample");
    await cas.log("Checking for page URL...");
    await cas.assertPageUrlStartsWith(page, "https://localhost:8444/cas/login");
    await cas.closeBrowser(browser);
})();
