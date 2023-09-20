const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    let service = "https://localhost:9859/anything/adaptive";
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    await page.waitForTimeout(1000);
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    await cas.assertInnerTextContains(page, "#loginErrorsPanel p", "authentication attempt is determined to be risky");
    await cas.assertCookie(page, false);
    await cas.goto(page, "http://localhost:8282");
    await page.waitForTimeout(5000);
    await cas.click(page, "table tbody td a");
    await page.waitForTimeout(1000);
    let body = await cas.textContent(page, "div[name=bodyPlainText] .well");
    await cas.log(`Email message body is: ${body}`);
    const link = body.substring(body.indexOf("link=") + 5);
    await cas.logg(`Verification link is ${link}`);
    let response = await cas.goto(page, link);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    await cas.assertInnerText(page, "#content h2", "Risky Authentication attempt is confirmed.");

    await cas.goto(page, `https://localhost:8443/cas/logout`);
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    await page.waitForTimeout(1000);
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    await cas.assertTicketParameter(page);

    await browser.close();
})();
