const puppeteer = require("puppeteer");

const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-simple");
    await cas.loginWith(page);
    await cas.waitForTimeout(page, 2000);
    await cas.screenshot(page);
    await cas.assertVisibility(page, "#token");

    await cas.log("Attempting to resend ticket...");
    await cas.click(page, "#resendButton");
    await cas.waitForTimeout(page, 1000);
    await cas.screenshot(page);
    await page.waitForSelector("#token", {visible: true});

    const code = await cas.extractFromEmailMessage(browser);

    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.waitForTimeout(page, 1000);
    await cas.submitForm(page, "#registerform");
    await cas.waitForTimeout(page, 1000);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);

    await browser.close();
})();
