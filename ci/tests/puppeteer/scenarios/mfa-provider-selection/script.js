
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const service = "https://localhost:9859/anything/cas";
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertVisibility(page, "#mfa-gauth");
    await cas.assertVisibility(page, "#mfa-yubikey");
    await cas.assertVisibility(page, "#mfaOptional");
    await cas.click(page, "#btnMfaOptional");
    await cas.sleep(2000);
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(service, ticket);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user.includes("casuser"));
    assert(authenticationSuccess.attributes["authenticationMethod"][0] === "Static Credentials");
    assert(authenticationSuccess.attributes["successfulAuthenticationHandlers"][0] === "Static Credentials");
    assert(authenticationSuccess.attributes["credentialType"][0] === "UsernamePasswordCredential");
    await cas.closeBrowser(browser);
})();
