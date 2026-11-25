const assert = require("assert");
const cas = require("../../cas.js");

const ACTUATOR = "https://localhost:8443/cas/actuator";
const SERVICE = "https://localhost:9859/anything/firestore";
const FIRESTORE = "http://localhost:9980/v1/projects/apereo-cas-gcp/databases/(default)/documents";

/*
 * Reads and writes the Firestore emulator through its REST API. "Bearer owner" is the emulator's administrative
 * token, which bypasses security rules. Collection names come from GoogleCloudFirestoreTicketCatalogConfigurationValuesProvider
 * and cas.service-registry.google-cloud-firestore.collection.
 */
async function firestore(path, method = "GET", body = undefined) {
    const response = await fetch(`${FIRESTORE}/${path}`, {
        method: method,
        headers: {"Authorization": "Bearer owner", "Content-Type": "application/json"},
        body: body === undefined ? undefined : JSON.stringify(body)
    });
    const text = await response.text();
    await cas.log(`Firestore ${method} ${path}: ${response.status}`);
    return {status: response.status, document: text.length > 0 ? JSON.parse(text) : undefined};
}

async function findDocument(collection, id) {
    const result = await firestore(`${collection}/${encodeURIComponent(id)}`);
    if (result.status === 404) {
        return undefined;
    }
    assert.equal(result.status, 200);
    return result.document;
}

const serviceRegistry = {
    insert: async (definition) => {
        const result = await firestore(`RegisteredServices/${definition.id}`, "PATCH",
            {fields: {json: {stringValue: JSON.stringify(definition)}}});
        assert.equal(result.status, 200);
    },
    contains: async (id) => await findDocument("RegisteredServices", String(id)) !== undefined
};

async function ssoSessionsFor(user) {
    const json = JSON.parse(await cas.doRequest(`${ACTUATOR}/ssoSessions/users/${user}`, "GET", {"Accept": "application/json"}, 200));
    return json.activeSsoSessions;
}

async function verifyTicketRegistry() {
    await cas.logg("Removing SSO sessions left by an earlier attempt");
    await cas.doRequest(`${ACTUATOR}/ssoSessions?type=ALL&from=0&count=1000`, "DELETE", {"Accept": "application/json"}, 200);
    assert.equal((await ssoSessionsFor("casuser")).length, 0);

    await serviceRegistry.insert({
        "@class": "org.apereo.cas.services.CasRegisteredService",
        "serviceId": `^${SERVICE}.*`,
        "name": "Firestore",
        "id": 1000,
        "evaluationOrder": 1000
    });

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await cas.assertCookie(page);

        await cas.logg("The ticket-granting ticket is stored in Firestore");
        const sessions = await ssoSessionsFor("casuser");
        assert.equal(sessions.length, 1);
        const tgt = sessions[0]["ticket_granting_ticket"];
        assert(tgt.startsWith("TGT-"));
        const tgtDocument = await findDocument("ticketGrantingTicketsCollection", tgt);
        assert(tgtDocument !== undefined, `Ticket-granting ticket ${tgt} must be stored in Firestore`);
        assert.equal(tgtDocument.fields.principal.stringValue, "casuser");
        assert.equal(tgtDocument.fields.prefix.stringValue, "TGT");
        assert.equal(tgtDocument.fields.type.stringValue, "org.apereo.cas.ticket.TicketGrantingTicketImpl");
        const allSessions = JSON.parse(await cas.doRequest(`${ACTUATOR}/ssoSessions?type=ALL`, "GET", {"Accept": "application/json"}, 200));
        assert.equal(allSessions.totalSsoSessions, 1);
        assert(allSessions.activeSsoSessions.some((session) => session["ticket_granting_ticket"] === tgt));

        await cas.logg("A service ticket is issued from the single sign-on session once the service is loaded from Firestore");
        let ticket = null;
        for (let attempt = 1; attempt <= 20 && ticket === null; attempt++) {
            await cas.gotoLogin(page, SERVICE);
            await cas.sleep(500);
            ticket = new URL(page.url()).searchParams.get("ticket");
            if (ticket === null) {
                await cas.sleep(1000);
            }
        }
        assert(ticket !== null, `Service ${SERVICE} must be authorized after it is written to Firestore`);
        const stDocument = await findDocument("serviceTicketsCollection", ticket);
        assert(stDocument !== undefined, `Service ticket ${ticket} must be stored in Firestore`);
        assert.equal(stDocument.fields.service.stringValue, SERVICE);
        assert.equal(stDocument.fields.prefix.stringValue, "ST");

        await cas.logg("Validating the service ticket removes it from Firestore");
        let payload = await cas.validateTicket(SERVICE, ticket);
        assert.equal(payload.serviceResponse.authenticationSuccess.user, "casuser");
        assert.equal(await findDocument("serviceTicketsCollection", ticket), undefined);
        payload = await cas.validateTicket(SERVICE, ticket);
        assert.equal(payload.serviceResponse.authenticationFailure.code, "INVALID_TICKET");

        await cas.logg("Logging out removes the ticket-granting ticket from Firestore");
        await cas.gotoLogout(page);
        await cas.sleep(1000);
        assert.equal(await findDocument("ticketGrantingTicketsCollection", tgt), undefined);
        assert.equal((await ssoSessionsFor("casuser")).length, 0);
        await cas.gotoLogin(page, SERVICE);
        await cas.sleep(1000);
        await cas.assertTicketParameter(page, false);
        await cas.assertVisibility(page, "#username");
    } finally {
        await cas.closeBrowser(browser);
    }
}

(async () => {
    await verifyTicketRegistry();
    await cas.verifyServiceRegistry(serviceRegistry);
})();
