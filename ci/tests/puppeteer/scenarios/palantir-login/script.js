const puppeteer = require("puppeteer");
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/palantir/dashboard");
    await cas.loginWith(page, "casadmin", "password");
    await cas.waitForTimeout(page);

    await cas.waitForTimeout(page);
    await cas.screenshot(page);
    const response = await cas.goto(page, "https://localhost:8443/cas/palantir/dashboard/services");

    await cas.screenshot(page);
    assert(response.ok());
    await browser.close();
})();
