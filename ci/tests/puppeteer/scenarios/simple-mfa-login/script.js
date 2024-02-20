const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-simple", "en");
    await cas.loginWith(page);

    await cas.assertVisibility(page, "#token");
    await cas.attributeValue(page, "html", "lang", "en");

    // Call MockMock - SMTP Mock Server
    const page2 = await browser.newPage();
    await page2.goto("http://localhost:8282");
    await cas.waitForTimeout(page2, 1000);
    await cas.click(page2, "table tbody td a");
    await cas.waitForTimeout(page2, 1000);
    const code = await cas.textContent(page2, "div[name=bodyPlainText] .well");
    await page2.close();

    await page.bringToFront();
    await cas.attributeValue(page, "html", "lang", "en");
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.waitForTimeout(page, 3000);

    await cas.submitForm(page, "#registerform");
    await cas.waitForTimeout(page, 3000);

    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);

    await browser.close();
})();
