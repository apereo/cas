
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8444");
    await cas.sleep(1000);
    await cas.goto(page, "https://localhost:8444/protected");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.logPage(page);
    const url = await page.url();
    assert(url.startsWith("https://localhost:8444/protected"));
    await cas.assertInnerTextContains(page, "div.starter-template h2 span", "casuser");
    await cas.sleep(1000);
    await cas.gotoLogin(page, "https://localhost:8444/protected", 8443, true);
    await cas.loginWith(page, "admin", "pwd");
    await cas.sleep(2000);
    await cas.assertInnerTextContains(page, "div.starter-template h2 span", "admin");
    await cas.sleep(1000);
    await cas.shutdownCas("https://localhost:8444");
    await browser.close();
})();
