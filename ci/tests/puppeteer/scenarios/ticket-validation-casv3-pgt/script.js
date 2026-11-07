
const assert = require("assert");
const cas = require("../../cas.js");
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");

const PROXIED = "https://localhost:9859/anything";
const PGT_URL = `${PROXIED}/pgt`;

async function validateTicket(service, ticket, format = "JSON", pgtUrl = PGT_URL) {
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/proxyValidate?service=${service}&ticket=${ticket}&format=${format}&pgtUrl=${pgtUrl}`);
    await cas.log(body);
    return body;
}

/**
 * Obtains the proxy-granting ticket for a service: the service ticket is validated with a callback URL, and the
 * proxy-granting ticket comes back encrypted with the public key of the service. The RSA padding is removed here
 * rather than by Node, which no longer decrypts PKCS#1 v1.5 padding.
 *
 * @param service the service that proxies
 * @param ticket its service or proxy ticket
 * @param pgtUrl the proxy callback URL
 * @returns the proxy-granting ticket
 */
async function proxyGrantingTicket(service, ticket, pgtUrl = PGT_URL) {
    const body = await validateTicket(service, ticket, "XML", pgtUrl);
    const encrypted = body.match(/<cas:proxyGrantingTicket>(.*?)<\/cas:proxyGrantingTicket>/)[1];
    const decrypted = crypto.privateDecrypt(
        {
            key: fs.readFileSync(path.join(__dirname, "private.key"), "utf8"),
            padding: crypto.constants.RSA_NO_PADDING
        },
        Buffer.from(encrypted, "base64")
    ).toString("utf8");
    return decrypted.substring(decrypted.indexOf("PGT-"));
}

/**
 * Requests a proxy ticket for a target service.
 *
 * @param target the proxied service
 * @param pgt the proxy-granting ticket
 * @returns the proxy ticket, or undefined if CAS refused to issue one
 */
async function proxyTicket(target, pgt) {
    const body = await requestProxyTicket(target, pgt);
    return body.match(/<cas:proxyTicket>(.*?)<\/cas:proxyTicket>/)?.[1];
}

async function requestProxyTicket(target, pgt) {
    const body = await cas.doRequest(`https://localhost:8443/cas/proxy?service=${target}&pgt=${pgt}`, "GET", {}, 0);
    await cas.log(body);
    return body;
}

async function serviceTicket(page, service) {
    await cas.gotoLogin(page, service);
    return cas.assertTicketParameter(page);
}

async function assertProxyCallbackRefused(service, pgtUrl, page) {
    const body = await validateTicket(service, await serviceTicket(page, service), "JSON", pgtUrl);
    const response = JSON.parse(body).serviceResponse;
    assert(response.authenticationSuccess === undefined, `${service} must not receive a proxy-granting ticket for ${pgtUrl}`);
    assert.equal(response.authenticationFailure.code, "INVALID_PROXY_CALLBACK");
}

async function assertProxyCallbackAccepted(service, pgtUrl, page) {
    const body = await validateTicket(service, await serviceTicket(page, service), "JSON", pgtUrl);
    const success = JSON.parse(body).serviceResponse.authenticationSuccess;
    assert.equal(success.user, "casuser");
    assert(success.proxyGrantingTicket.startsWith("PGTIOU-"), `${service} must receive a proxy-granting ticket IOU for ${pgtUrl}`);
}

/**
 * The proxy policy of a service decides which callback URLs may receive a proxy-granting ticket; the callback must
 * also answer with an acceptable status. A refused callback fails validation with INVALID_PROXY_CALLBACK.
 *
 * @param page the page, with a single sign-on session
 */
async function verifyProxyCallbackPolicies(page) {
    await cas.logg("A service whose proxy policy refuses proxying gets no proxy-granting ticket");
    await assertProxyCallbackRefused(`${PROXIED}/proxyrefused`, PGT_URL, page);

    await cas.logg("A callback that answers with an unacceptable status gets no proxy-granting ticket");
    await assertProxyCallbackRefused(`${PROXIED}/cas`, "https://localhost:9859/status/404", page);

    await cas.logg("An exact-match proxy policy accepts only the callback URL it names, not one it is a prefix of");
    await assertProxyCallbackRefused(`${PROXIED}/proxyexact`, `${PROXIED}/pgtexactly`, page);
    await assertProxyCallbackAccepted(`${PROXIED}/proxyexact`, `${PROXIED}/pgtexact`, page);

    await cas.logg("A proxy policy that reuses the service id accepts only callbacks matching that id");
    await assertProxyCallbackRefused(`${PROXIED}/proxyself`, PGT_URL, page);
    await assertProxyCallbackAccepted(`${PROXIED}/proxyself`, `${PROXIED}/proxyself/callback`, page);
}

/**
 * Proxy requests and proxy ticket validations that must fail, and a proxy chain of two hops: the proxied service
 * receives its own proxy-granting ticket, and the final proxy ticket lists both callbacks, most recent first.
 *
 * @param pgt a proxy-granting ticket issued to the service at /anything/cas
 */
