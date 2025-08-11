const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#forgotPasswordLink", "Reset your password");
    await cas.click(page, "#forgotPasswordLink");
    await cas.sleep(2000);

    await cas.type(page,"#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.screenshot(page);

    await cas.assertInnerText(page, "#content h2", "Password Reset Instructions Sent Successfully.");
    await cas.assertInnerTextStartsWith(page, "#content p", "You should shortly receive a message");
    
    const link = await cas.extractFromEmail(browser);
    assert(link !== undefined);
    await cas.goto(page, link);
    await cas.sleep(1000);
    
    await cas.assertVisibility(page, "#mfa-gauth");
    await cas.assertVisibility(page, "#mfa-simple");

    await cas.log("Selecting mfa-gauth");
    await cas.submitForm(page, "#mfa-gauth > form[name=fm-mfa-gauth]");
    await cas.sleep(1000);

    const scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    await cas.log(`Using scratch code ${scratch} to login...`);
    await cas.type(page,"#token", scratch);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertInnerText(page, "#pwdmain h3", "Hello, casuser. You must change your password.");

    await cas.type(page,"#password", "Jv!e0mKD&dCNl^Q");
    await cas.type(page,"#confirmedPassword", "Jv!e0mKD&dCNl^Q");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertInnerText(page, "#content h2", "Password Change Successful");
    
    await cas.closeBrowser(browser);
})();
