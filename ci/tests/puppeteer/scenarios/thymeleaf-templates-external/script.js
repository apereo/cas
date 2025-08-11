const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.assertVisibility(page, "#externalFooter");
    const response = await cas.goto(page, "https://localhost:8443/cas/pages/index.txt");
    await cas.sleep(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    await cas.closeBrowser(browser);
})();
