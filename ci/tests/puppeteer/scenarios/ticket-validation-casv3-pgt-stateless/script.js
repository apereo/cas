
const assert = require("assert");
const cas = require("../../cas.js");

const PROXIED_SERVICE = "https://localhost:9859/anything/sample";

async function proxyValidateTicket(service, ticket, format = "JSON") {
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/proxyValidate?service=${service}&ticket=${ticket}&format=${format}&pgtUrl=${PROXIED_SERVICE}`);
    await cas.log(body);
    return body;
}

async function getProxyTicket(service, ticket) {
    const body = await cas.doRequest(`https://localhost:8443/cas/proxy?service=${service}&pgt=${ticket}`);
    await cas.log(body);
    return body;
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";

    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(3000);

    let ticket = await cas.assertTicketParameter(page);
    let body = await proxyValidateTicket(service, ticket);
    let json = JSON.parse(body);
    let authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    assert(authenticationSuccess.attributes.uid === undefined);
    assert(authenticationSuccess.attributes.phone === undefined);
    assert(authenticationSuccess.attributes.lastname === undefined);
    assert(authenticationSuccess.attributes.firstname === undefined);
    assert(authenticationSuccess.attributes.credentialType !== undefined);
    assert(authenticationSuccess.attributes.proxyGrantingTicket !== undefined);
    assert(authenticationSuccess.attributes.mail[0] === "casuser@example.org");

    await cas.gotoLogin(page, service);
    ticket = await cas.assertTicketParameter(page);
    body = await proxyValidateTicket(service, ticket, "XML");
    assert(body.includes("<cas:proxyGrantingTicket>"));
    assert(body.includes("<cas:authenticationMethod>STATIC</cas:authenticationMethod>"));
    assert(body.includes("<cas:credentialType>UsernamePasswordCredential</cas:credentialType>"));
    assert(body.includes("<cas:user>casuser</cas:user>"));
    assert(body.includes("<cas:longTermAuthenticationRequestTokenUsed>false</cas:longTermAuthenticationRequestTokenUsed>"));

    const pgtPattern = /<cas:proxyGrantingTicket>(.*?)<\/cas:proxyGrantingTicket>/;
    const pgtMatch = body.match(pgtPattern);
    assert(pgtMatch !== null);
    const proxyGrantingTicket = pgtMatch && pgtMatch[1];
    const proxyTicketResponse = await getProxyTicket(PROXIED_SERVICE, proxyGrantingTicket);
    assert(proxyTicketResponse !== undefined);

    const ptPattern = /<cas:proxyTicket>(.*?)<\/cas:proxyTicket>/;
    const ptMatch = proxyTicketResponse.match(ptPattern);
    assert(ptMatch !== null);
    const proxyTicket = ptMatch && ptMatch[1];
    assert(proxyTicket !== undefined);

    json = await cas.validateTicket(PROXIED_SERVICE, proxyTicket);
    authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    assert(authenticationSuccess.attributes.uid[0] === "casuser");
    assert(authenticationSuccess.attributes.phone[0] === "13477464523");
    assert(authenticationSuccess.attributes.lastname[0] === "User");
    assert(authenticationSuccess.attributes.firstname[0] === "CAS");
    assert(authenticationSuccess.attributes.credentialType[0] === "HttpBasedServiceCredential");
    assert(authenticationSuccess.attributes.proxyGrantingTicket === undefined);
    assert(authenticationSuccess.attributes.mail[0] === "casuser@example.org");
    assert(authenticationSuccess.attributes.username[0] === "casuser");
    await browser.close();
})();
