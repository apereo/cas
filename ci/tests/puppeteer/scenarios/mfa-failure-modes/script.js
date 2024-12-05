
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    let service = "http://localhost:9889/anything/closed";
    await cas.logg("Checking CLOSED failure mode");
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#content h2", "MFA Provider Unavailable");

    await cas.gotoLogout(page);

    service = "http://localhost:9889/anything/phantom";
    await cas.logg("Checking PHANTOM failure mode");
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(2000);
    let ticket = await cas.assertTicketParameter(page);
    let body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.logg(body);
    let json = JSON.parse(body.toString());
    let authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes.bypassMultifactorAuthentication[0] === true);
    assert(authenticationSuccess.attributes.bypassedMultifactorAuthenticationProviderId[0] === "mfa-yubikey");
    assert(authenticationSuccess.attributes.authenticationContext[0] === "mfa-yubikey");

    await cas.gotoLogout(page);

    service = "http://localhost:9889/anything/open";
    await cas.logg("Checking OPEN failure mode");
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(2000);
    ticket = await cas.assertTicketParameter(page);
    body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.logg(body);
    json = JSON.parse(body.toString());
    authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes.bypassMultifactorAuthentication[0] === true);
    assert(authenticationSuccess.attributes.bypassedMultifactorAuthenticationProviderId[0] === "mfa-yubikey");
    assert(authenticationSuccess.attributes.authenticationContext === undefined);

    await cas.gotoLogout(page);

    service = "http://localhost:9889/anything/none";
    await cas.logg("Checking NONE failure mode");
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertTextContent(page, "#login h3", "Use your registered YubiKey device(s) to authenticate.");
    await cas.assertVisibility(page, "#token");

    await cas.gotoLogout(page);
    service = "http://localhost:9889/anything/undefined";
    await cas.logg("Checking UNDEFINED failure mode");
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(2000);
    ticket = await cas.assertTicketParameter(page);
    body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.logg(body);
    json = JSON.parse(body.toString());
    authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes.bypassMultifactorAuthentication[0] === true);
    assert(authenticationSuccess.attributes.bypassedMultifactorAuthenticationProviderId[0] === "mfa-yubikey");
    assert(authenticationSuccess.attributes.authenticationContext[0] === "mfa-yubikey");

    await browser.close();
})();
