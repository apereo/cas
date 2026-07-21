
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.goto(page, "http://localhost:8761");
    const app = await page.evaluate((element) => {
        const elements = document.querySelectorAll(element);
        const btn = elements[elements.length - 1];
        return btn.querySelectorAll(":scope > tbody tr td")[0].innerText;
    }, "table#instances");
    assert(app === "CAS");
    await cas.closeBrowser(browser);

    await cas.sleep(3000);
    await cas.doGet("https://localhost:8443/cas/actuator/clusterTopology/discovery",
        (res) => {
            cas.log(res.data);
        },
        (error) => {
            throw (error);
        }, {
            "Content-Type": "application/json"
        });
})();
