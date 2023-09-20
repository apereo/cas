const puppeteer = require("puppeteer");
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    let scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    const page = await cas.newPage(browser);
    const service = "https://google.com";
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    await page.waitForTimeout(1000);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(500);

    await cas.log("Select mfa-gauth");
    await cas.assertVisibility(page, '#mfa-gauth');

    await cas.submitForm(page, "#mfa-gauth > form[name=fm-mfa-gauth]");
    await page.waitForTimeout(1000);

    await cas.log(`Using scratch code ${scratch} to login...`);
    await cas.type(page,'#token', scratch);
    await cas.pressEnter(page);
    await page.waitForNavigation();
    await page.waitForTimeout(1000);

    let ticket = await cas.assertTicketParameter(page);

    await cas.log(`Validating ticket ${ticket} with service ${service}`);
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}`);

    await cas.logg(body);
    
    assert(body.includes("<cas:authenticationSuccess>"));
    assert(body.includes("<cas:user>casuser</cas:user>"));
    assert(body.includes("<cas:credentialType>GoogleAuthenticatorTokenCredential</cas:credentialType>"));

    assert(body.includes("<cas:authenticationMethod>Accept</cas:authenticationMethod>"));
    assert(body.includes("<cas:authenticationMethod>GoogleAuth</cas:authenticationMethod>"));

    assert(body.includes("<cas:authnContextClass>mfa-gauth</cas:authnContextClass>"));

    assert(body.includes("<cas:successfulAuthenticationHandlers>Accept</cas:successfulAuthenticationHandlers>"));
    assert(body.includes("<cas:successfulAuthenticationHandlers>GoogleAuth</cas:successfulAuthenticationHandlers>"));

    await browser.close();
})();
