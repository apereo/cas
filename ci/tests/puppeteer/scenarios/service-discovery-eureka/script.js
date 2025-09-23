
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.goto(page, "https://localhost:8761");
    const app = await page.evaluate((element) => {
        const elements = document.querySelectorAll(element);
        const btn = elements[elements.length - 1];
        return btn.querySelectorAll(":scope > tbody tr td")[0].innerText;
    }, "table#instances");
    assert(app === "CAS");
    await cas.closeBrowser(browser);
})();
