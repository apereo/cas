const assert = require("assert");
const cas = require("../../cas.js");

const NODES = [8443, 8444];
const CONCURRENT_REQUESTS = 10;

async function postForm(url, form, expectedStatus = 0) {
    const body = new URLSearchParams(form).toString();
    let status = 0;
    const response = await cas.doRequest(url, "POST", {
        "Accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded",
        "Content-Length": Buffer.byteLength(body)
    }, expectedStatus, body, (res) => {
        status = res.statusCode;
    });
    return {status, body: response};
}

async function actuator(port, path) {
    return cas.doRequest(`https://localhost:${port}/cas/actuator/${path}`, "GET", {"Accept": "application/json"}, 200);
}

/**
 * The ticketRegistry query of the JPA registry reads its rows inside a transaction of the registry, which MySQL needs
 * for the result set to stay open; the response is the list of matching tickets.
 */
async function queryTicketRegistry(port, type) {
    return actuator(port, `ticketRegistry/query?type=${type}&count=1000`);
}

/**
 * The service ticket is read before the lock is taken and validated against the shared table, so every request of
 * the race carries a copy with no use recorded. Only the request whose removal of the row counts one may succeed,
 * whatever the interleaving and on whichever node it lands.
 */
async function verifyConcurrentServiceTicketValidation(ticketGrantingTicket) {
    const service = "https://localhost:9859/anything/concurrent-validation";
    const grant = await postForm(`https://localhost:${NODES[0]}/cas/v1/tickets/${ticketGrantingTicket}`, {service}, 200);
    const serviceTicket = grant.body;
    assert(serviceTicket.startsWith("ST-"));

    await cas.logg(`Validating ${serviceTicket} ${CONCURRENT_REQUESTS} times at once across both nodes`);
    const responses = await Promise.all(Array.from({length: CONCURRENT_REQUESTS}, async (_, index) => {
        const port = NODES[index % NODES.length];
        const body = await cas.doRequest(`https://localhost:${port}/cas/p3/serviceValidate?service=${service}&ticket=${serviceTicket}&format=JSON`);
        return JSON.parse(body).serviceResponse;
    }));
    const successes = responses.filter((response) => response.authenticationSuccess !== undefined);
    const failures = responses.filter((response) => response.authenticationFailure !== undefined);
    await cas.log(`Successful validations: ${successes.length}, failure codes: ${failures.map((response) => response.authenticationFailure.code)}`);
    assert.equal(successes.length, 1, "A service ticket must validate exactly once");
    assert.equal(successes[0].authenticationSuccess.user, "casuser");
    assert.equal(failures.length, CONCURRENT_REQUESTS - 1);
    return {[serviceTicket]: service};
}

async function mysql(sql) {
    return cas.dockerExec("mysql-server", ["mysql", "-uroot", "-ppassword", "-N", "-D", "cas", "-e", sql]);
}

/**
 * Each grant updates the services the shared ticket-granting ticket tracks. With the distributed lock, grants on the
 * two nodes are serialized and each one reads the ticket the previous one wrote, so no service ticket goes missing.
 * The tracked services are read from the row both nodes share, and each node's ticketRegistry query must list every
 * service ticket issued by either node before they are validated.
 */
