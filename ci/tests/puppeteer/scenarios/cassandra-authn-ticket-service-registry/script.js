const assert = require("assert");
const cas = require("../../cas.js");

async function cql(query) {
    return cas.dockerExec("cassandra", ["cqlsh", "--ssl", "-e", query]);
}

async function countRows(query) {
    const output = await cql(query);
    const line = output.split("\n").map((value) => value.trim()).find((value) => /^\d+$/.test(value));
    return Number(line);
}

async function storedTicketGrantingTickets() {
    return countRows("SELECT count(*) FROM cas.ticketgrantingticketstable;");
}

async function assertLogin(page, username, password, success) {
    await cas.gotoLogout(page);
    await cas.gotoLogin(page);
    await cas.loginWith(page, username, password);
    await cas.sleep(1000);
    await cas.assertCookie(page, success);
}

(async () => {
    await cas.logg("Tickets kept in Cassandra are shared by both CAS nodes");
    await cas.verifyTicketRegistryAcrossNodes();

    await cas.logg("Service definitions are kept in Cassandra");
    await cas.verifyServiceRegistry({
        insert: async (definition) => {
            await cql(`INSERT INTO cas.casservices (id, data) VALUES (${definition.id}, '${JSON.stringify(definition)}');`);
        },
        contains: async (id) => (await countRows(`SELECT count(*) FROM cas.casservices WHERE id = ${id};`)) > 0
    });

    await cas.logg("Accounts are verified against the Cassandra users table");
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await assertLogin(page, "cassandrauser", "WrongPassword", false);
        await assertLogin(page, "unknown-cassandra-user", "Cassandr4", false);

        const before = await storedTicketGrantingTickets();
        await assertLogin(page, "cassandrauser", "Cassandr4", true);
        const after = await storedTicketGrantingTickets();
        assert(after === before + 1, `The ticket-granting ticket must be stored in Cassandra: ${before} before, ${after} after`);

        await cas.gotoLogout(page);
        await cas.assertCookie(page, false);
        const removed = await storedTicketGrantingTickets();
        assert(removed === before, `Logging out must remove the ticket-granting ticket from Cassandra: ${removed} left`);
    } finally {
        await cas.closeBrowser(browser);
    }
})();
