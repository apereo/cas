const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    let service = "https://localhost:9859/anything/adaptive";
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    await page.waitForTimeout(2000);
    await cas.loginWith(page);
    await page.waitForTimeout(2000);

    await cas.screenshot(page);

    const page2 = await browser.newPage();
    await page2.goto("http://localhost:8282");
    await page2.waitForTimeout(1000);
    await cas.click(page2, "table tbody td a[title=CAS]");
    await page2.waitForTimeout(1000);
    let code = await cas.textContent(page2, "div[name=bodyPlainText] .well");
    await cas.screenshot(page);
    await page2.close();

    const page3 = await browser.newPage();
    await cas.goto(page3, "http://localhost:8282");
    await page3.waitForTimeout(1000);
    await cas.click(page3, "table tbody td a[title=CasRiskyAuthN]");
    await page3.waitForTimeout(1000);
    let body = await cas.textContent(page3, "div[name=bodyPlainText] .well");
    await cas.screenshot(page);
    await cas.log(`Email message body is: ${body}`);
    assert(body.includes("casuser with score 1.00"));
    await page3.close();

    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await page.waitForTimeout(2000);
    const url = `${await page.url()}`;
    await cas.logPage(page);
    assert(url.includes(service));
    await cas.assertTicketParameter(page);

    await cas.goto(page, `https://localhost:8443/cas/login`);
    await cas.assertCookie(page);
    await cas.click(page, "#auth-tab");
    await page.waitForTimeout(1000);
    await cas.screenshot(page);
    await cas.type(page, "#attribute-tab-1 input[type=search]", "risk");
    await cas.screenshot(page);
    await cas.assertInnerTextStartsWith(page, "#triggeredRiskBasedAuthentication td code kbd", "[true]");
    
    await browser.close();
})();
