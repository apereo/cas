const puppeteer = require('puppeteer');
const assert = require("assert");
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login?authn_method=mfa-simple");
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    await cas.screenshot(page);

    await cas.assertVisibility(page, '#continueButton');
    await cas.log("Checking all emails");
    await page.evaluate(() => {
        const emails = document.querySelectorAll("input[type=checkbox]");
        emails.forEach(lnk => {
            const id = lnk.getAttribute("id");
            console.log(id);
            lnk.click();
        });
    });
    await cas.screenshot(page);
    await cas.submitForm(page, "#fm1");

    await cas.assertVisibility(page, '#token');

    const page2 = await browser.newPage();
    await page2.goto("http://localhost:8282");
    await page2.waitForTimeout(1000);
    await cas.click(page2, "table tbody td a");
    await page2.waitForTimeout(1000);
    let addresses = await cas.textContent(page2, "div[name=addresses] span");
    assert(addresses.includes("casperson@example.com"));
    assert(addresses.includes("casuser@example.org"));
    let code = await cas.textContent(page2, "div[name=bodyPlainText] .well");
    await cas.log(`Code to use is extracted as ${code}`);
    await page2.close();
    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await page.waitForTimeout(1000);
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await page.waitForTimeout(1000);
    await cas.assertCookie(page);
    await browser.close();
})();
