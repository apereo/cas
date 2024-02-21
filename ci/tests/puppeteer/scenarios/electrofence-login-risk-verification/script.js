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
    await cas.assertCookie(page, false);
    await cas.goto(page, "http://localhost:8282");
    await cas.click(page, "table tbody td a");
    await cas.waitForElement(page, "div[name=bodyPlainText] .well");
    const body = await cas.textContent(page, "div[name=bodyPlainText] .well");
    await cas.log(`Email message body is: ${body}`);
    const link = body.substring(body.indexOf("link=") + 5);
    await cas.logg(`Verification link is ${link}`);
    const response = await cas.goto(page, link);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    await cas.assertInnerText(page, "#content h2", "Risky Authentication attempt is confirmed.");

    await cas.gotoLogout(page);
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);

    await browser.close();
})();
