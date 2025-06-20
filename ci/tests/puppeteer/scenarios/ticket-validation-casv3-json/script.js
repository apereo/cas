
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://example.com";

    await cas.gotoLogin(page, service);
    await cas.loginWith(page);

    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(service, ticket);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    assert(authenticationSuccess.attributes.credentialType !== undefined);
    assert(authenticationSuccess.attributes.isFromNewLogin !== undefined);
    assert(authenticationSuccess.attributes.authenticationDate !== undefined);
    assert(authenticationSuccess.attributes.authenticationMethod !== undefined);
    assert(authenticationSuccess.attributes.successfulAuthenticationHandlers !== undefined);
    assert(authenticationSuccess.attributes.longTermAuthenticationRequestTokenUsed !== undefined);
    await browser.close();
})();
