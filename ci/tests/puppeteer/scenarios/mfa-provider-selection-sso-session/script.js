const puppeteer = require("puppeteer");
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://google.com");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertTicketParameter(page);

    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.assertCookie(page);

    await cas.goto(page, "https://localhost:8443/cas/login?service=https://github.com/apereo/cas");
    await page.waitForTimeout(500);
    await cas.assertInvisibility(page, "#username");

    await cas.log("Selecting mfa-gauth");
    await cas.assertVisibility(page, '#mfa-gauth');
    await cas.assertVisibility(page, '#mfa-yubikey');

    await cas.submitForm(page, "#mfa-gauth > form[name=fm-mfa-gauth]");
    await page.waitForTimeout(500);

    let scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    await cas.log(`Using scratch code ${scratch} to login...`);
    await cas.type(page,'#token', scratch);
    await cas.pressEnter(page);
    await page.waitForNavigation();

    await cas.assertTicketParameter(page);

    await cas.goto(page, "https://localhost:8443/cas/login");
    await page.waitForTimeout(1000);

    await cas.assertInnerTextStartsWith(page, "#authnContextClass td.attribute-value", "[mfa-gauth]");
    
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://github.com/apereo/cas");
    const url = await page.url();
    await cas.log(`Page url: ${url}`);
    assert(url.startsWith("https://github.com/"));

    await cas.goto(page, "https://localhost:8443/cas/logout");
    await cas.assertCookie(page, false);

    await cas.log("Starting with MFA selection menu");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://github.com/apereo/cas");
    await page.waitForTimeout(500);
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.submitForm(page, "#mfa-gauth > form[name=fm1]");
    await page.waitForTimeout(500);

    scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    await cas.log(`Using scratch code ${scratch} to login...`);
    await cas.type(page,'#token', scratch);
    await cas.pressEnter(page);
    await page.waitForNavigation();

    await cas.log("Navigating to second service with SSO session");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://github.com/apereo");
    await page.waitForTimeout(1000);
    await cas.assertInvisibility(page, "#username");
    await cas.assertTicketParameter(page);

    await browser.close();
})();