async function verifyConcurrentServiceTicketGrants(ticketGrantingTicket) {
    await cas.logg(`Granting ${CONCURRENT_REQUESTS} service tickets at once from one ticket-granting ticket across both nodes`);
    const grants = await Promise.all(Array.from({length: CONCURRENT_REQUESTS}, async (_, index) => {
        const port = NODES[index % NODES.length];
        const service = `https://localhost:9859/anything/concurrent-grant-${index}`;
        return postForm(`https://localhost:${port}/cas/v1/tickets/${ticketGrantingTicket}`, {service});
    }));
    await cas.log(`Grant responses: ${grants.map((grant) => `${grant.status}:${grant.body}`)}`);
    assert(grants.every((grant) => grant.status === 200 && grant.body.startsWith("ST-")), "Every grant must issue a service ticket");
    const granted = grants.map((grant) => grant.body);

    const tables = (await mysql("SELECT table_name FROM information_schema.tables WHERE table_schema='cas' AND table_name LIKE '%tickets'"))
        .split("\n").map((line) => line.trim()).filter((line) => line.length > 0);
    assert.equal(tables.length, 1, `Expected exactly one ticket table, found ${tables}`);
    const body = await mysql(`SELECT body FROM ${tables[0]} WHERE id='${ticketGrantingTicket}'`);
    assert(body.length > 0, `Ticket-granting ticket ${ticketGrantingTicket} must be stored`);
    const missing = granted.filter((serviceTicket) => !body.includes(serviceTicket));
    assert.equal(missing.length, 0, `Service tickets not tracked by the shared ticket-granting ticket: ${missing}`);

    for (const port of NODES) {
        await cas.log(`Querying service tickets through the ticket registry endpoint of node ${port}`);
        const serviceTickets = await queryTicketRegistry(port, "ST");
        const unlisted = granted.filter((serviceTicket) => !serviceTickets.includes(serviceTicket));
        assert.equal(unlisted.length, 0, `Service tickets not listed by node ${port}: ${unlisted}`);
    }

    await cas.logg("Every tracked service ticket validates once, on either node");
    for (let index = 0; index < granted.length; index++) {
        const service = `https://localhost:9859/anything/concurrent-grant-${index}`;
        const payload = await cas.validateTicket(service, granted[index], "JSON", NODES[(index + 1) % NODES.length]);
        assert.equal(payload.serviceResponse.authenticationSuccess?.user, "casuser");
    }
    return Object.fromEntries(granted.map((serviceTicket, index) => [serviceTicket, `https://localhost:9859/anything/concurrent-grant-${index}`]));
}

/**
 * The ssoSessions endpoint reads the JPA registry inside a ticket transaction (SingleSignOnSessionsEndpoint
 * readInTicketTransaction), so on MySQL both nodes list the shared session with every service ticket the concurrency
 * steps issued from it, and the ticketRegistry query lists the ticket-granting ticket.
 */
async function verifySessionsThroughActuators(ticketGrantingTicket, issued) {
    for (const port of NODES) {
        await cas.logg(`Reading the shared single sign-on session through the actuator endpoints of node ${port}`);
        const payload = JSON.parse(await actuator(port, "ssoSessions?type=ALL"));
        const session = payload.activeSsoSessions.find((sso) => sso.ticket_granting_ticket === ticketGrantingTicket);
        assert(session !== undefined, `Node ${port} does not list ${ticketGrantingTicket}: ${JSON.stringify(payload.activeSsoSessions)}`);
        assert.equal(session.authenticated_principal, "casuser");
        assert(payload.totalSsoSessions >= 1);
        for (const [serviceTicket, service] of Object.entries(issued)) {
            assert.equal(session.authenticated_services[serviceTicket]?.id, service,
                `Node ${port} does not list ${serviceTicket} for ${service} in the session`);
        }

        const userSessions = JSON.parse(await actuator(port, "ssoSessions/users/casuser")).activeSsoSessions;
        assert(userSessions.some((sso) => sso.ticket_granting_ticket === ticketGrantingTicket),
            `Node ${port} does not list ${ticketGrantingTicket} among the sessions of casuser`);

        const ticketGrantingTickets = await queryTicketRegistry(port, "TGT");
        assert(ticketGrantingTickets.includes(ticketGrantingTicket), `Node ${port} does not list ${ticketGrantingTicket} in the ticket registry`);
    }
}

(async () => {
    await cas.verifyTicketRegistryAcrossNodes();

    const credentials = new URLSearchParams({username: "casuser", password: "Mellon"}).toString();
    const ticketGrantingTicket = await cas.doRequest(`https://localhost:${NODES[0]}/cas/v1/tickets`, "POST", {
        "Accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded",
        "Content-Length": Buffer.byteLength(credentials)
    }, 201, credentials);
    assert(ticketGrantingTicket.startsWith("TGT-"));
    try {
        const validated = await verifyConcurrentServiceTicketValidation(ticketGrantingTicket);
        const granted = await verifyConcurrentServiceTicketGrants(ticketGrantingTicket);
        await verifySessionsThroughActuators(ticketGrantingTicket, {...validated, ...granted});
    } finally {
        await cas.doDelete(`https://localhost:${NODES[0]}/cas/v1/tickets/${ticketGrantingTicket}`);
    }
})();
