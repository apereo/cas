
const assert = require("assert");
const cas = require("../../cas.js");
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");

const PROXIED = "https://localhost:9859/anything";

async function validateTicket(service, ticket, format = "JSON") {
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/proxyValidate?service=${service}&ticket=${ticket}&format=${format}&pgtUrl=https://localhost:9859/anything/pgt`);
    await cas.log(body);
    return body;
}

/**
 * Obtains the proxy-granting ticket for a service: the service ticket is validated with a callback URL, and the
 * proxy-granting ticket comes back encrypted with the public key of the service. The RSA padding is removed here
 * rather than by Node, which no longer decrypts PKCS#1 v1.5 padding.
 *
 * @param service the service that proxies
 * @param ticket its service ticket
 * @returns the proxy-granting ticket
 */
async function proxyGrantingTicket(service, ticket) {
    const body = await validateTicket(service, ticket, "XML");
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
    const body = await cas.doRequest(`https://localhost:8443/cas/proxy?service=${target}&pgt=${pgt}`, "GET", {}, 0);
    await cas.log(body);
    return body.match(/<cas:proxyTicket>(.*?)<\/cas:proxyTicket>/)?.[1];
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
    await cas.closeBrowser(browser);
})();
