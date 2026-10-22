const assert = require("assert");
const cas = require("../../cas.js");

/*
 * Counts the items memcached holds, read straight from the server with the stats command over its text protocol.
 */
async function storedItems() {
    const output = await cas.dockerExec("memcached", ["sh", "-c", "printf 'stats\\r\\nquit\\r\\n' | nc -w 2 127.0.0.1 11211"]);
    const line = output.split("\n").find((value) => value.startsWith("STAT curr_items "));
    assert(line !== undefined, "memcached must report its current items");
    return Number(line.trim().split(" ").pop());
}

(async () => {
    await cas.logg("Tickets kept in memcached are shared by both CAS nodes");
    await cas.verifyTicketRegistryAcrossNodes();

    await cas.logg("A login writes the ticket-granting ticket to memcached and logging out removes it");
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        const before = await storedItems();
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await cas.assertCookie(page);
        const after = await storedItems();
        assert(after > before, `memcached must hold the new ticket-granting ticket: ${before} before, ${after} after`);
        await cas.gotoLogout(page);
        await cas.assertCookie(page, false);
        assert(await storedItems() < after, "Logging out must remove the ticket-granting ticket from memcached");
    } finally {
        await cas.closeBrowser(browser);
    }
})();
