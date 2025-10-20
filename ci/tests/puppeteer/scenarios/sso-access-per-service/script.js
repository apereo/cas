
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const ssoDisabledService2 = "https://localhost:9859/anything/cas2";
    const ssoEnabledService = "https://localhost:9859/anything/cas1";

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page, ssoEnabledService);
    await cas.loginWith(page);
    let ticket = await cas.assertTicketParameter(page);
    await cas.sleep(1000);
    let json = await cas.validateTicket(ssoEnabledService, ticket);
    let authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes.randomNumber.length === 1);

    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.sleep(1000);

    await cas.gotoLogin(page, ssoEnabledService);
    ticket = await cas.assertTicketParameter(page);
    await cas.sleep(1000);
    json = await cas.validateTicket(ssoEnabledService, ticket);
    authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes.randomNumber.length === 1);

    await cas.log("Attempting to access service again, now without SSO session");
    await cas.gotoLogin(page, ssoDisabledService2);
    await cas.loginWith(page);
    ticket = await cas.assertTicketParameter(page);
    await cas.sleep(1000);
    json = await cas.validateTicket(ssoDisabledService2, ticket);
    authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes.randomNumber.length === 1);

    await cas.log("Attempting to access service again, now with SSO session");
    await cas.gotoLogin(page, ssoEnabledService);
    await cas.assertTicketParameter(page);
    await cas.sleep(1000);

    await cas.log("Attempting to access service once again, now without SSO session");
    await cas.gotoLogin(page, ssoDisabledService2);
    await cas.loginWith(page);
    ticket = await cas.assertTicketParameter(page);
    await cas.sleep(1000);
    json = await cas.validateTicket(ssoDisabledService2, ticket);
    authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes.randomNumber.length === 1);

    await cas.log("Attempting to access service once again, now without SSO session");
    await cas.gotoLogin(page, ssoDisabledService2);
    await cas.loginWith(page, "casuser2");
    ticket = await cas.assertTicketParameter(page);
    await cas.sleep(1000);
    json = await cas.validateTicket(ssoDisabledService2, ticket);
    authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes.randomNumber.length === 1);

    await cas.closeBrowser(browser);
})();
