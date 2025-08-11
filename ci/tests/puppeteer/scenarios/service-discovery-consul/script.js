
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const response = await cas.goto(page, "http://localhost:8500/ui/dc1/services/cas/instances");
    await cas.sleep(2000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    await cas.click(page, "div.header a");
    await page.waitForResponse((response) => response.status() === 200);
    await cas.sleep(2000);
    await cas.closeBrowser(browser);
})();
