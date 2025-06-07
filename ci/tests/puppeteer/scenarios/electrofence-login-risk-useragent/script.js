
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/adaptive";
    await cas.gotoLogin(page, service);
    await cas.sleep(2000);
    await cas.loginWith(page);
    await cas.sleep(2000);

    await cas.screenshot(page);

    const code = await cas.extractFromEmail(browser);

    const page3 = await browser.newPage();
    await cas.goto(page3, "http://localhost:8282");
    await cas.sleep(1000);
    await cas.click(page3, "table tbody td a[title=CasRiskyAuthN]");
    await cas.sleep(1000);
    const body = await cas.textContent(page3, "div[name=bodyPlainText] .well");
    await cas.screenshot(page);
    await cas.log(`Email message body is: ${body}`);
    assert(body.includes("casuser with score 1.00"));
    await page3.close();

    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(2000);
    await cas.logPage(page);
    await cas.assertPageUrlContains(page, service);
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.click(page, "#auth-tab");
    await cas.sleep(1000);
    await cas.screenshot(page);
    await cas.type(page, "#attribute-tab-1 input[type=search]", "risk");
    await cas.screenshot(page);
    await cas.assertInnerTextStartsWith(page, "#triggeredRiskBasedAuthentication td code kbd", "[true]");
    
    await browser.close();
})();
