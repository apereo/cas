
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";

    await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    
    await cas.loginWith(page);
    const ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/serviceValidate?service=${service}&ticket=${ticket}`);
    await cas.log(body);
    assert(body.includes("<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"));
    assert(body.includes("<cas:user>casuser</cas:user>"));
    assert(body.includes("<cas:credentialType>UsernamePasswordCredential</cas:credentialType>"));
    assert(body.includes("<cas:isFromNewLogin>true</cas:isFromNewLogin>"));
    assert(body.includes("<cas:authenticationMethod>STATIC</cas:authenticationMethod>"));
    assert(body.includes("<cas:successfulAuthenticationHandlers>STATIC</cas:successfulAuthenticationHandlers>"));
    assert(body.includes("<cas:longTermAuthenticationRequestTokenUsed>false</cas:longTermAuthenticationRequestTokenUsed>"));

    await cas.log("A service ticket cannot be validated twice");
    const reused = await cas.doRequest(`https://localhost:8443/cas/serviceValidate?service=${service}&ticket=${ticket}`);
    await cas.log(reused);
    assert(reused.includes("<cas:authenticationFailure code=\"INVALID_TICKET\">"));

    await cas.log("A validation request without a ticket or without a service is invalid");
    const missingTicket = await cas.doRequest(`https://localhost:8443/cas/serviceValidate?service=${service}`);
    await cas.log(missingTicket);
    assert(missingTicket.includes("<cas:authenticationFailure code=\"INVALID_REQUEST\">"));
    const missingService = await cas.doRequest(`https://localhost:8443/cas/serviceValidate?ticket=${ticket}`);
    await cas.log(missingService);
    assert(missingService.includes("<cas:authenticationFailure code=\"INVALID_REQUEST\">"));
    await cas.closeBrowser(browser);
})();
