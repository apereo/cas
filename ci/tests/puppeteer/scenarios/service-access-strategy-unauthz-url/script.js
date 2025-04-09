
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.screenshot(page);
    await cas.logPage(page);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    await cas.sleep(4000);
    const url = await page.url();
    assert(url === "https://localhost:9859/anything/info");
    await browser.close();
})();
