
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-simple");
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.assertVisibility(page, "#token");

    await cas.log("Attempting to resend ticket...");
    await cas.click(page, "#resendButton");
    await cas.sleep(1000);
    await cas.screenshot(page);
    await page.waitForSelector("#token", {visible: true});

    const code = await cas.extractFromEmail(browser);

    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(1000);
    await cas.submitForm(page, "#registerform");
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);

    await cas.closeBrowser(browser);
})();
