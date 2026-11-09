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
}

async function mysql(sql) {
    return cas.dockerExec("mysql-server", ["mysql", "-uroot", "-ppassword", "-N", "-D", "cas", "-e", sql]);
}

/**
 * Each grant updates the services the shared ticket-granting ticket tracks. With the distributed lock, grants on the
 * two nodes are serialized and each one reads the ticket the previous one wrote, so no service ticket goes missing.
 * The tracked services are read from the row both nodes share. The ssoSessions endpoint streams the registry outside
 * a transaction, which MySQL (cursor fetch) does not support, so it is not used here.
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

    await cas.logg("Every tracked service ticket validates once, on either node");
    for (let index = 0; index < granted.length; index++) {
        const service = `https://localhost:9859/anything/concurrent-grant-${index}`;
        const payload = await cas.validateTicket(service, granted[index], "JSON", NODES[(index + 1) % NODES.length]);
        assert.equal(payload.serviceResponse.authenticationSuccess?.user, "casuser");
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
        await verifyConcurrentServiceTicketValidation(ticketGrantingTicket);
        await verifyConcurrentServiceTicketGrants(ticketGrantingTicket);
    } finally {
        await cas.doDelete(`https://localhost:${NODES[0]}/cas/v1/tickets/${ticketGrantingTicket}`);
    }
})();
