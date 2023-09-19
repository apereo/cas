const puppeteer = require('puppeteer');
const assert = require("assert");
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login?authn_method=mfa-simple");
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    await cas.assertVisibility(page, '#token');

    const page2 = await browser.newPage();
    await page2.goto("http://localhost:8282");
    await page2.waitForTimeout(1000);
    await cas.click(page2, "table tbody td a");
    await page2.waitForTimeout(1000);

    await page2.evaluate((selector) => {
        const element = document.querySelector(selector);
        if (element) {
            element.scrollIntoView();
        }
    }, "#greeting");
    await page2.waitForTimeout(1000);
    await cas.screenshot(page);

    let greeting = await cas.textContent(page2, "#greeting");
    assert(greeting === "Hello, CAS Apereo");
    let code = await cas.textContent(page2, "#otpcode");
    await cas.log(`Code to use is extracted as ${code}`);
    await page2.close();

    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await page.waitForTimeout(1000);
    await cas.submitForm(page, "#registerform");
    await page.waitForTimeout(1000);
    await cas.assertCookie(page);

    await browser.close();
})();
