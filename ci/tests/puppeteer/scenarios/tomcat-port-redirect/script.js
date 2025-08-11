
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "http://localhost:8080/cas/login");
    await cas.logPage(page);
    await cas.assertPageUrl(page, "https://localhost:8443/cas/login");
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");
    await cas.closeBrowser(browser);
})();