async function verifyProxyFailuresAndChains(pgt) {
    await cas.logg("A proxy-granting ticket unknown to CAS issues no proxy ticket");
    const failure = await requestProxyTicket(`${PROXIED}/ptdefault`, "PGT-1-unknown-ticket");
    assert(failure.includes("<cas:proxyFailure code=\"INVALID_TICKET\">"));

    await cas.logg("A proxy ticket validated for another service is refused, and the failed attempt counts as a use");
    const ticket = await proxyTicket(`${PROXIED}/ptdefault`, pgt);
    let response = JSON.parse(await cas.doRequest(`https://localhost:8443/cas/p3/proxyValidate?service=${PROXIED}/ptonce&ticket=${ticket}&format=JSON`)).serviceResponse;
    assert.equal(response.authenticationFailure.code, "INVALID_SERVICE");
    assert(await proxyTicketValidates(`${PROXIED}/ptdefault`, ticket), "A proxy ticket allowed ten uses survives one failed attempt");

    await cas.logg("A proxied service that may proxy again builds a chain of two proxies");
    const hopService = `${PROXIED}/proxyhop`;
    const hopCallback = `${PROXIED}/pgthop`;
    const secondPgt = await proxyGrantingTicket(hopService, await proxyTicket(hopService, pgt), hopCallback);
    assert(secondPgt.startsWith("PGT-"));

    const chainedTicket = await proxyTicket(`${PROXIED}/ptdefault`, secondPgt);
    response = JSON.parse(await cas.doRequest(`https://localhost:8443/cas/p3/proxyValidate?service=${PROXIED}/ptdefault&ticket=${chainedTicket}&format=JSON`)).serviceResponse;
    assert.equal(response.authenticationSuccess.user, "casuser");
    assert.deepStrictEqual(response.authenticationSuccess.proxies, [hopCallback, PGT_URL]);

    const chainedXmlTicket = await proxyTicket(`${PROXIED}/ptdefault`, secondPgt);
    const xml = await cas.doRequest(`https://localhost:8443/cas/p3/proxyValidate?service=${PROXIED}/ptdefault&ticket=${chainedXmlTicket}`);
    await cas.log(xml);
    const proxies = [...xml.matchAll(/<cas:proxy>(.*?)<\/cas:proxy>/g)].map((match) => match[1]);
    assert.deepStrictEqual(proxies, [hopCallback, PGT_URL]);

    await cas.logg("The CAS 2.0 response lists each proxy of the chain once, most recent first");
    const chainedCas2Ticket = await proxyTicket(`${PROXIED}/ptdefault`, secondPgt);
    const cas2Xml = await cas.doRequest(`https://localhost:8443/cas/proxyValidate?service=${PROXIED}/ptdefault&ticket=${chainedCas2Ticket}`);
    await cas.log(cas2Xml);
    assert(cas2Xml.includes("<cas:user>casuser</cas:user>"), "The CAS 2.0 proxy ticket validation must succeed");
    assert.equal((cas2Xml.match(/<cas:proxies>/g) || []).length, 1, "The CAS 2.0 response must carry one proxies element");
    const cas2Proxies = [...cas2Xml.matchAll(/<cas:proxy>(.*?)<\/cas:proxy>/g)].map((match) => match[1].trim());
    assert.deepStrictEqual(cas2Proxies, [hopCallback, PGT_URL]);
}

/**
 * Validates a proxy ticket for its target service.
 *
 * @param target the proxied service
 * @param ticket the proxy ticket
 * @returns true if the ticket validated
 */
async function proxyTicketValidates(target, ticket) {
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/proxyValidate?service=${target}&ticket=${ticket}&format=JSON`, "GET", {}, 0);
    await cas.log(body);
    return JSON.parse(body).serviceResponse.authenticationSuccess !== undefined;
}

/**
 * The proxy ticket policy of the proxied service overrides the global one, which allows ten uses within a minute,
 * and the proxy-granting ticket policy of the proxying service limits how long it can issue proxy tickets.
 *
 * @param page the page, with a single sign-on session
 * @param pgt a proxy-granting ticket under the global policy
 */
async function verifyProxyExpirationPolicies(page, pgt) {
    await cas.logg("Proxy tickets for a service without a policy follow the global policy");
    const defaultTicket = await proxyTicket(`${PROXIED}/ptdefault`, pgt);
    assert(await proxyTicketValidates(`${PROXIED}/ptdefault`, defaultTicket));
    assert(await proxyTicketValidates(`${PROXIED}/ptdefault`, defaultTicket));

    await cas.logg("Proxy tickets for a service that allows one use");
    const onceTicket = await proxyTicket(`${PROXIED}/ptonce`, pgt);
    assert(await proxyTicketValidates(`${PROXIED}/ptonce`, onceTicket));
    assert(!await proxyTicketValidates(`${PROXIED}/ptonce`, onceTicket), "Second use of the proxy ticket must be refused");

    await cas.logg("Proxy tickets for a service with a short time to live");
    const shortTicket = await proxyTicket(`${PROXIED}/ptshort`, pgt);
    await cas.sleep(3000);
    assert(!await proxyTicketValidates(`${PROXIED}/ptshort`, shortTicket), "Proxy ticket must expire after its time to live");

    await cas.logg("A proxy-granting ticket stops issuing proxy tickets after the time to live of its service policy");
    const service = `${PROXIED}/pgtshort`;
    await cas.gotoLogin(page, service);
    const shortPgt = await proxyGrantingTicket(service, await cas.assertTicketParameter(page));
    assert(await proxyTicket(`${PROXIED}/ptdefault`, shortPgt) !== undefined);
    await cas.sleep(4000);
    assert(await proxyTicket(`${PROXIED}/ptdefault`, shortPgt) === undefined, "Expired proxy-granting ticket must not issue proxy tickets");
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

    await cas.gotoLogin(page, service);
    const pgt = await proxyGrantingTicket(service, await cas.assertTicketParameter(page));
    await verifyProxyExpirationPolicies(page, pgt);
    await verifyProxyCallbackPolicies(page);
    await verifyProxyFailuresAndChains(pgt);
    await cas.closeBrowser(browser);
})();
