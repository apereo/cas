const puppeteer = require("puppeteer");

const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-simple");
    await cas.loginWith(page);
    await cas.screenshot(page);
    await cas.assertVisibility(page, "#token");

    await cas.log("Attempting to resend ticket...");
    await cas.click(page, "#resendButton");
    await cas.screenshot(page);
    await cas.waitForElement(page, "#token");
    const page2 = await browser.newPage();
    await page2.goto("http://localhost:8282");
    await cas.click(page2, "table tbody td a");
    await cas.waitForElement(page2, "div[name=bodyPlainText] .well");
    const code = await cas.textContent(page2, "div[name=bodyPlainText] .well");

    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");

    await cas.submitForm(page, "#registerform");
    await cas.waitForElement(page, "#content div h2");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);

    await browser.close();
})();
