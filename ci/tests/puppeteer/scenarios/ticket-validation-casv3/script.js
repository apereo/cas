
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const service1 = "https://localhost:9859/anything/sample";
    await cas.gotoLogin(page, service1);
    await cas.loginWith(page);
    let ticket = await cas.assertTicketParameter(page);
    const body = await cas.validateTicket(service1, ticket, "XML");
    assert(body.includes("<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"));
    assert(body.includes("<cas:user>CASUSER@EXAMPLE.ORG</cas:user>"));
    assert(body.includes("<cas:credentialType>UsernamePasswordCredential</cas:credentialType>"));
    assert(body.includes("<cas:isFromNewLogin>true</cas:isFromNewLogin>"));
    assert(body.includes("<cas:authenticationMethod>STATIC</cas:authenticationMethod>"));
    assert(body.includes("<cas:successfulAuthenticationHandlers>STATIC</cas:successfulAuthenticationHandlers>"));
    assert(body.includes("<cas:longTermAuthenticationRequestTokenUsed>false</cas:longTermAuthenticationRequestTokenUsed>"));
    assert(body.includes("<cas:memberOf>Colleague Admins</cas:memberOf>"));

    const service2 = "http://localhost:9889/anything/cas";
    await cas.gotoLogin(page, service2);
    ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(service2, ticket);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "CAS_USER_APEREO@APEREO.ORG");
    await browser.close();
})();
