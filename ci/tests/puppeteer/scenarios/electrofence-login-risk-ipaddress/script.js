const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/adaptive";
    await cas.gotoLogin(page, service);
    await cas.sleep(2000);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertInnerTextContains(page, "#loginErrorsPanel p", "authentication attempt is determined to be risky");

    await cas.goto(page, "http://localhost:8282");
    await cas.sleep(1000);
    await cas.click(page, "table tbody td a");
    await cas.sleep(1000);
    const body = await cas.textContent(page, "div[name=bodyPlainText] .well");
    await cas.log(`Email message body is: ${body}`);
    assert(body.includes("casuser with score 1.00"));
    await browser.close();
})();
