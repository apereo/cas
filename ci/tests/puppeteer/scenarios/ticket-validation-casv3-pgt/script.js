
const assert = require("assert");
const cas = require("../../cas.js");

async function validateTicket(service, ticket, format = "JSON") {
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/proxyValidate?service=${service}&ticket=${ticket}&format=${format}&pgtUrl=https://github.com/apereo/cas`);
    await cas.log(body);
    return body;
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";

    await cas.gotoLogin(page, service);
    await cas.loginWith(page);

    let ticket = await cas.assertTicketParameter(page);
    let body = await validateTicket(service, ticket);
    const json = JSON.parse(body);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    assert(authenticationSuccess.attributes.credentialType !== undefined);
    assert(authenticationSuccess.attributes.proxyGrantingTicket !== undefined);

    await cas.gotoLogin(page, service);
    ticket = await cas.assertTicketParameter(page);
    body = await validateTicket(service, ticket, "XML");
    assert(body.includes("<cas:proxyGrantingTicket>"));
    assert(body.includes("<cas:userAgent>"));
    assert(body.includes("<cas:authenticationMethod>STATIC</cas:authenticationMethod>"));
    assert(body.includes("<cas:credentialType>UsernamePasswordCredential</cas:credentialType>"));
    assert(body.includes("<cas:user>casuser</cas:user>"));
    await cas.closeBrowser(browser);
})();
