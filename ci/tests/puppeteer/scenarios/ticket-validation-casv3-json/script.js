const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://example.com";

    await page.goto(`https://localhost:8443/cas/login?service=${service}`);
    await cas.loginWith(page, "casuser", "Mellon");

    let ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    console.log(body)
    let json = JSON.parse(body);
    let authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    assert(authenticationSuccess.attributes.credentialType != null);
    assert(authenticationSuccess.attributes.isFromNewLogin != null);
    assert(authenticationSuccess.attributes.authenticationDate != null);
    assert(authenticationSuccess.attributes.authenticationMethod != null);
    assert(authenticationSuccess.attributes.successfulAuthenticationHandlers != null);
    assert(authenticationSuccess.attributes.longTermAuthenticationRequestTokenUsed != null);
    await browser.close();
})();
