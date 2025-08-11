
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page, "https://google.com");
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page);
    await cas.assertCookie(page);

    await cas.gotoLogin(page, "https://github.com/apereo/cas");
    await cas.sleep(500);
    await cas.assertInvisibility(page, "#username");

    await cas.log("Selecting mfa-gauth");
    await cas.assertVisibility(page, "#mfa-gauth");
    await cas.assertVisibility(page, "#mfa-yubikey");

    await cas.submitForm(page, "#mfa-gauth > form[name=fm-mfa-gauth]");
    await cas.sleep(500);

    let scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    await cas.log(`Using scratch code ${scratch} to login...`);
    await cas.type(page,"#token", scratch);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page);
    await cas.sleep(1000);

    await cas.assertInnerTextStartsWith(page, "#authnContextClass td.attribute-value", "[mfa-gauth]");
    
    await cas.gotoLogin(page, "https://github.com/apereo/cas");
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://github.com/");

    await cas.gotoLogout(page);
    await cas.assertCookie(page, false);

    await cas.log("Starting with MFA selection menu");
    await cas.gotoLogin(page, "https://github.com/apereo/cas");
    await cas.sleep(500);
    await cas.loginWith(page);
    await cas.submitForm(page, "#mfa-gauth > form[name=fm-mfa-gauth]");
    await cas.sleep(500);

    scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    await cas.log(`Using scratch code ${scratch} to login...`);
    await cas.type(page,"#token", scratch);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);

    await cas.log("Navigating to second service with SSO session");
    await cas.gotoLogin(page, "https://github.com/apereo");
    await cas.sleep(1000);
    await cas.assertInvisibility(page, "#username");
    await cas.assertTicketParameter(page);

    await cas.closeBrowser(browser);
})();
