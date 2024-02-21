const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/adaptive";
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.assertInnerTextContains(page, "#loginErrorsPanel p", "authentication attempt is determined to be risky");

    await cas.goto(page, "http://localhost:8282");
    await cas.click(page, "table tbody td a");
    await cas.waitForElement(page, "div[name=bodyPlainText] .well");
    const body = await cas.textContent(page, "div[name=bodyPlainText] .well");
    await cas.log(`Email message body is: ${body}`);
    assert(body.includes("casuser with score 1.00"));
    await browser.close();
})();
