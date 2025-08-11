const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-simple");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.screenshot(page);
    await cas.assertInnerText(page, "#content h2", "Account Registration");
    await cas.assertVisibility(page, "#fmRegister");

    await cas.logb("Registering invalid account...");
    await cas.type(page, "#email", "bob@unknown.com");
    await cas.submitForm(page, "#fmRegister");
    await cas.assertInnerTextStartsWith(page, "#errorPanel p", "Unable to register your account");
    
    await cas.logb("Registering valid account...");
    await cas.type(page, "#email", "cas@example.org");
    await cas.submitForm(page, "#fmRegister");

    await cas.logb("Extracting registration code from email");
    await cas.sleep(1000);
    let code = await cas.extractFromEmail(browser);
    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");

    await cas.sleep(1000);
    await cas.logb("Extracting mfa code from email");
    code = await cas.extractFromEmail(browser);
    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);

    await cas.closeBrowser(browser);
})();
